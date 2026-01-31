package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.vente.model.*;
import com.gestion.achat_vente_stock.vente.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 27-28: Ventes > Facture Client
 * - Ligne 27: Générer facture client depuis livraison
 * - Ligne 28: Contrôler TVA et conformité (Comptable)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FactureClientService {

    private final FactureClientRepository factureClientRepository;
    private final LigneFactureClientRepository ligneFactureClientRepository;
    private final BonLivraisonRepository bonLivraisonRepository;
    private final CommandeClientRepository commandeClientRepository;
    private final AuditService auditService;

    /** Préfixe pour les numéros de facture */
    private static final String PREFIXE_NUMERO = "FC";

    /** Taux TVA par défaut */
    private static final BigDecimal TAUX_TVA = new BigDecimal("0.20");

    // ==================== CRUD ====================

    /**
     * TODO.YML Ligne 27: Générer une facture depuis un bon de livraison
     */
    public FactureClient genererDepuisBL(Long blId, Utilisateur utilisateur) {
        BonLivraison bl = bonLivraisonRepository.findById(blId)
                .orElseThrow(() -> new IllegalArgumentException("Bon de livraison non trouvé: " + blId));

        // Vérifier que le BL est livré
        if (!"LIVRE".equals(bl.getStatut())) {
            throw new IllegalStateException("Le bon de livraison doit être livré pour générer une facture");
        }

        // Vérifier qu'une facture n'existe pas déjà pour ce BL
        List<FactureClient> facturesExistantes = factureClientRepository.findByBonLivraisonId(blId);
        if (!facturesExistantes.isEmpty()) {
            throw new IllegalStateException("Une facture existe déjà pour ce bon de livraison");
        }

        CommandeClient commande = bl.getCommandeClient();

        // Créer la facture
        FactureClient facture = new FactureClient();
        facture.setNumero(genererNumero());
        facture.setCommandeClient(commande);
        facture.setBonLivraison(bl);
        facture.setClient(commande.getClient());
        facture.setDateFacture(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30)); // Échéance 30 jours par défaut
        facture.setStatut("BROUILLON");

        FactureClient savedFacture = factureClientRepository.save(facture);

        // Créer les lignes de facture depuis le BL
        BigDecimal totalHt = BigDecimal.ZERO;
        for (LigneBL ligneBL : bl.getLignes()) {
            LigneFactureClient ligneFacture = new LigneFactureClient();
            ligneFacture.setFactureClient(savedFacture);
            ligneFacture.setArticle(ligneBL.getArticle());
            ligneFacture.setQuantite(ligneBL.getQuantiteLivree());
            ligneFacture.setPrixUnitaireHt(ligneBL.getLigneCommande().getPrixUnitaireHt());
            ligneFacture.calculerMontant();
            ligneFactureClientRepository.save(ligneFacture);
            savedFacture.getLignes().add(ligneFacture);

            if (ligneFacture.getMontantLigneHt() != null) {
                totalHt = totalHt.add(ligneFacture.getMontantLigneHt());
            }
        }

        // Calculer les totaux
        savedFacture.setMontantHt(totalHt);
        savedFacture.setMontantTva(totalHt.multiply(TAUX_TVA).setScale(4, RoundingMode.HALF_UP));
        savedFacture.setMontantTtc(totalHt.add(savedFacture.getMontantTva()));
        factureClientRepository.save(savedFacture);

        auditService.logAction(utilisateur, "facture_client", savedFacture.getId(),
                "CREATE_FROM_BL", null, "Facture créée depuis BL " + bl.getNumero(), null);

        return savedFacture;
    }

    /**
     * Enregistrer une facture
     */
    public FactureClient enregistrer(FactureClient facture) {
        if (facture.getId() == null) {
            facture.setNumero(genererNumero());
            facture.setDateFacture(LocalDate.now());
            facture.setStatut("BROUILLON");
        }
        return factureClientRepository.save(facture);
    }

    /**
     * Récupérer toutes les factures
     */
    public List<FactureClient> listerTous() {
        return factureClientRepository.findAll();
    }

    /**
     * Récupérer une facture par ID
     */
    public FactureClient obtenirParId(Long id) {
        return factureClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée: " + id));
    }

    /**
     * Récupérer les factures par statut
     */
    public List<FactureClient> listerParStatut(String statut) {
        return factureClientRepository.findByStatut(statut);
    }

    /**
     * Récupérer les factures par client
     */
    public List<FactureClient> listerParClient(Long clientId) {
        return factureClientRepository.findByClientId(clientId);
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 28: Valider la facture (contrôle TVA)
     */
    public void validerFacture(Long factureId, Utilisateur comptable) {
        FactureClient facture = obtenirParId(factureId);

        if (!"BROUILLON".equals(facture.getStatut())) {
            throw new IllegalStateException("Seules les factures en brouillon peuvent être validées");
        }

        // Contrôle TVA
        BigDecimal tvaCalculee = facture.getMontantHt().multiply(TAUX_TVA).setScale(4, RoundingMode.HALF_UP);
        BigDecimal ecartTva = facture.getMontantTva().subtract(tvaCalculee).abs();

        // Tolérance de 0.01 pour les arrondis
        if (ecartTva.compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalStateException("Erreur de calcul TVA détectée. TVA attendue: "
                    + tvaCalculee + ", TVA facture: " + facture.getMontantTva());
        }

        facture.setStatut("VALIDEE");
        factureClientRepository.save(facture);

        // Mettre à jour le statut de la commande
        CommandeClient commande = facture.getCommandeClient();
        if (commande != null) {
            commande.setStatut("FACTUREE");
            commandeClientRepository.save(commande);
        }

        auditService.logAction(comptable, "facture_client", factureId,
                "VALIDATE", "BROUILLON", "VALIDEE", "Contrôle TVA OK");
    }

    /**
     * Marquer comme partiellement payée
     */
    public void marquerPaiementPartiel(Long factureId, BigDecimal montantRecu) {
        FactureClient facture = obtenirParId(factureId);

        if (!"VALIDEE".equals(facture.getStatut()) && !"PAYEE_PARTIELLEMENT".equals(facture.getStatut())) {
            throw new IllegalStateException("La facture doit être validée pour enregistrer un paiement");
        }

        facture.setStatut("PAYEE_PARTIELLEMENT");
        factureClientRepository.save(facture);
    }

    /**
     * Marquer comme totalement payée
     */
    public void marquerPayee(Long factureId, Utilisateur utilisateur) {
        FactureClient facture = obtenirParId(factureId);

        facture.setStatut("PAYEE");
        factureClientRepository.save(facture);

        auditService.logAction(utilisateur, "facture_client", factureId,
                "PAID", facture.getStatut(), "PAYEE", null);
    }

    /**
     * Annuler une facture
     */
    public void annulerFacture(Long factureId, Utilisateur utilisateur, String motif) {
        FactureClient facture = obtenirParId(factureId);

        if ("PAYEE".equals(facture.getStatut())) {
            throw new IllegalStateException("Les factures payées ne peuvent pas être annulées");
        }

        String ancienStatut = facture.getStatut();
        facture.setStatut("ANNULEE");
        factureClientRepository.save(facture);

        auditService.logAction(utilisateur, "facture_client", factureId,
                "CANCEL", ancienStatut, "ANNULEE", motif);
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique de facture
     */
    private String genererNumero() {
        long count = factureClientRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }

    /**
     * Calculer le solde restant d'une facture
     */
    public BigDecimal calculerSoldeRestant(Long factureId, BigDecimal totalEncaisse) {
        FactureClient facture = obtenirParId(factureId);
        return facture.getMontantTtc().subtract(totalEncaisse);
    }
}
