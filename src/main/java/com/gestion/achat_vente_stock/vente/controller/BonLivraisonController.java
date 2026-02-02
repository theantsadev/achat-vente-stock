package com.gestion.achat_vente_stock.vente.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.vente.model.BonLivraison;
import com.gestion.achat_vente_stock.vente.model.LigneBL;
import com.gestion.achat_vente_stock.vente.service.BonLivraisonService;
import com.gestion.achat_vente_stock.vente.service.CommandeClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.YML Lignes 24-26: Ventes > Livraison
 * - Ligne 24: Préparer livraison (picking : article, qté, emplacement, lot)
 * - Ligne 25: Confirmer picking et générer bon de livraison
 * - Ligne 26: Bloquer livraison si stock insuffisant
 * 
 * Contrôleur web pour la gestion des bons de livraison
 * 
 * Rôles autorisés: ROLE-MAGASINIER-SORT, ROLE-CHEF-MAGASIN, ROLE-ADMIN
 */
@Controller
@RequestMapping("/ventes/livraisons")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-COMMERCIAL', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class BonLivraisonController {

    private final BonLivraisonService bonLivraisonService;
    private final CommandeClientService commandeClientService;
    private final DepotRepository depotRepository;
    private final SessionService sessionService;

    // ==================== LISTE ====================

    /**
     * Liste des bons de livraison
     */
    @GetMapping
    public String lister(Model model) {
        List<BonLivraison> livraisons = bonLivraisonService.listerTous();
        model.addAttribute("livraisons", livraisons);
        return "ventes/livraisons/liste";
    }

    // ==================== CRÉATION ====================

    /**
     * TODO.YML Ligne 24: Créer un BL depuis une commande
     */
    @PostMapping("/depuis-commande/{commandeId}")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creerDepuisCommande(@PathVariable Long commandeId,
            @RequestParam Long depotId,
            RedirectAttributes redirectAttributes) {
        try {
            // TODO.YML Ligne 26: Vérifier la disponibilité du stock
            boolean stockDisponible = bonLivraisonService.verifierDisponibiliteStock(commandeId);
            if (!stockDisponible) {
                redirectAttributes.addFlashAttribute("error",
                        "Stock insuffisant pour cette commande. Livraison bloquée.");
                return "redirect:/ventes/commandes/" + commandeId;
            }

            Utilisateur magasinier = sessionService.getUtilisateurConnecte();
            Depot depot = depotRepository.findById(depotId).orElse(null);

            BonLivraison bl = bonLivraisonService.creerBonLivraison(commandeId, depot, magasinier);
            redirectAttributes.addFlashAttribute("success", "Bon de livraison créé: " + bl.getNumero());
            return "redirect:/ventes/livraisons/" + bl.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/commandes/" + commandeId;
        }
    }

    /**
     * Formulaire de sélection du dépôt pour créer un BL
     */
    @GetMapping("/preparer/{commandeId}")
    public String preparerFormulaire(@PathVariable Long commandeId, Model model) {
        model.addAttribute("commande", commandeClientService.obtenirParId(commandeId));
        model.addAttribute("depots", depotRepository.findAll());
        return "ventes/livraisons/preparer";
    }

    // ==================== DÉTAIL ====================

    /**
     * Détail d'un bon de livraison
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BonLivraison bl = bonLivraisonService.obtenirParId(id);
        model.addAttribute("livraison", bl);
        return "ventes/livraisons/detail";
    }

    // ==================== PICKING ====================

    /**
     * TODO.YML Ligne 24: Formulaire de picking
     */
    @GetMapping("/{id}/picking")
    public String pickingFormulaire(@PathVariable Long id, Model model) {
        BonLivraison bl = bonLivraisonService.obtenirParId(id);
        model.addAttribute("livraison", bl);
        model.addAttribute("lignesJson", convertLignesToJson(bl.getLignes()));
        return "ventes/livraisons/picking";
    }

    /**
     * TODO.YML Ligne 24: Mettre à jour une ligne de picking
     */
    @PostMapping("/{id}/picking/{ligneId}")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String mettreAJourLignePicking(@PathVariable Long id,
            @PathVariable Long ligneId,
            @RequestParam BigDecimal quantitePreparee,
            @RequestParam(required = false) String lotNumero,
            @RequestParam(required = false) String dluo,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur magasinier = sessionService.getUtilisateurConnecte();
            LocalDate dluoDate = dluo != null && !dluo.isEmpty() ? LocalDate.parse(dluo) : null;

            bonLivraisonService.mettreAJourLignePicking(ligneId, quantitePreparee, lotNumero, dluoDate, magasinier);
            redirectAttributes.addFlashAttribute("success", "Ligne mise à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/livraisons/" + id + "/picking";
    }

    /**
     * TODO.YML Ligne 25: Confirmer le picking
     */
    @PostMapping("/{id}/confirmer-picking")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String confirmerPicking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur magasinier = sessionService.getUtilisateurConnecte();
            bonLivraisonService.confirmerPicking(id, magasinier);
            redirectAttributes.addFlashAttribute("success", "Picking confirmé - BL prêt pour expédition");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/livraisons/" + id;
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 25: Expédier le BL
     */
    @PostMapping("/{id}/expedier")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String expedier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur magasinier = sessionService.getUtilisateurConnecte();
            bonLivraisonService.expedierBL(id, magasinier);
            redirectAttributes.addFlashAttribute("success", "Bon de livraison expédié");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/livraisons/" + id;
    }

    /**
     * Confirmer la livraison
     */
    @PostMapping("/{id}/confirmer-livraison")
    public String confirmerLivraison(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            bonLivraisonService.confirmerLivraison(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Livraison confirmée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/livraisons/" + id;
    }

    // ==================== API JSON ====================

    /**
     * API REST pour récupérer les lignes d'un BL
     */
    @GetMapping("/api/{id}/lignes")
    @ResponseBody
    public List<Map<String, Object>> getLignesBL(@PathVariable Long id) {
        BonLivraison bl = bonLivraisonService.obtenirParId(id);
        return convertLignesToList(bl.getLignes());
    }

    // ==================== UTILITAIRES ====================

    private String convertLignesToJson(List<LigneBL> lignes) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(convertLignesToList(lignes));
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Map<String, Object>> convertLignesToList(List<LigneBL> lignes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LigneBL ligne : lignes) {
            Map<String, Object> ligneMap = new HashMap<>();
            ligneMap.put("id", ligne.getId());
            ligneMap.put("quantiteLivree", ligne.getQuantiteLivree());
            ligneMap.put("lotNumero", ligne.getLotNumero());
            ligneMap.put("dluo", ligne.getDluo());

            if (ligne.getArticle() != null) {
                Map<String, Object> articleMap = new HashMap<>();
                articleMap.put("id", ligne.getArticle().getId());
                articleMap.put("code", ligne.getArticle().getCode());
                articleMap.put("designation", ligne.getArticle().getDesignation());
                ligneMap.put("article", articleMap);
            }

            if (ligne.getLigneCommande() != null) {
                ligneMap.put("quantiteCommandee", ligne.getLigneCommande().getQuantite());
            }

            result.add(ligneMap);
        }
        return result;
    }
}
