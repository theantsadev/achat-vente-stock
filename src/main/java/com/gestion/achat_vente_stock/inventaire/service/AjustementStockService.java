package com.gestion.achat_vente_stock.inventaire.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.MotifAjustement;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.StatutAjustement;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire;
import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import com.gestion.achat_vente_stock.inventaire.repository.AjustementStockRepository;
import com.gestion.achat_vente_stock.inventaire.repository.InventaireRepository;
import com.gestion.achat_vente_stock.inventaire.repository.LigneInventaireRepository;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.model.MouvementStock;
import com.gestion.achat_vente_stock.stock.service.MouvementStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 52-54: Inventaire > Ajustements
 * - Ligne 52: Valider ajustements si écart > seuil (chef magasin)
 * - Ligne 53: Appliquer ajustements au stock et journaliser
 * - Ligne 54: Interdire : même personne comptage + validation ajustement
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AjustementStockService {

    private final AjustementStockRepository ajustementStockRepository;
    private final LigneInventaireRepository ligneInventaireRepository;
    private final InventaireRepository inventaireRepository;
    private final ArticleRepository articleRepository;
    private final DepotRepository depotRepository;
    private final MouvementStockService mouvementStockService;

    // Seuil de validation chef magasin (en valeur absolue)
    private static final BigDecimal SEUIL_VALIDATION = new BigDecimal("1000.00");

    /**
     * Génère un numéro d'ajustement unique
     */
    private String generateNumero() {
        String prefix = "AJS-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long count = ajustementStockRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /**
     * Crée un ajustement à partir d'une ligne d'inventaire
     */
    public AjustementStock creerDepuisLigneInventaire(Long ligneInventaireId, Utilisateur demandeur, String justification) {
        LigneInventaire ligne = ligneInventaireRepository.findById(ligneInventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne inventaire non trouvée: " + ligneInventaireId));

        if (ligne.getEcartQuantite() == null || ligne.getEcartQuantite().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Pas d'écart à ajuster pour cette ligne");
        }

        AjustementStock ajustement = new AjustementStock();
        ajustement.setNumero(generateNumero());
        ajustement.setInventaire(ligne.getInventaire());
        ajustement.setArticle(ligne.getArticle());
        ajustement.setDepot(ligne.getInventaire().getDepot());
        ajustement.setLotNumero(ligne.getLotNumero());
        ajustement.setQuantiteAvant(ligne.getQuantiteTheorique());
        ajustement.setQuantiteApres(ligne.getQuantiteRetenue());
        ajustement.setEcart(ligne.getEcartQuantite());
        ajustement.setValeurAjustement(ligne.getEcartValeur());
        ajustement.setMotif(MotifAjustement.INVENTAIRE);
        ajustement.setJustification(justification);
        ajustement.setDemandeBy(demandeur);
        ajustement.setDemandeAt(LocalDateTime.now());

        // TODO.YML Ligne 52: Valider ajustements si écart > seuil
        if (ligne.getEcartValeur() != null && ligne.getEcartValeur().abs().compareTo(SEUIL_VALIDATION) <= 0) {
            // Petit écart: validation automatique
            ajustement.setStatut(StatutAjustement.VALIDE);
            log.info("Ajustement {} validé automatiquement (écart < seuil)", ajustement.getNumero());
        } else {
            // Gros écart: nécessite validation chef magasin
            ajustement.setStatut(StatutAjustement.EN_ATTENTE);
            log.info("Ajustement {} en attente de validation (écart > seuil)", ajustement.getNumero());
        }

        return ajustementStockRepository.save(ajustement);
    }

    /**
     * Crée un ajustement manuel (hors inventaire)
     */
    public AjustementStock creerManuel(Long articleId, Long depotId, BigDecimal quantiteApres, 
                                       MotifAjustement motif, String justification, 
                                       String lotNumero, Utilisateur demandeur) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article non trouvé: " + articleId));
        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new IllegalArgumentException("Dépôt non trouvé: " + depotId));

        // Récupérer la quantité actuelle
        BigDecimal quantiteAvant = mouvementStockService.getStockDisponible(articleId, depotId);

        AjustementStock ajustement = new AjustementStock();
        ajustement.setNumero(generateNumero());
        ajustement.setArticle(article);
        ajustement.setDepot(depot);
        ajustement.setLotNumero(lotNumero);
        ajustement.setQuantiteAvant(quantiteAvant);
        ajustement.setQuantiteApres(quantiteApres);
        ajustement.setEcart(quantiteApres.subtract(quantiteAvant));
        ajustement.setMotif(motif);
        ajustement.setJustification(justification);
        ajustement.setDemandeBy(demandeur);
        ajustement.setDemandeAt(LocalDateTime.now());
        ajustement.setStatut(StatutAjustement.EN_ATTENTE);

        return ajustementStockRepository.save(ajustement);
    }

    /**
     * TODO.YML Ligne 52: Valider ajustements si écart > seuil (chef magasin)
     * TODO.YML Ligne 54: Interdire : même personne comptage + validation ajustement
     */
    public AjustementStock valider(Long ajustementId, Utilisateur valideur) {
        AjustementStock ajustement = ajustementStockRepository.findById(ajustementId)
                .orElseThrow(() -> new IllegalArgumentException("Ajustement non trouvé: " + ajustementId));

        if (ajustement.getStatut() != StatutAjustement.EN_ATTENTE && 
            ajustement.getStatut() != StatutAjustement.VALIDE) {
            throw new IllegalStateException("L'ajustement ne peut pas être validé dans son état actuel");
        }

        // TODO.YML Ligne 54: Vérifier que le valideur n'est pas le demandeur
        if (ajustement.getDemandeBy() != null && ajustement.getDemandeBy().getId().equals(valideur.getId())) {
            throw new IllegalStateException("Le valideur ne peut pas être la même personne que le demandeur");
        }

        // Si c'est un ajustement d'inventaire, vérifier que le valideur n'est pas un compteur
        if (ajustement.getInventaire() != null) {
            List<LigneInventaire> lignesComptees = ligneInventaireRepository
                    .findByInventaireIdAndArticleId(ajustement.getInventaire().getId(), ajustement.getArticle().getId());
            
            for (LigneInventaire ligne : lignesComptees) {
                if ((ligne.getCompteur1() != null && ligne.getCompteur1().getId().equals(valideur.getId())) ||
                    (ligne.getCompteur2() != null && ligne.getCompteur2().getId().equals(valideur.getId()))) {
                    throw new IllegalStateException("Le valideur ne peut pas être un des compteurs de cette ligne");
                }
            }
        }

        ajustement.setStatut(StatutAjustement.VALIDE);
        ajustement.setValideBy(valideur);
        ajustement.setValideAt(LocalDateTime.now());

        log.info("Ajustement {} validé par {}", ajustement.getNumero(), valideur.getLogin());
        
        return ajustementStockRepository.save(ajustement);
    }

    /**
     * TODO.YML Ligne 53: Appliquer ajustements au stock et journaliser
     */
    public AjustementStock appliquer(Long ajustementId, Utilisateur utilisateur) {
        AjustementStock ajustement = ajustementStockRepository.findById(ajustementId)
                .orElseThrow(() -> new IllegalArgumentException("Ajustement non trouvé: " + ajustementId));

        if (ajustement.getStatut() != StatutAjustement.VALIDE) {
            throw new IllegalStateException("L'ajustement doit être validé avant d'être appliqué");
        }

        // Créer le mouvement de stock correspondant
        BigDecimal ecart = ajustement.getEcart();
        MouvementStock mouvement;

        if (ecart.compareTo(BigDecimal.ZERO) > 0) {
            // Écart positif = entrée
            BigDecimal coutUnitaire = ajustement.getValeurAjustement() != null && ecart.compareTo(BigDecimal.ZERO) != 0
                    ? ajustement.getValeurAjustement().divide(ecart, 4, BigDecimal.ROUND_HALF_UP).abs()
                    : BigDecimal.ZERO;
            
            mouvement = mouvementStockService.creerMouvementEntree(
                    ajustement.getArticle(),
                    ajustement.getDepot(),
                    "ENTREE_AJUSTEMENT",
                    ecart,
                    coutUnitaire,
                    null, // emplacement
                    ajustement.getLotNumero(),
                    null, // dluo
                    ajustement.getId(), // documentId
                    "AJUSTEMENT", // typeDocument
                    utilisateur
            );
        } else {
            // Écart négatif = sortie
            mouvement = mouvementStockService.creerMouvementSortie(
                    ajustement.getArticle(),
                    ajustement.getDepot(),
                    "SORTIE_AJUSTEMENT",
                    ecart.abs(),
                    null, // emplacement
                    ajustement.getLotNumero(),
                    ajustement.getId(), // documentId
                    "AJUSTEMENT", // typeDocument
                    utilisateur
            );
        }

        ajustement.setStatut(StatutAjustement.APPLIQUE);
        ajustement = ajustementStockRepository.save(ajustement);

        log.info("Ajustement {} appliqué: mouvement {} créé", ajustement.getNumero(), mouvement.getNumero());
        
        return ajustement;
    }

    /**
     * Refuse un ajustement
     */
    public AjustementStock refuser(Long ajustementId, Utilisateur valideur, String motifRefus) {
        AjustementStock ajustement = ajustementStockRepository.findById(ajustementId)
                .orElseThrow(() -> new IllegalArgumentException("Ajustement non trouvé: " + ajustementId));

        if (ajustement.getStatut() != StatutAjustement.EN_ATTENTE) {
            throw new IllegalStateException("Seuls les ajustements en attente peuvent être refusés");
        }

        ajustement.setStatut(StatutAjustement.REFUSE);
        ajustement.setValideBy(valideur);
        ajustement.setValideAt(LocalDateTime.now());
        ajustement.setJustification(ajustement.getJustification() + " | REFUS: " + motifRefus);

        log.info("Ajustement {} refusé par {}: {}", ajustement.getNumero(), valideur.getLogin(), motifRefus);
        
        return ajustementStockRepository.save(ajustement);
    }

    /**
     * Génère tous les ajustements pour un inventaire
     */
    public List<AjustementStock> genererAjustementsInventaire(Long inventaireId, Utilisateur demandeur) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        List<LigneInventaire> lignesAvecEcart = ligneInventaireRepository.findLignesAvecEcart(inventaireId);

        return lignesAvecEcart.stream()
                .map(ligne -> creerDepuisLigneInventaire(ligne.getId(), demandeur, "Écart inventaire " + inventaire.getNumero()))
                .toList();
    }

    // ===== Méthodes de consultation =====

    public Optional<AjustementStock> findById(Long id) {
        return ajustementStockRepository.findById(id);
    }

    public Optional<AjustementStock> findByNumero(String numero) {
        return ajustementStockRepository.findByNumero(numero);
    }

    public List<AjustementStock> findAll() {
        return ajustementStockRepository.findAll();
    }

    public List<AjustementStock> findByStatut(StatutAjustement statut) {
        return ajustementStockRepository.findByStatut(statut);
    }

    public List<AjustementStock> findEnAttenteValidation() {
        return ajustementStockRepository.findEnAttenteValidation();
    }

    public List<AjustementStock> findByInventaire(Long inventaireId) {
        return ajustementStockRepository.findByInventaireId(inventaireId);
    }

    public List<AjustementStock> findHistoriqueByArticle(Long articleId) {
        return ajustementStockRepository.findHistoriqueByArticle(articleId);
    }

    public BigDecimal getSeuilValidation() {
        return SEUIL_VALIDATION;
    }
}
