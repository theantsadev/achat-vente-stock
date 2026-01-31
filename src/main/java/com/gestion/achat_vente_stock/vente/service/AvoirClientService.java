package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.vente.model.AvoirClient;
import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.repository.AvoirClientRepository;
import com.gestion.achat_vente_stock.vente.repository.FactureClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Ligne 30: Ventes > Avoirs
 * Créer avoirs (retour, erreur prix, casse) avec validation
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AvoirClientService {

    private final AvoirClientRepository avoirClientRepository;
    private final FactureClientRepository factureClientRepository;
    private final AuditService auditService;

    /** Préfixe pour les numéros d'avoir */
    private static final String PREFIXE_NUMERO = "AV";
    
    /** Taux TVA par défaut */
    private static final BigDecimal TAUX_TVA = new BigDecimal("0.20");

    // ==================== CRUD ====================

    /**
     * TODO.YML Ligne 30: Créer un avoir sur une facture
     */
    public AvoirClient creerAvoir(Long factureId, BigDecimal montantHt, String motif, Utilisateur utilisateur) {
        FactureClient facture = factureClientRepository.findById(factureId)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée: " + factureId));

        // Vérifier que la facture est validée ou payée
        if (!"VALIDEE".equals(facture.getStatut()) 
                && !"PAYEE".equals(facture.getStatut()) 
                && !"PAYEE_PARTIELLEMENT".equals(facture.getStatut())) {
            throw new IllegalStateException("La facture doit être validée pour créer un avoir");
        }

        // Vérifier que le montant ne dépasse pas le montant de la facture
        if (montantHt.compareTo(facture.getMontantHt()) > 0) {
            throw new IllegalStateException("Le montant de l'avoir ne peut pas dépasser le montant de la facture");
        }

        // Créer l'avoir
        AvoirClient avoir = new AvoirClient();
        avoir.setNumero(genererNumero());
        avoir.setFactureClient(facture);
        avoir.setClient(facture.getClient());
        avoir.setDateAvoir(LocalDate.now());
        avoir.setMontantHt(montantHt);
        avoir.setMontantTva(montantHt.multiply(TAUX_TVA).setScale(4, RoundingMode.HALF_UP));
        avoir.setMontantTtc(avoir.getMontantHt().add(avoir.getMontantTva()));
        avoir.setMotif(motif);
        avoir.setStatut("EN_ATTENTE_VALIDATION");

        AvoirClient saved = avoirClientRepository.save(avoir);

        auditService.logAction(utilisateur, "avoir_client", saved.getId(),
                "CREATE", null, "Avoir créé pour facture " + facture.getNumero() + " - Motif: " + motif, null);

        return saved;
    }

    /**
     * Créer un avoir sans facture (geste commercial)
     */
    public AvoirClient creerAvoirSansFacture(Long clientId, BigDecimal montantHt, String motif, Utilisateur utilisateur) {
        AvoirClient avoir = new AvoirClient();
        avoir.setNumero(genererNumero());
        // avoir.setClient(...) - À récupérer depuis le repository client
        avoir.setDateAvoir(LocalDate.now());
        avoir.setMontantHt(montantHt);
        avoir.setMontantTva(montantHt.multiply(TAUX_TVA).setScale(4, RoundingMode.HALF_UP));
        avoir.setMontantTtc(avoir.getMontantHt().add(avoir.getMontantTva()));
        avoir.setMotif(motif);
        avoir.setStatut("EN_ATTENTE_VALIDATION");

        AvoirClient saved = avoirClientRepository.save(avoir);

        auditService.logAction(utilisateur, "avoir_client", saved.getId(),
                "CREATE", null, "Avoir commercial créé - Motif: " + motif, null);

        return saved;
    }

    /**
     * Récupérer tous les avoirs
     */
    public List<AvoirClient> listerTous() {
        return avoirClientRepository.findAll();
    }

    /**
     * Récupérer un avoir par ID
     */
    public AvoirClient obtenirParId(Long id) {
        return avoirClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avoir non trouvé: " + id));
    }

    /**
     * Récupérer les avoirs par statut
     */
    public List<AvoirClient> listerParStatut(String statut) {
        return avoirClientRepository.findByStatut(statut);
    }

    /**
     * Récupérer les avoirs par client
     */
    public List<AvoirClient> listerParClient(Long clientId) {
        return avoirClientRepository.findByClientId(clientId);
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 30: Valider un avoir (double validation)
     */
    public void validerAvoir(Long avoirId, Utilisateur valideur, boolean approuve, String commentaire) {
        AvoirClient avoir = obtenirParId(avoirId);

        if (!"EN_ATTENTE_VALIDATION".equals(avoir.getStatut())) {
            throw new IllegalStateException("Cet avoir n'est pas en attente de validation");
        }

        String ancienStatut = avoir.getStatut();
        if (approuve) {
            avoir.setStatut("VALIDE");
            avoir.setValideBy(valideur);
        } else {
            avoir.setStatut("ANNULE");
        }

        avoirClientRepository.save(avoir);

        auditService.logAction(valideur, "avoir_client", avoirId,
                approuve ? "APPROVE" : "REJECT", ancienStatut, avoir.getStatut(), commentaire);
    }

    /**
     * Appliquer l'avoir (déduire du compte client)
     */
    public void appliquerAvoir(Long avoirId, Utilisateur utilisateur) {
        AvoirClient avoir = obtenirParId(avoirId);

        if (!"VALIDE".equals(avoir.getStatut())) {
            throw new IllegalStateException("Seuls les avoirs validés peuvent être appliqués");
        }

        avoir.setStatut("APPLIQUE");
        avoirClientRepository.save(avoir);

        auditService.logAction(utilisateur, "avoir_client", avoirId,
                "APPLY", "VALIDE", "APPLIQUE", null);
    }

    /**
     * Annuler un avoir
     */
    public void annulerAvoir(Long avoirId, Utilisateur utilisateur, String motif) {
        AvoirClient avoir = obtenirParId(avoirId);

        if ("APPLIQUE".equals(avoir.getStatut())) {
            throw new IllegalStateException("Les avoirs appliqués ne peuvent pas être annulés");
        }

        String ancienStatut = avoir.getStatut();
        avoir.setStatut("ANNULE");
        avoirClientRepository.save(avoir);

        auditService.logAction(utilisateur, "avoir_client", avoirId,
                "CANCEL", ancienStatut, "ANNULE", motif);
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique d'avoir
     */
    private String genererNumero() {
        long count = avoirClientRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }
}
