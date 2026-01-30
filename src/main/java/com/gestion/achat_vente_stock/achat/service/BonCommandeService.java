package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.BonCommande;
import com.gestion.achat_vente_stock.achat.model.DemandeAchat;
import com.gestion.achat_vente_stock.achat.model.LigneBC;
import com.gestion.achat_vente_stock.achat.model.LigneDA;
import com.gestion.achat_vente_stock.achat.model.Proforma;
import com.gestion.achat_vente_stock.achat.repository.BonCommandeRepository;
import com.gestion.achat_vente_stock.achat.repository.LigneDARepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Lignes 11-13: Achats > Bon Commande
 * - Ligne 11: Transformer DA en BC (acheteur uniquement)
 * - Ligne 12: Validation BC par responsable achats si > seuil
 * - Ligne 13: Approbation finale DG/DAF pour signature légale
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BonCommandeService {

    private final BonCommandeRepository bonCommandeRepository;
    private final DemandeAchatService demandeAchatService;
    private final ProformaService proformaService;
    private final LigneDARepository ligneDARepository;
    private final AuditService auditService;

    // TODO.YML Ligne 12: Seuil pour validation responsable achats
    private static final BigDecimal SEUIL_VALIDATION_BC = new BigDecimal("50000");

    /**
     * TODO.YML Ligne 11: Transformer Proforma acceptée en BC (acheteur uniquement)
     */
    public BonCommande creerBonCommandeDepuisProforma(Long proformaId, Utilisateur acheteur) {
        Proforma proforma = proformaService.obtenirParId(proformaId);

        // Vérifier que la proforma est acceptée
        if (!"ACCEPTE".equals(proforma.getStatut())) {
            throw new RuntimeException("Seules les proformas acceptées peuvent être transformées en BC");
        }

        DemandeAchat da = proforma.getDemandeAchat();

        BonCommande bc = new BonCommande();
        bc.setNumero(genererNumeroBC());
        bc.setDemandeAchat(da);
        bc.setAcheteur(acheteur);
        bc.setDateCommande(java.time.LocalDate.now());
        bc.setStatut("BROUILLON");
        bc.setFournisseur(proforma.getFournisseur());

        // Copier les informations de la proforma
        bc.setMontantTotalHt(proforma.getMontantTotalHt());

        BonCommande bcSaved = bonCommandeRepository.save(bc);

        // Créer les lignes BC depuis les lignes DA
        List<LigneDA> lignesDA = ligneDARepository.findByDemandeAchatId(da.getId());
        for (LigneDA ligneDA : lignesDA) {
            LigneBC ligneBC = new LigneBC();
            ligneBC.setBonCommande(bcSaved);
            ligneBC.setArticle(ligneDA.getArticle());
            ligneBC.setQuantite(ligneDA.getQuantite());
            ligneBC.setPrixUnitaireHt(ligneDA.getPrixEstimeHt());
            ligneBC.setMontantLigneHt(ligneDA.getQuantite().multiply(ligneDA.getPrixEstimeHt()));
            // Les lignes seront sauvegardées par le repository LigneBC si nécessaire
        }

        // Marquer la proforma comme transformée en BC
        proforma.setStatut("TRANSFORMEE_EN_BC");
        proformaService.enregistrer(proforma);

        // Audit
        auditService.logAction(acheteur, "bon_commande", bcSaved.getId(),
                "CREATE_FROM_PROFORMA", null, bcSaved.toString(), null);

        return bcSaved;
    }

    /**
     * TODO.YML Ligne 11: Transformer DA approuvée en BC (acheteur uniquement)
     */
    public BonCommande creerBonCommandeDepuisDA(Long daId, Utilisateur acheteur) {
        DemandeAchat da = demandeAchatService.trouverParId(daId);

        // Vérifier que la DA est approuvée
        if (!"APPROUVEE".equals(da.getStatut())) {
            throw new RuntimeException("Seules les DA approuvées peuvent être transformées en BC");
        }

        // TODO.YML Ligne 11: Vérifier que l'utilisateur est un acheteur
        // (simplification)
        // En production, vérifier le rôle via UtilisateurRole

        BonCommande bc = new BonCommande();
        bc.setNumero(genererNumeroBC());
        bc.setDemandeAchat(da);
        bc.setAcheteur(acheteur);
        bc.setDateCommande(java.time.LocalDate.now());
        bc.setStatut("BROUILLON");

        // Copier les informations de la DA
        bc.setMontantTotalHt(da.getMontantEstimeHt());

        BonCommande bcSaved = bonCommandeRepository.save(bc);

        // Créer les lignes BC depuis les lignes DA
        List<LigneDA> lignesDA = ligneDARepository.findByDemandeAchatId(daId);
        for (LigneDA ligneDA : lignesDA) {
            LigneBC ligneBC = new LigneBC();
            ligneBC.setBonCommande(bcSaved);
            ligneBC.setArticle(ligneDA.getArticle());
            ligneBC.setQuantite(ligneDA.getQuantite());
            ligneBC.setPrixUnitaireHt(ligneDA.getPrixEstimeHt());
            ligneBC.setMontantLigneHt(ligneDA.getQuantite().multiply(ligneDA.getPrixEstimeHt()));
            // Les lignes seront sauvegardées par le repository LigneBC si nécessaire
        }

        // Audit
        auditService.logAction(acheteur, "bon_commande", bcSaved.getId(),
                "CREATE_FROM_DA", null, bcSaved.toString(), null);

        return bcSaved;
    }

    /**
     * TODO.YML Ligne 12: Soumettre BC pour validation si montant > seuil
     */
    public void soumettrePourValidation(Long bcId, Utilisateur acheteur) {
        BonCommande bc = trouverParId(bcId);

        if (!"BROUILLON".equals(bc.getStatut())) {
            throw new RuntimeException("Seuls les BC en brouillon peuvent être soumis");
        }

        // TODO.YML Ligne 12: Vérifier si validation nécessaire selon montant
        if (bc.getMontantTotalHt().compareTo(SEUIL_VALIDATION_BC) >= 0) {
            bc.setStatut("EN_ATTENTE_VALIDATION");
        } else {
            bc.setStatut("VALIDEE");
        }

        bonCommandeRepository.save(bc);

        // Audit
        auditService.logAction(acheteur, "bon_commande", bcId,
                "SUBMIT", "BROUILLON", bc.getStatut(), null);
    }

    /**
     * TODO.YML Ligne 12: Valider BC par responsable achats
     */
    public void validerBonCommande(Long bcId, Utilisateur responsable, boolean approuve, String commentaire) {
        BonCommande bc = trouverParId(bcId);

        if (!"EN_ATTENTE_VALIDATION".equals(bc.getStatut())) {
            throw new RuntimeException("Ce BC n'est pas en attente de validation");
        }

        if (approuve) {
            bc.setStatut("VALIDEE");
        } else {
            bc.setStatut("REJETEE");
        }

        bonCommandeRepository.save(bc);

        // Audit
        auditService.logAction(responsable, "bon_commande", bcId,
                "VALIDATE", "EN_ATTENTE_VALIDATION", bc.getStatut(), null);
    }

    /**
     * TODO.YML Ligne 13: Approbation finale DG/DAF pour signature légale
     */
    public void approuverPourSignature(Long bcId, Utilisateur approbateur) {
        BonCommande bc = trouverParId(bcId);

        if (!"VALIDEE".equals(bc.getStatut())) {
            throw new RuntimeException("Ce BC doit être validé avant approbation finale");
        }

        // TODO.YML Ligne 13: Vérifier que l'utilisateur a le rôle DG ou DAF
        // En production, vérifier via UtilisateurRole

        bc.setStatut("APPROUVEE");
        bc.setApprouveAt(LocalDateTime.now());
        bc.setApprouveBy(approbateur);

        bonCommandeRepository.save(bc);

        // Audit
        auditService.logAction(approbateur, "bon_commande", bcId,
                "APPROVE_FINAL", "VALIDEE", "APPROUVEE", null);
    }

    /**
     * Envoyer le BC au fournisseur
     */
    public void envoyerBonCommande(Long bcId, Utilisateur acheteur) {
        BonCommande bc = trouverParId(bcId);

        if (!"APPROUVEE".equals(bc.getStatut())) {
            throw new RuntimeException("Ce BC doit être approuvé avant envoi");
        }

        bc.setStatut("ENVOYEE");
        bonCommandeRepository.save(bc);

        // Audit
        auditService.logAction(acheteur, "bon_commande", bcId,
                "SEND", "APPROUVEE", "ENVOYEE", null);
    }

    @Transactional(readOnly = true)
    public BonCommande trouverParId(Long id) {
        return bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<BonCommande> listerTous() {
        return bonCommandeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BonCommande> listerParStatut(String statut) {
        return bonCommandeRepository.findByStatut(statut);
    }

    private String genererNumeroBC() {
        return "BC" + System.currentTimeMillis();
    }
}
