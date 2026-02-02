package com.gestion.achat_vente_stock.vente.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.model.LigneFactureClient;
import com.gestion.achat_vente_stock.vente.service.BonLivraisonService;
import com.gestion.achat_vente_stock.vente.service.FactureClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.YML Lignes 27-28: Ventes > Facturation
 * - Ligne 27: Émettre facture depuis BL (avec calcul auto)
 * - Ligne 28: Contrôle TVA obligatoire
 * 
 * Contrôleur web pour la gestion des factures clients
 */
@Controller
@RequestMapping("/ventes/factures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-COMMERCIAL', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class FactureClientController {

    private final FactureClientService factureClientService;
    private final BonLivraisonService bonLivraisonService;
    private final SessionService sessionService;

    // ==================== LISTE ====================

    /**
     * Liste des factures
     */
    @GetMapping
    public String lister(Model model) {
        List<FactureClient> factures = factureClientService.listerToutes();
        model.addAttribute("factures", factures);
        return "ventes/factures/liste";
    }

    /**
     * Liste des factures non payées
     */
    @GetMapping("/non-payees")
    public String listerNonPayees(Model model) {
        List<FactureClient> factures = factureClientService.listerFacturesNonPayees();
        model.addAttribute("factures", factures);
        model.addAttribute("filtreActif", "non-payees");
        return "ventes/factures/liste";
    }

    /**
     * Factures d'un client
     */
    @GetMapping("/client/{clientId}")
    public String listerParClient(@PathVariable Long clientId, Model model) {
        List<FactureClient> factures = factureClientService.listerParClient(clientId);
        model.addAttribute("factures", factures);
        return "ventes/factures/liste";
    }

    // ==================== CRÉATION ====================

    /**
     * TODO.YML Ligne 27: Générer une facture depuis un bon de livraison
     */
    @PostMapping("/depuis-bl/{blId}")
    public String genererDepuisBL(@PathVariable Long blId,
                                  @RequestParam(defaultValue = "30") int delaiPaiement,
                                  RedirectAttributes redirectAttributes) {
        try {
            FactureClient facture = factureClientService.genererDepuisBL(blId, delaiPaiement);
            redirectAttributes.addFlashAttribute("success", 
                "Facture " + facture.getNumero() + " générée avec succès");
            return "redirect:/ventes/factures/" + facture.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/livraisons/" + blId;
        }
    }

    /**
     * Formulaire pour générer une facture depuis BL
     */
    @GetMapping("/generer/{blId}")
    public String genererFormulaire(@PathVariable Long blId, Model model) {
        model.addAttribute("bonLivraison", bonLivraisonService.obtenirParId(blId));
        return "ventes/factures/generer";
    }

    // ==================== DÉTAIL ====================

    /**
     * Détail d'une facture
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        FactureClient facture = factureClientService.obtenirParId(id);
        BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(id);
        
        model.addAttribute("facture", facture);
        model.addAttribute("soldeRestant", soldeRestant);
        return "ventes/factures/detail";
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 28: Valider une facture (contrôle TVA)
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            factureClientService.validerFacture(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Facture validée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur de validation: " + e.getMessage());
        }
        return "redirect:/ventes/factures/" + id;
    }

    /**
     * Envoyer la facture au client
     */
    @PostMapping("/{id}/envoyer")
    public String envoyer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            factureClientService.envoyerFacture(id);
            redirectAttributes.addFlashAttribute("success", "Facture envoyée au client");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/factures/" + id;
    }

    /**
     * Marquer la facture comme payée
     */
    @PostMapping("/{id}/marquer-payee")
    public String marquerPayee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            factureClientService.marquerPayee(id);
            redirectAttributes.addFlashAttribute("success", "Facture marquée comme payée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/factures/" + id;
    }

    /**
     * Annuler une facture
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            factureClientService.annulerFacture(id);
            redirectAttributes.addFlashAttribute("success", "Facture annulée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/factures/" + id;
    }

    // ==================== IMPRESSION ====================

    /**
     * Imprimer/générer PDF de la facture
     */
    @GetMapping("/{id}/imprimer")
    public String imprimer(@PathVariable Long id, Model model) {
        FactureClient facture = factureClientService.obtenirParId(id);
        BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(id);
        
        model.addAttribute("facture", facture);
        model.addAttribute("soldeRestant", soldeRestant);
        return "ventes/factures/imprimer";
    }

    // ==================== API JSON ====================

    /**
     * API REST pour récupérer les lignes d'une facture
     */
    @GetMapping("/api/{id}/lignes")
    @ResponseBody
    public List<Map<String, Object>> getLignesFacture(@PathVariable Long id) {
        FactureClient facture = factureClientService.obtenirParId(id);
        return convertLignesToList(facture.getLignes());
    }

    /**
     * API REST pour récupérer le solde restant
     */
    @GetMapping("/api/{id}/solde")
    @ResponseBody
    public Map<String, Object> getSoldeRestant(@PathVariable Long id) {
        FactureClient facture = factureClientService.obtenirParId(id);
        BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("factureId", id);
        result.put("montantTtc", facture.getMontantTtc());
        result.put("soldeRestant", soldeRestant);
        result.put("estPayee", facture.getEstPayee());
        
        return result;
    }

    // ==================== UTILITAIRES ====================

    private String convertLignesToJson(List<LigneFactureClient> lignes) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(convertLignesToList(lignes));
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Map<String, Object>> convertLignesToList(List<LigneFactureClient> lignes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LigneFactureClient ligne : lignes) {
            Map<String, Object> ligneMap = new HashMap<>();
            ligneMap.put("id", ligne.getId());
            ligneMap.put("quantite", ligne.getQuantite());
            ligneMap.put("prixUnitaireHt", ligne.getPrixUnitaireHt());
            ligneMap.put("remisePourcent", ligne.getRemisePourcent());
            ligneMap.put("montantHt", ligne.calculerMontantHt());
            
            if (ligne.getArticle() != null) {
                Map<String, Object> articleMap = new HashMap<>();
                articleMap.put("id", ligne.getArticle().getId());
                articleMap.put("code", ligne.getArticle().getCode());
                articleMap.put("designation", ligne.getArticle().getDesignation());
                articleMap.put("tauxTva", ligne.getArticle().getTauxTva());
                ligneMap.put("article", articleMap);
            }
            
            result.add(ligneMap);
        }
        return result;
    }
}
