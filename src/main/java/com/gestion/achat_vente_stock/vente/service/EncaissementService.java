package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.vente.model.Encaissement;
import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.repository.EncaissementRepository;
import com.gestion.achat_vente_stock.vente.repository.FactureClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Ligne 29: Ventes > Encaissement
 * Enregistrer encaissements clients
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EncaissementService {

    private final EncaissementRepository encaissementRepository;
    private final FactureClientRepository factureClientRepository;
    private final FactureClientService factureClientService;
    private final AuditService auditService;

    /** Préfixe pour les numéros d'encaissement */
    private static final String PREFIXE_NUMERO = "ENC";

    // ==================== CRUD ====================

    /**
     * TODO.YML Ligne 29: Enregistrer un encaissement
     */
    public Encaissement enregistrerEncaissement(Long factureId, BigDecimal montant, 
            String modePaiement, String reference, Utilisateur utilisateur) {
        
        FactureClient facture = factureClientRepository.findById(factureId)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée: " + factureId));

        // Vérifier que la facture est validée
        if (!"VALIDEE".equals(facture.getStatut()) && !"PAYEE_PARTIELLEMENT".equals(facture.getStatut())) {
            throw new IllegalStateException("La facture doit être validée pour enregistrer un encaissement");
        }

        // Vérifier que le montant ne dépasse pas le solde restant
        BigDecimal totalEncaisse = encaissementRepository.calculerTotalEncaisseParFacture(factureId);
        BigDecimal soldeRestant = facture.getMontantTtc().subtract(totalEncaisse);
        
        if (montant.compareTo(soldeRestant) > 0) {
            throw new IllegalStateException("Le montant encaissé (" + montant 
                    + ") dépasse le solde restant (" + soldeRestant + ")");
        }

        // Créer l'encaissement
        Encaissement encaissement = new Encaissement();
        encaissement.setNumero(genererNumero());
        encaissement.setFactureClient(facture);
        encaissement.setDateEncaissement(LocalDate.now());
        encaissement.setMontantEncaisse(montant);
        encaissement.setModePaiement(modePaiement);
        encaissement.setReference(reference);
        encaissement.setStatut("VALIDE");

        Encaissement saved = encaissementRepository.save(encaissement);

        // Mettre à jour le statut de la facture
        BigDecimal nouveauTotal = totalEncaisse.add(montant);
        if (nouveauTotal.compareTo(facture.getMontantTtc()) >= 0) {
            factureClientService.marquerPayee(factureId, utilisateur);
        } else {
            factureClientService.marquerPaiementPartiel(factureId, nouveauTotal);
        }

        auditService.logAction(utilisateur, "encaissement", saved.getId(),
                "CREATE", null, "Encaissement de " + montant + " € pour facture " + facture.getNumero(), null);

        return saved;
    }

    /**
     * Récupérer tous les encaissements
     */
    public List<Encaissement> listerTous() {
        return encaissementRepository.findAll();
    }

    /**
     * Récupérer un encaissement par ID
     */
    public Encaissement obtenirParId(Long id) {
        return encaissementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encaissement non trouvé: " + id));
    }

    /**
     * Récupérer les encaissements par facture
     */
    public List<Encaissement> listerParFacture(Long factureId) {
        return encaissementRepository.findByFactureClientId(factureId);
    }

    /**
     * Récupérer les encaissements par mode de paiement
     */
    public List<Encaissement> listerParModePaiement(String modePaiement) {
        return encaissementRepository.findByModePaiement(modePaiement);
    }

    // ==================== WORKFLOW ====================

    /**
     * Rejeter un encaissement (chèque impayé par exemple)
     */
    public void rejeterEncaissement(Long encaissementId, Utilisateur utilisateur, String motif) {
        Encaissement encaissement = obtenirParId(encaissementId);

        if (!"VALIDE".equals(encaissement.getStatut())) {
            throw new IllegalStateException("Seuls les encaissements validés peuvent être rejetés");
        }

        encaissement.setStatut("REJETE");
        encaissementRepository.save(encaissement);

        // Recalculer le statut de la facture
        FactureClient facture = encaissement.getFactureClient();
        BigDecimal totalEncaisse = encaissementRepository.calculerTotalEncaisseParFacture(facture.getId());
        
        if (totalEncaisse.compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatut("VALIDEE");
        } else if (totalEncaisse.compareTo(facture.getMontantTtc()) < 0) {
            facture.setStatut("PAYEE_PARTIELLEMENT");
        }
        factureClientRepository.save(facture);

        auditService.logAction(utilisateur, "encaissement", encaissementId,
                "REJECT", "VALIDE", "REJETE", motif);
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique d'encaissement
     */
    private String genererNumero() {
        long count = encaissementRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }

    /**
     * Calculer le total encaissé pour une facture
     */
    public BigDecimal calculerTotalEncaisse(Long factureId) {
        return encaissementRepository.calculerTotalEncaisseParFacture(factureId);
    }
}
