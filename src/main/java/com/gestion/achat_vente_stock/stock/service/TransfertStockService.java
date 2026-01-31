package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.model.LigneTransfert;
import com.gestion.achat_vente_stock.stock.model.TransfertStock;
import com.gestion.achat_vente_stock.stock.repository.LigneTransfertRepository;
import com.gestion.achat_vente_stock.stock.repository.TransfertStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 34-35: Stock > Transferts
 * - Ligne 34: Gérer transferts entre dépôts/sites
 * - Ligne 35: Validation transferts par chef magasin
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransfertStockService {

    private final TransfertStockRepository transfertStockRepository;
    private final LigneTransfertRepository ligneTransfertRepository;
    private final MouvementStockService mouvementStockService;
    private final DepotRepository depotRepository;
    private final ArticleRepository articleRepository;
    private final AuditService auditService;

    // Statuts de transfert
    public static final String STATUT_BROUILLON = "BROUILLON";
    public static final String STATUT_DEMANDE = "DEMANDE";
    public static final String STATUT_VALIDEE = "VALIDEE";
    public static final String STATUT_EN_TRANSIT = "EN_TRANSIT";
    public static final String STATUT_RECUE = "RECUE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    /**
     * TODO.YML Ligne 34: Créer un transfert inter-dépôts
     */
    public TransfertStock creerTransfert(Long depotSourceId, Long depotDestinationId, 
                                          Utilisateur demandeur) {
        Depot depotSource = depotRepository.findById(depotSourceId)
                .orElseThrow(() -> new RuntimeException("Dépôt source non trouvé"));
        Depot depotDestination = depotRepository.findById(depotDestinationId)
                .orElseThrow(() -> new RuntimeException("Dépôt destination non trouvé"));

        if (depotSourceId.equals(depotDestinationId)) {
            throw new RuntimeException("Le dépôt source et destination doivent être différents");
        }

        TransfertStock transfert = new TransfertStock();
        transfert.setNumero(genererNumeroTransfert());
        transfert.setDepotSource(depotSource);
        transfert.setDepotDestination(depotDestination);
        transfert.setDemandeur(demandeur);
        transfert.setDateDemande(LocalDate.now());
        transfert.setStatut(STATUT_BROUILLON);

        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(demandeur, "transfert_stock", saved.getId(),
                "CREATE", null, saved.toString(), null);

        return saved;
    }

    /**
     * Ajouter une ligne au transfert
     */
    public LigneTransfert ajouterLigne(Long transfertId, Long articleId, 
                                        BigDecimal quantiteDemandee, String lotNumero,
                                        Utilisateur utilisateur) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (!STATUT_BROUILLON.equals(transfert.getStatut())) {
            throw new RuntimeException("Impossible d'ajouter des lignes à un transfert déjà soumis");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        // Vérifier le stock disponible au dépôt source
        BigDecimal stockDispo = mouvementStockService.getQuantiteDisponible(
                articleId, transfert.getDepotSource().getId(), lotNumero);
        if (stockDispo.compareTo(quantiteDemandee) < 0) {
            throw new RuntimeException("Stock insuffisant au dépôt source. Disponible: " + stockDispo);
        }

        LigneTransfert ligne = new LigneTransfert();
        ligne.setTransfert(transfert);
        ligne.setArticle(article);
        ligne.setQuantiteDemandee(quantiteDemandee);
        ligne.setLotNumero(lotNumero);

        LigneTransfert saved = ligneTransfertRepository.save(ligne);

        // Audit
        auditService.logAction(utilisateur, "ligne_transfert", saved.getId(),
                "ADD", null, saved.toString(), null);

        return saved;
    }

    /**
     * Soumettre la demande de transfert
     */
    public TransfertStock soumettreTransfert(Long transfertId, Utilisateur utilisateur) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (!STATUT_BROUILLON.equals(transfert.getStatut())) {
            throw new RuntimeException("Ce transfert ne peut pas être soumis");
        }

        if (transfert.getLignes().isEmpty()) {
            throw new RuntimeException("Le transfert doit avoir au moins une ligne");
        }

        transfert.setStatut(STATUT_DEMANDE);
        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(utilisateur, "transfert_stock", saved.getId(),
                "SOUMETTRE", STATUT_BROUILLON, STATUT_DEMANDE, null);

        return saved;
    }

    /**
     * TODO.YML Ligne 35: Validation transferts par chef magasin
     */
    public TransfertStock validerTransfert(Long transfertId, Utilisateur chefMagasin) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (!STATUT_DEMANDE.equals(transfert.getStatut())) {
            throw new RuntimeException("Ce transfert ne peut pas être validé");
        }

        // TODO: Vérifier que l'utilisateur a le rôle CHEF_MAGASIN
        // TODO: Vérifier que le chef magasin est responsable du dépôt source

        transfert.setStatut(STATUT_VALIDEE);
        transfert.setValideBy(chefMagasin);
        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(chefMagasin, "transfert_stock", saved.getId(),
                "VALIDER", STATUT_DEMANDE, STATUT_VALIDEE, null);

        return saved;
    }

    /**
     * Expédier le transfert (créer mouvements de sortie au dépôt source)
     */
    public TransfertStock expedierTransfert(Long transfertId, Utilisateur magasinier) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (!STATUT_VALIDEE.equals(transfert.getStatut())) {
            throw new RuntimeException("Ce transfert doit être validé avant expédition");
        }

        // Créer les mouvements de sortie pour chaque ligne
        for (LigneTransfert ligne : transfert.getLignes()) {
            // Créer mouvement de sortie
            mouvementStockService.creerMouvementSortie(
                    ligne.getArticle(),
                    transfert.getDepotSource(),
                    MouvementStockService.SORTIE_TRANSFERT,
                    ligne.getQuantiteDemandee(),
                    null, // emplacement
                    ligne.getLotNumero(),
                    transfert.getId(),
                    "TRANSFERT",
                    magasinier
            );

            // Marquer comme expédié
            ligne.setQuantiteExpedie(ligne.getQuantiteDemandee());
            ligneTransfertRepository.save(ligne);
        }

        transfert.setStatut(STATUT_EN_TRANSIT);
        transfert.setDateExpedition(LocalDate.now());
        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(magasinier, "transfert_stock", saved.getId(),
                "EXPEDIER", STATUT_VALIDEE, STATUT_EN_TRANSIT, null);

        return saved;
    }

    /**
     * Réceptionner le transfert (créer mouvements d'entrée au dépôt destination)
     */
    public TransfertStock receptionnerTransfert(Long transfertId, Utilisateur magasinier) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (!STATUT_EN_TRANSIT.equals(transfert.getStatut())) {
            throw new RuntimeException("Ce transfert n'est pas en transit");
        }

        // Créer les mouvements d'entrée pour chaque ligne
        for (LigneTransfert ligne : transfert.getLignes()) {
            BigDecimal quantiteRecue = ligne.getQuantiteExpedie(); // Par défaut, reçu = expédié
            
            // Créer mouvement d'entrée
            mouvementStockService.creerMouvementEntree(
                    ligne.getArticle(),
                    transfert.getDepotDestination(),
                    MouvementStockService.ENTREE_TRANSFERT,
                    quantiteRecue,
                    BigDecimal.ZERO, // Coût à zéro pour transfert interne
                    null, // emplacement
                    ligne.getLotNumero(),
                    null, // dluo
                    transfert.getId(),
                    "TRANSFERT",
                    magasinier
            );

            // Marquer comme reçu
            ligne.setQuantiteRecue(quantiteRecue);
            ligneTransfertRepository.save(ligne);
        }

        transfert.setStatut(STATUT_RECUE);
        transfert.setDateReception(LocalDate.now());
        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(magasinier, "transfert_stock", saved.getId(),
                "RECEPTIONNER", STATUT_EN_TRANSIT, STATUT_RECUE, null);

        return saved;
    }

    /**
     * Réceptionner une ligne avec une quantité différente (écart)
     */
    public LigneTransfert receptionnerLigneAvecEcart(Long ligneId, BigDecimal quantiteRecue,
                                                      Utilisateur magasinier) {
        LigneTransfert ligne = ligneTransfertRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée"));

        TransfertStock transfert = ligne.getTransfert();
        if (!STATUT_EN_TRANSIT.equals(transfert.getStatut())) {
            throw new RuntimeException("Ce transfert n'est pas en transit");
        }

        ligne.setQuantiteRecue(quantiteRecue);
        LigneTransfert saved = ligneTransfertRepository.save(ligne);

        // Audit avec écart
        String details = "Expédié: " + ligne.getQuantiteExpedie() + ", Reçu: " + quantiteRecue + 
                        ", Écart: " + ligne.getEcart();
        auditService.logAction(magasinier, "ligne_transfert", saved.getId(),
                "RECEPTION_ECART", ligne.getQuantiteExpedie().toString(), details, null);

        return saved;
    }

    /**
     * Annuler un transfert
     */
    public TransfertStock annulerTransfert(Long transfertId, Utilisateur utilisateur) {
        TransfertStock transfert = trouverParId(transfertId);
        
        if (STATUT_EN_TRANSIT.equals(transfert.getStatut()) || STATUT_RECUE.equals(transfert.getStatut())) {
            throw new RuntimeException("Impossible d'annuler un transfert en transit ou reçu");
        }

        String ancienStatut = transfert.getStatut();
        transfert.setStatut(STATUT_ANNULEE);
        TransfertStock saved = transfertStockRepository.save(transfert);

        // Audit
        auditService.logAction(utilisateur, "transfert_stock", saved.getId(),
                "ANNULER", ancienStatut, STATUT_ANNULEE, null);

        return saved;
    }

    /**
     * Générer un numéro de transfert unique
     */
    private String genererNumeroTransfert() {
        String prefix = "TRF-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Optional<String> lastNumero = transfertStockRepository.findLastNumeroByPrefix(prefix);
        
        int sequence = 1;
        if (lastNumero.isPresent()) {
            String last = lastNumero.get();
            String seqStr = last.substring(last.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(seqStr) + 1;
        }
        
        return prefix + String.format("%04d", sequence);
    }

    @Transactional(readOnly = true)
    public TransfertStock trouverParId(Long id) {
        return transfertStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfert non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<TransfertStock> listerTous() {
        return transfertStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransfertStock> listerParStatut(String statut) {
        return transfertStockRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<TransfertStock> listerTransfertsAValider(Long depotId) {
        return transfertStockRepository.findTransfertsAValider(depotId);
    }

    @Transactional(readOnly = true)
    public List<TransfertStock> listerTransfertsEnTransitVers(Long depotId) {
        return transfertStockRepository.findTransfertsEnTransitVers(depotId);
    }

    @Transactional(readOnly = true)
    public List<LigneTransfert> getLignesTransfert(Long transfertId) {
        return ligneTransfertRepository.findByTransfertId(transfertId);
    }

    @Transactional(readOnly = true)
    public List<LigneTransfert> getLignesAvecEcarts(Long transfertId) {
        return ligneTransfertRepository.findLignesAvecEcarts(transfertId);
    }
}
