package com.gestion.achat_vente_stock.referentiel.controller;

import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.service.DepotService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Ligne 5: Référentiels > Dépôts
 * Page "Gestion dépôts"
 * Créer dépôts/emplacements (site, zone, adresse)
 */
@Controller
@RequestMapping("/depots")
@RequiredArgsConstructor
public class DepotController {
    
    private final DepotService depotService;
    
    @GetMapping
    public String listerDepots(Model model) {
        model.addAttribute("depots", depotService.listerTous());
        model.addAttribute("titre", "Gestion des Dépôts");
        return "referentiel/depots/liste";
    }
    
    @GetMapping("/nouveau")
    public String nouveauDepot(Model model) {
        model.addAttribute("depot", new Depot());
        model.addAttribute("titre", "Nouveau Dépôt");
        model.addAttribute("action", "Créer");
        return "referentiel/depots/formulaire";
    }
    
    @PostMapping
    public String creerDepot(@ModelAttribute Depot depot, RedirectAttributes redirectAttributes) {
        try {
            depotService.creerDepot(depot);
            redirectAttributes.addFlashAttribute("message", "Dépôt créé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/depots";
    }
    
    @GetMapping("/{id}/modifier")
    public String modifierDepot(@PathVariable Long id, Model model) {
        model.addAttribute("depot", depotService.trouverParId(id));
        model.addAttribute("titre", "Modifier Dépôt");
        model.addAttribute("action", "Modifier");
        return "referentiel/depots/formulaire";
    }
    
    @PostMapping("/{id}")
    public String mettreAJourDepot(@PathVariable Long id, @ModelAttribute Depot depot, 
                                    RedirectAttributes redirectAttributes) {
        try {
            depotService.modifierDepot(id, depot);
            redirectAttributes.addFlashAttribute("message", "Dépôt modifié avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/depots";
    }
    
    @GetMapping("/{id}/supprimer")
    public String supprimerDepot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            depotService.supprimerDepot(id);
            redirectAttributes.addFlashAttribute("message", "Dépôt supprimé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/depots";
    }
}
