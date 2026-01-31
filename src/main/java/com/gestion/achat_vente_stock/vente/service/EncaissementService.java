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
     * TODO.YML Ligne 29: Enregistrer un encaissement avec tous les détails
     */
    public Encaissement enregistrerEncaissement(Long factureId, BigDecimal montant, 
            String modePaiement, String reference, String banque, LocalDate dateEcheance, Utilisateur utilisateur) {
        
        FactureClient facture = factureClientRepository.findById(factureId)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée: " + factureId));

        // Vérifier que la facture est validée
        if (!"VALIDEE".equals(facture.getStatut()) && !"PAYEE_PARTIELLEMENT".equals(facture.getStatut()) && !"ENVOYEE".equals(facture.getStatut())) {
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
        encaissement.setBanque(banque);
        encaissement.setDateEcheance(dateEcheance);
        encaissement.setStatut("EN_ATTENTE");

        Encaissement saved = encaissementRepository.save(encaissement);

        auditService.logAction(utilisateur, "encaissement", saved.getId(),
                "CREATE", null, "Encaissement de " + montant + " € pour facture " + facture.getNumero(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 29: Enregistrer un encaissement (surcharge simple)
     */
    public Encaissement enregistrerEncaissement(Long factureId, BigDecimal montant, 
            String modePaiement, String reference, Utilisateur utilisateur) {
        return enregistrerEncaissement(factureId, montant, modePaiement, reference, null, null, utilisateur);
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

    /**
     * Récupérer les encaissements par date
     */
    public List<Encaissement> listerParDate(LocalDate date) {
        return encaissementRepository.findByDateEncaissement(date);
    }

    /**
     * Récupérer les encaissements par période
     */
    public List<Encaissement> listerParPeriode(LocalDate debut, LocalDate fin) {
        return encaissementRepository.findByDateEncaissementBetween(debut, fin);
    }

    // ==================== WORKFLOW ====================

    /**
     * Valider un encaissement
     */
    public void validerEncaissement(Long encaissementId, Utilisateur utilisateur) {
        Encaissement encaissement = obtenirParId(encaissementId);

        if (!"EN_ATTENTE".equals(encaissement.getStatut())) {
            throw new IllegalStateException("Seuls les encaissements en attente peuvent être validés");
        }

        encaissement.setStatut("VALIDE");
        encaissementRepository.save(encaissement);

        auditService.logAction(utilisateur, "encaissement", encaissementId,
                "VALIDATE", "EN_ATTENTE", "VALIDE", null);
    }

    /**
     * Rejeter un encaissement (surcharge simple, sans utilisateur)
     */
    public void rejeterEncaissement(Long encaissementId, String motif) {
        rejeterEncaissement(encaissementId, null, motif);
    }

    /**
     * Rejeter un encaissement (chèque impayé par exemple)
     */
    public void rejeterEncaissement(Long encaissementId, Utilisateur utilisateur, String motif) {
        Encaissement encaissement = obtenirParId(encaissementId);

        if (!"VALIDE".equals(encaissement.getStatut()) && !"EN_ATTENTE".equals(encaissement.getStatut())) {
            throw new IllegalStateException("Seuls les encaissements validés ou en attente peuvent être rejetés");
        }

        encaissement.setStatut("REJETE");
        encaissement.setCommentaire(motif);
        encaissementRepository.save(encaissement);

        // Recalculer le statut de la facture
        FactureClient facture = encaissement.getFactureClient();
        BigDecimal totalEncaisse = encaissementRepository.calculerTotalEncaisseParFacture(facture.getId());
        
        if (totalEncaisse.compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatut("VALIDEE");
            facture.setEstPayee(false);
        } else if (totalEncaisse.compareTo(facture.getMontantTtc()) < 0) {
            facture.setStatut("PAYEE_PARTIELLEMENT");
        }
        factureClientRepository.save(facture);

        if (utilisateur != null) {
            auditService.logAction(utilisateur, "encaissement", encaissementId,
                    "REJECT", "VALIDE", "REJETE", motif);
        }
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
