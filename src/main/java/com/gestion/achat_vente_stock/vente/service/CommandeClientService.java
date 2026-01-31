package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.vente.model.*;
import com.gestion.achat_vente_stock.vente.repository.CommandeClientRepository;
import com.gestion.achat_vente_stock.vente.repository.LigneCommandeClientRepository;
import com.gestion.achat_vente_stock.vente.repository.DevisRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 22-23: Ventes > Commande Client
 * - Ligne 22: Transformer devis en commande client
 * - Ligne 23: Réserver stock à la commande (configurable)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommandeClientService {

    private final CommandeClientRepository commandeClientRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final DevisRepository devisRepository;
    private final AuditService auditService;

    /** Préfixe pour les numéros de commande */
    private static final String PREFIXE_NUMERO = "CC";

    // ==================== CRUD ====================

    /**
     * Créer une commande client directement
     */
    public CommandeClient creerCommande(CommandeClient commande, Utilisateur commercial) {
        commande.setNumero(genererNumero());
        commande.setCommercial(commercial);
        commande.setDateCommande(LocalDate.now());
        commande.setStatut("BROUILLON");
        commande.setStockReserve(false);

        calculerTotaux(commande);

        CommandeClient saved = commandeClientRepository.save(commande);

        auditService.logAction(commercial, "commande_client", saved.getId(),
                "CREATE", null, "Commande créée: " + saved.getNumero(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 22: Transformer un devis accepté en commande client
     */
    public CommandeClient transformerDevisEnCommande(Long devisId, Utilisateur commercial) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé: " + devisId));

        // Vérifier que le devis est accepté
        if (!"ACCEPTE".equals(devis.getStatut())) {
            throw new IllegalStateException("Seuls les devis acceptés peuvent être transformés en commande");
        }

        // Créer la commande
        CommandeClient commande = new CommandeClient();
        commande.setNumero(genererNumero());
        commande.setDevis(devis);
        commande.setClient(devis.getClient());
        commande.setCommercial(commercial);
        commande.setDateCommande(LocalDate.now());
        commande.setMontantTotalHt(devis.getMontantTotalHt());
        commande.setMontantTva(devis.getMontantTva());
        commande.setMontantTotalTtc(devis.getMontantTotalTtc());
        commande.setStatut("BROUILLON");
        commande.setStockReserve(false);

        CommandeClient savedCommande = commandeClientRepository.save(commande);

        // Copier les lignes du devis
        for (LigneDevis ligneDevis : devis.getLignes()) {
            LigneCommandeClient ligneCC = new LigneCommandeClient();
            ligneCC.setCommandeClient(savedCommande);
            ligneCC.setArticle(ligneDevis.getArticle());
            ligneCC.setQuantite(ligneDevis.getQuantite());
            ligneCC.setPrixUnitaireHt(ligneDevis.getPrixUnitaireHt());
            ligneCC.setRemisePourcent(ligneDevis.getRemisePourcent());
            ligneCC.setMontantLigneHt(ligneDevis.getMontantLigneHt());
            ligneCC.setQuantitePreparee(BigDecimal.ZERO);
            ligneCC.setQuantiteLivree(BigDecimal.ZERO);
            ligneCommandeClientRepository.save(ligneCC);
            savedCommande.getLignes().add(ligneCC);
        }

        // Mettre à jour le statut du devis
        devis.setStatut("TRANSFORME");
        devisRepository.save(devis);

        auditService.logAction(commercial, "commande_client", savedCommande.getId(),
                "CREATE_FROM_DEVIS", null, "Commande créée depuis devis " + devis.getNumero(), null);

        return savedCommande;
    }

    /**
     * Enregistrer une commande
     */
    public CommandeClient enregistrer(CommandeClient commande) {
        if (commande.getId() == null) {
            commande.setNumero(genererNumero());
            commande.setDateCommande(LocalDate.now());
            commande.setStatut("BROUILLON");
            commande.setStockReserve(false);
        }
        calculerTotaux(commande);
        return commandeClientRepository.save(commande);
    }

    /**
     * Récupérer toutes les commandes
     */
    public List<CommandeClient> listerTous() {
        return commandeClientRepository.findAll();
    }

    /**
     * Récupérer une commande par ID
     */
    public CommandeClient obtenirParId(Long id) {
        return commandeClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée: " + id));
    }

    /**
     * Récupérer les commandes par statut
     */
    public List<CommandeClient> listerParStatut(String statut) {
        return commandeClientRepository.findByStatut(statut);
    }

    // ==================== WORKFLOW ====================

    /**
     * Confirmer une commande (prête pour préparation)
     */
    public void confirmerCommande(Long commandeId, Utilisateur utilisateur) {
        CommandeClient commande = obtenirParId(commandeId);

        if (!"BROUILLON".equals(commande.getStatut())) {
            throw new IllegalStateException("Seules les commandes en brouillon peuvent être confirmées");
        }

        commande.setStatut("CONFIRMEE");
        commandeClientRepository.save(commande);

        auditService.logAction(utilisateur, "commande_client", commandeId,
                "CONFIRM", "BROUILLON", "CONFIRMEE", null);
    }

    /**
     * TODO.YML Ligne 23: Réserver le stock pour une commande
     */
    public void reserverStock(Long commandeId, Utilisateur utilisateur) {
        CommandeClient commande = obtenirParId(commandeId);

        if (commande.getStockReserve()) {
            throw new IllegalStateException("Le stock est déjà réservé pour cette commande");
        }

        // TODO: Implémenter la réservation réelle dans le module Stock
        // Pour l'instant, on marque juste la commande
        commande.setStockReserve(true);
        commandeClientRepository.save(commande);

        auditService.logAction(utilisateur, "commande_client", commandeId,
                "RESERVE_STOCK", null, "Stock réservé", null);
    }

    /**
     * Libérer le stock réservé
     */
    public void libererStock(Long commandeId, Utilisateur utilisateur) {
        CommandeClient commande = obtenirParId(commandeId);

        if (!commande.getStockReserve()) {
            throw new IllegalStateException("Aucun stock réservé pour cette commande");
        }

        // TODO: Implémenter la libération réelle dans le module Stock
        commande.setStockReserve(false);
        commandeClientRepository.save(commande);

        auditService.logAction(utilisateur, "commande_client", commandeId,
                "RELEASE_STOCK", null, "Stock libéré", null);
    }

    /**
     * Passer la commande en préparation
     */
    public void demarrerPreparation(Long commandeId, Utilisateur utilisateur) {
        CommandeClient commande = obtenirParId(commandeId);

        if (!"CONFIRMEE".equals(commande.getStatut())) {
            throw new IllegalStateException("Seules les commandes confirmées peuvent être préparées");
        }

        commande.setStatut("EN_PREPARATION");
        commandeClientRepository.save(commande);

        auditService.logAction(utilisateur, "commande_client", commandeId,
                "START_PREPARATION", "CONFIRMEE", "EN_PREPARATION", null);
    }

    /**
     * Annuler une commande
     */
    public void annulerCommande(Long commandeId, Utilisateur utilisateur, String motif) {
        CommandeClient commande = obtenirParId(commandeId);

        if ("LIVREE".equals(commande.getStatut()) || "FACTUREE".equals(commande.getStatut())) {
            throw new IllegalStateException("Les commandes livrées ou facturées ne peuvent pas être annulées");
        }

        String ancienStatut = commande.getStatut();
        commande.setStatut("ANNULEE");
        
        // Libérer le stock si réservé
        if (commande.getStockReserve()) {
            commande.setStockReserve(false);
        }
        
        commandeClientRepository.save(commande);

        auditService.logAction(utilisateur, "commande_client", commandeId,
                "CANCEL", ancienStatut, "ANNULEE", motif);
    }

    // ==================== CALCULS ====================

    /**
     * Calculer les totaux de la commande
     */
    public void calculerTotaux(CommandeClient commande) {
        BigDecimal totalHt = BigDecimal.ZERO;

        for (LigneCommandeClient ligne : commande.getLignes()) {
            ligne.calculerMontant();
            if (ligne.getMontantLigneHt() != null) {
                totalHt = totalHt.add(ligne.getMontantLigneHt());
            }
        }

        commande.setMontantTotalHt(totalHt);

        // TVA à 20%
        BigDecimal tva = totalHt.multiply(new BigDecimal("0.20")).setScale(4, RoundingMode.HALF_UP);
        commande.setMontantTva(tva);

        // TTC
        commande.setMontantTotalTtc(totalHt.add(tva));
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique de commande
     */
    private String genererNumero() {
        long count = commandeClientRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }
}
