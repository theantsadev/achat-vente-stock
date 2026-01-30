package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.FactureFournisseur;
import com.gestion.achat_vente_stock.achat.model.PaiementFournisseur;
import com.gestion.achat_vente_stock.achat.repository.PaiementFournisseurRepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO.YML Ligne 19: Achats > Paiement
 * Enregistrer paiements fournisseurs
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaiementFournisseurService {

    private final PaiementFournisseurRepository paiementFournisseurRepository;
    private final FactureFournisseurService factureFournisseurService;
    private final AuditService auditService;

    /**
     * TODO.YML Ligne 19: Créer un paiement fournisseur
     * TODO.YML Ligne 18: Vérifie que la facture n'est pas bloquée
     */
    public PaiementFournisseur creerPaiement(PaiementFournisseur paiement, Utilisateur tresorier) {
        FactureFournisseur facture = factureFournisseurService.trouverParId(
                paiement.getFactureFournisseur().getId());

        // TODO.YML Ligne 18: Bloquer paiement si facture bloquée
        if ("BLOQUEE".equals(facture.getStatut())) {
            throw new RuntimeException("Impossible de payer: Facture bloquée avec écarts non résolus");
        }

        if (!"VALIDEE".equals(facture.getStatut())) {
            throw new RuntimeException("La facture doit être validée avant paiement");
        }

        paiement.setNumero(genererNumeroPaiement());
        paiement.setStatut("EN_ATTENTE");

        PaiementFournisseur saved = paiementFournisseurRepository.save(paiement);

        // Audit
        auditService.logAction(tresorier, "paiement_fournisseur", saved.getId(),
                "CREATE", null, saved.toString(), null);

        return saved;
    }

    /**
     * Exécuter le paiement
     */
    public void executerPaiement(Long paiementId, Utilisateur tresorier) {
        PaiementFournisseur paiement = trouverParId(paiementId);

        if (!"EN_ATTENTE".equals(paiement.getStatut())) {
            throw new RuntimeException("Ce paiement n'est pas en attente");
        }

        paiement.setStatut("EXECUTE");
        paiementFournisseurRepository.save(paiement);

        // Mettre à jour le statut de la facture
        FactureFournisseur facture = paiement.getFactureFournisseur();
        facture.setStatut("PAYEE");
        factureFournisseurService.getFactureFournisseurRepository().save(facture);

        // Audit
        auditService.logAction(tresorier, "paiement_fournisseur", paiementId,
                "EXECUTE", "EN_ATTENTE", "EXECUTE", null);
    }

    /**
     * Annuler un paiement
     */
    public void annulerPaiement(Long paiementId, String motif, Utilisateur tresorier) {
        PaiementFournisseur paiement = trouverParId(paiementId);

        if ("EXECUTE".equals(paiement.getStatut())) {
            throw new RuntimeException("Un paiement exécuté ne peut pas être annulé");
        }

        paiement.setStatut("ANNULE");
        paiementFournisseurRepository.save(paiement);

        // Audit
        auditService.logAction(tresorier, "paiement_fournisseur", paiementId,
                "CANCEL", paiement.getStatut(), "ANNULE", motif);
    }

    @Transactional(readOnly = true)
    public PaiementFournisseur trouverParId(Long id) {
        return paiementFournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<PaiementFournisseur> listerTous() {
        return paiementFournisseurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PaiementFournisseur> listerParStatut(String statut) {
        return paiementFournisseurRepository.findByStatut(statut);
    }

    private String genererNumeroPaiement() {
        return "PAY" + System.currentTimeMillis();
    }
}
