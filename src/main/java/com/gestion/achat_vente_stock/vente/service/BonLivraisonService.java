package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.vente.model.*;
import com.gestion.achat_vente_stock.vente.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 24-26: Ventes > Livraison
 * - Ligne 24: Préparer livraison (picking : article, qté, emplacement, lot)
 * - Ligne 25: Confirmer picking et générer bon de livraison
 * - Ligne 26: Bloquer livraison si stock insuffisant
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BonLivraisonService {

    private final BonLivraisonRepository bonLivraisonRepository;
    private final LigneBLRepository ligneBLRepository;
    private final CommandeClientRepository commandeClientRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final AuditService auditService;

    /** Préfixe pour les numéros de BL */
    private static final String PREFIXE_NUMERO = "BL";

    // ==================== CRUD ====================

    /**
     * TODO.YML Ligne 24: Créer un bon de livraison depuis une commande
     */
    public BonLivraison creerBonLivraison(Long commandeId, Depot depot, Utilisateur magasinier) {
        CommandeClient commande = commandeClientRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée: " + commandeId));

        // Vérifier que la commande est confirmée ou en préparation
        if (!"CONFIRMEE".equals(commande.getStatut()) && !"EN_PREPARATION".equals(commande.getStatut())) {
            throw new IllegalStateException("La commande doit être confirmée pour créer un BL");
        }

        // Créer le BL
        BonLivraison bl = new BonLivraison();
        bl.setNumero(genererNumero());
        bl.setCommandeClient(commande);
        bl.setMagasinier(magasinier);
        bl.setDepot(depot);
        bl.setDateLivraison(LocalDate.now());
        bl.setStatut("EN_PREPARATION");

        BonLivraison savedBL = bonLivraisonRepository.save(bl);

        // Créer les lignes du BL depuis les lignes de commande
        for (LigneCommandeClient ligneCC : commande.getLignes()) {
            LigneBL ligneBL = new LigneBL();
            ligneBL.setBonLivraison(savedBL);
            ligneBL.setLigneCommande(ligneCC);
            ligneBL.setArticle(ligneCC.getArticle());
            ligneBL.setQuantiteLivree(BigDecimal.ZERO); // À renseigner lors du picking
            ligneBLRepository.save(ligneBL);
            savedBL.getLignes().add(ligneBL);
        }

        // Mettre à jour le statut de la commande
        commande.setStatut("EN_PREPARATION");
        commandeClientRepository.save(commande);

        auditService.logAction(magasinier, "bon_livraison", savedBL.getId(),
                "CREATE", null, "BL créé pour commande " + commande.getNumero(), null);

        return savedBL;
    }

    /**
     * Récupérer tous les BL
     */
    public List<BonLivraison> listerTous() {
        return bonLivraisonRepository.findAll();
    }

    /**
     * Récupérer un BL par ID
     */
    public BonLivraison obtenirParId(Long id) {
        return bonLivraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bon de livraison non trouvé: " + id));
    }

    /**
     * Récupérer les BL par statut
     */
    public List<BonLivraison> listerParStatut(String statut) {
        return bonLivraisonRepository.findByStatut(statut);
    }

    // ==================== PICKING ====================

    /**
     * TODO.YML Ligne 24: Mettre à jour une ligne de picking
     */
    public void mettreAJourLignePicking(Long ligneBLId, BigDecimal quantitePreparee, 
            String lotNumero, LocalDate dluo, Utilisateur magasinier) {
        LigneBL ligne = ligneBLRepository.findById(ligneBLId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne BL non trouvée: " + ligneBLId));

        LigneCommandeClient ligneCommande = ligne.getLigneCommande();
        
        // TODO.YML Ligne 26: Vérifier le stock disponible
        // Pour l'instant, on vérifie juste que la quantité ne dépasse pas la commande
        BigDecimal quantiteRestante = ligneCommande.getQuantite()
                .subtract(ligneCommande.getQuantitePreparee() != null ? ligneCommande.getQuantitePreparee() : BigDecimal.ZERO);
        
        if (quantitePreparee.compareTo(quantiteRestante) > 0) {
            throw new IllegalStateException("Quantité préparée supérieure à la quantité restante à préparer");
        }

        ligne.setQuantiteLivree(quantitePreparee);
        ligne.setLotNumero(lotNumero);
        ligne.setDluo(dluo);
        ligneBLRepository.save(ligne);

        // Mettre à jour la quantité préparée sur la ligne de commande
        BigDecimal totalPreparee = ligneCommande.getQuantitePreparee() != null 
                ? ligneCommande.getQuantitePreparee() : BigDecimal.ZERO;
        ligneCommande.setQuantitePreparee(totalPreparee.add(quantitePreparee));
        ligneCommandeClientRepository.save(ligneCommande);

        auditService.logAction(magasinier, "ligne_bl", ligneBLId,
                "PICK", null, "Picking: " + quantitePreparee + " " + ligne.getArticle().getCode(), null);
    }

    /**
     * TODO.YML Ligne 25: Confirmer le picking et préparer le BL
     */
    public void confirmerPicking(Long blId, Utilisateur magasinier) {
        BonLivraison bl = obtenirParId(blId);

        if (!"EN_PREPARATION".equals(bl.getStatut())) {
            throw new IllegalStateException("Ce BL n'est pas en préparation");
        }

        // Vérifier que toutes les lignes ont été préparées
        for (LigneBL ligne : bl.getLignes()) {
            if (ligne.getQuantiteLivree() == null || ligne.getQuantiteLivree().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Toutes les lignes doivent être préparées avant confirmation");
            }
        }

        bl.setStatut("PRET");
        bonLivraisonRepository.save(bl);

        // Mettre à jour le statut de la commande
        CommandeClient commande = bl.getCommandeClient();
        commande.setStatut("PREPAREE");
        commandeClientRepository.save(commande);

        auditService.logAction(magasinier, "bon_livraison", blId,
                "CONFIRM_PICKING", "EN_PREPARATION", "PRET", null);
    }

    /**
     * TODO.YML Ligne 25: Expédier le BL
     */
    public void expedierBL(Long blId, Utilisateur magasinier) {
        BonLivraison bl = obtenirParId(blId);

        if (!"PRET".equals(bl.getStatut())) {
            throw new IllegalStateException("Ce BL n'est pas prêt pour expédition");
        }

        bl.setStatut("EXPEDIE");
        bl.setDateLivraison(LocalDate.now());
        bonLivraisonRepository.save(bl);

        auditService.logAction(magasinier, "bon_livraison", blId,
                "SHIP", "PRET", "EXPEDIE", null);
    }

    /**
     * Confirmer la livraison
     */
    public void confirmerLivraison(Long blId, Utilisateur utilisateur) {
        BonLivraison bl = obtenirParId(blId);

        if (!"EXPEDIE".equals(bl.getStatut())) {
            throw new IllegalStateException("Ce BL n'est pas en cours d'expédition");
        }

        bl.setStatut("LIVRE");
        bonLivraisonRepository.save(bl);

        // Mettre à jour les quantités livrées sur la commande
        for (LigneBL ligneBL : bl.getLignes()) {
            LigneCommandeClient ligneCC = ligneBL.getLigneCommande();
            BigDecimal totalLivree = ligneCC.getQuantiteLivree() != null 
                    ? ligneCC.getQuantiteLivree() : BigDecimal.ZERO;
            ligneCC.setQuantiteLivree(totalLivree.add(ligneBL.getQuantiteLivree()));
            ligneCommandeClientRepository.save(ligneCC);
        }

        // Vérifier si la commande est totalement livrée
        CommandeClient commande = bl.getCommandeClient();
        boolean toutLivre = true;
        for (LigneCommandeClient ligne : commande.getLignes()) {
            if (ligne.getQuantiteLivree() == null 
                    || ligne.getQuantiteLivree().compareTo(ligne.getQuantite()) < 0) {
                toutLivre = false;
                break;
            }
        }

        if (toutLivre) {
            commande.setStatut("LIVREE");
            commandeClientRepository.save(commande);
        }

        auditService.logAction(utilisateur, "bon_livraison", blId,
                "DELIVER", "EXPEDIE", "LIVRE", null);
    }

    /**
     * TODO.YML Ligne 26: Vérifier la disponibilité du stock
     */
    public boolean verifierDisponibiliteStock(Long commandeId) {
        CommandeClient commande = commandeClientRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée: " + commandeId));

        // TODO: Implémenter la vérification réelle avec le module Stock
        // Pour l'instant, retourne toujours true
        for (LigneCommandeClient ligne : commande.getLignes()) {
            // Vérifier stock disponible pour chaque article
            // BigDecimal stockDisponible = stockService.getStockDisponible(ligne.getArticle().getId());
            // if (stockDisponible.compareTo(ligne.getQuantite()) < 0) {
            //     return false;
            // }
        }

        return true;
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique de BL
     */
    private String genererNumero() {
        long count = bonLivraisonRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }
}
