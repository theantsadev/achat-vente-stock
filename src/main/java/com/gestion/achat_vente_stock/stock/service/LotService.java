package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;
import com.gestion.achat_vente_stock.stock.model.Lot;
import com.gestion.achat_vente_stock.stock.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 37-38: Stock > Lots/Séries
 * - Ligne 37: Traçabilité lot obligatoire sur familles définies
 * - Ligne 38: Bloquer automatiquement lots expirés/non conformes
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LotService {

    private final LotRepository lotRepository;
    private final AuditService auditService;

    // Statuts de lot
    public static final String STATUT_ACTIF = "ACTIF";
    public static final String STATUT_BLOQUE = "BLOQUE";
    public static final String STATUT_EXPIRE = "EXPIRE";
    public static final String STATUT_NON_CONFORME = "NON_CONFORME";

    /**
     * TODO.YML Ligne 37: Créer un nouveau lot
     */
    public Lot creerLot(Article article, Fournisseur fournisseur, 
                        LocalDate dateFabrication, LocalDate dluo, LocalDate dlc,
                        Utilisateur utilisateur) {
        
        // Vérifier si traçabilité lot obligatoire pour cet article
        if (Boolean.TRUE.equals(article.getTracabiliteLot())) {
            validateDates(dateFabrication, dluo, dlc);
        }

        Lot lot = new Lot();
        lot.setNumero(genererNumeroLot(article.getCode()));
        lot.setArticle(article);
        lot.setFournisseur(fournisseur);
        lot.setDateFabrication(dateFabrication);
        lot.setDluo(dluo);
        lot.setDlc(dlc);
        lot.setStatut(STATUT_ACTIF);

        Lot saved = lotRepository.save(lot);

        // Audit
        auditService.logAction(utilisateur, "lot", saved.getId(),
                "CREATE", null, saved.toString(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 38: Bloquer un lot (manuellement)
     */
    public Lot bloquerLot(Long lotId, String motifBlocage, Utilisateur utilisateur) {
        Lot lot = trouverParId(lotId);
        String ancienStatut = lot.getStatut();

        lot.setStatut(STATUT_BLOQUE);
        lot.setMotifBlocage(motifBlocage);
        lot.setBloqueAt(LocalDateTime.now());

        Lot saved = lotRepository.save(lot);

        // Audit
        auditService.logAction(utilisateur, "lot", saved.getId(),
                "BLOQUER", ancienStatut, STATUT_BLOQUE + " - " + motifBlocage, null);

        return saved;
    }

    /**
     * Débloquer un lot
     */
    public Lot debloquerLot(Long lotId, Utilisateur utilisateur) {
        Lot lot = trouverParId(lotId);
        
        // Vérifier si le lot n'est pas expiré avant de débloquer
        if (lot.isExpire()) {
            throw new RuntimeException("Impossible de débloquer un lot expiré");
        }

        String ancienStatut = lot.getStatut();
        lot.setStatut(STATUT_ACTIF);
        lot.setMotifBlocage(null);
        lot.setBloqueAt(null);

        Lot saved = lotRepository.save(lot);

        // Audit
        auditService.logAction(utilisateur, "lot", saved.getId(),
                "DEBLOQUER", ancienStatut, STATUT_ACTIF, null);

        return saved;
    }

    /**
     * TODO.YML Ligne 38: Vérifier et bloquer automatiquement les lots expirés
     * Tâche planifiée qui s'exécute tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void bloquerLotsExpires() {
        LocalDate today = LocalDate.now();
        
        // Bloquer les lots dont la DLC est dépassée
        List<Lot> lotsExpiresDLC = lotRepository.findLotsExpiresDLC(today);
        for (Lot lot : lotsExpiresDLC) {
            lot.setStatut(STATUT_EXPIRE);
            lot.setMotifBlocage("DLC dépassée le " + lot.getDlc());
            lot.setBloqueAt(LocalDateTime.now());
            lotRepository.save(lot);
        }

        // Bloquer les lots dont la DLUO est dépassée
        List<Lot> lotsExpiresDLUO = lotRepository.findLotsExpiresDLUO(today);
        for (Lot lot : lotsExpiresDLUO) {
            lot.setStatut(STATUT_EXPIRE);
            lot.setMotifBlocage("DLUO dépassée le " + lot.getDluo());
            lot.setBloqueAt(LocalDateTime.now());
            lotRepository.save(lot);
        }
    }

    /**
     * Obtenir les lots bientôt expirés (alerte)
     */
    @Transactional(readOnly = true)
    public List<Lot> getLotsBientotExpires(int joursAvantExpiration) {
        LocalDate today = LocalDate.now();
        LocalDate dateAlerte = today.plusDays(joursAvantExpiration);
        
        List<Lot> lotsDLC = lotRepository.findLotsBientotExpiresDLC(today, dateAlerte);
        List<Lot> lotsDLUO = lotRepository.findLotsBientotExpiresDLUO(today, dateAlerte);
        
        lotsDLC.addAll(lotsDLUO);
        return lotsDLC;
    }

    /**
     * TODO.YML Ligne 37: Vérifier si un lot est disponible pour utilisation
     */
    public boolean isLotDisponible(String numeroLot) {
        Optional<Lot> lotOpt = lotRepository.findByNumero(numeroLot);
        return lotOpt.map(Lot::isDisponible).orElse(false);
    }

    /**
     * Générer un numéro de lot unique
     */
    private String genererNumeroLot(String codeArticle) {
        String prefix = "LOT-" + codeArticle + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Optional<String> lastNumero = lotRepository.findLastNumeroByPrefix(prefix);
        
        int sequence = 1;
        if (lastNumero.isPresent()) {
            String last = lastNumero.get();
            String seqStr = last.substring(last.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(seqStr) + 1;
        }
        
        return prefix + String.format("%04d", sequence);
    }

    private void validateDates(LocalDate dateFabrication, LocalDate dluo, LocalDate dlc) {
        LocalDate today = LocalDate.now();
        
        if (dateFabrication != null && dateFabrication.isAfter(today)) {
            throw new RuntimeException("La date de fabrication ne peut pas être dans le futur");
        }
        
        if (dlc != null && dlc.isBefore(today)) {
            throw new RuntimeException("La DLC est déjà dépassée");
        }
        
        if (dluo != null && dlc != null && dluo.isAfter(dlc)) {
            throw new RuntimeException("La DLUO ne peut pas être après la DLC");
        }
    }

    @Transactional(readOnly = true)
    public Lot trouverParId(Long id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lot non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Lot> trouverParNumero(String numero) {
        return lotRepository.findByNumero(numero);
    }

    @Transactional(readOnly = true)
    public List<Lot> listerTous() {
        return lotRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Lot> listerParArticle(Long articleId) {
        return lotRepository.findByArticleId(articleId);
    }

    @Transactional(readOnly = true)
    public List<Lot> listerParStatut(String statut) {
        return lotRepository.findByStatut(statut);
    }

    // TODO.YML Ligne 36: Lots pour allocation FEFO
    @Transactional(readOnly = true)
    public List<Lot> getLotsActifsFEFO(Long articleId) {
        return lotRepository.findLotsActifsByArticleFEFO(articleId);
    }

    // Lots pour allocation FIFO
    @Transactional(readOnly = true)
    public List<Lot> getLotsActifsFIFO(Long articleId) {
        return lotRepository.findLotsActifsByArticleFIFO(articleId);
    }
}
