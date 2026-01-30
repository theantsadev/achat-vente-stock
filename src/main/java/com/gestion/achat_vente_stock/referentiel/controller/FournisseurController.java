package com.gestion.achat_vente_stock.referentiel.controller;

import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;
import com.gestion.achat_vente_stock.referentiel.service.FournisseurService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Ligne 3: Référentiels > Fournisseurs
 * Page "Gestion fournisseurs"
 * Créer/modifier fournisseurs (raison sociale, contacts, conditions)
 */
@Controller
@RequestMapping("/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {
    
    private final FournisseurService fournisseurService;
    
    @GetMapping
    public String listerFournisseurs(Model model) {
        model.addAttribute("fournisseurs", fournisseurService.listerTous());
        model.addAttribute("titre", "Gestion des Fournisseurs");
        return "referentiel/fournisseurs/liste";
    }
    
    @GetMapping("/nouveau")
    public String nouveauFournisseur(Model model) {
        model.addAttribute("fournisseur", new Fournisseur());
        model.addAttribute("titre", "Nouveau Fournisseur");
        model.addAttribute("action", "Créer");
        return "referentiel/fournisseurs/formulaire";
    }
    
    @PostMapping
    public String creerFournisseur(@ModelAttribute Fournisseur fournisseur, RedirectAttributes redirectAttributes) {
        try {
            fournisseurService.creerFournisseur(fournisseur);
            redirectAttributes.addFlashAttribute("message", "Fournisseur créé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/fournisseurs";
    }
    
    @GetMapping("/{id}/modifier")
    public String modifierFournisseur(@PathVariable Long id, Model model) {
        model.addAttribute("fournisseur", fournisseurService.trouverParId(id));
        model.addAttribute("titre", "Modifier Fournisseur");
        model.addAttribute("action", "Modifier");
        return "referentiel/fournisseurs/formulaire";
    }
    
    @PostMapping("/{id}")
    public String mettreAJourFournisseur(@PathVariable Long id, @ModelAttribute Fournisseur fournisseur, 
                                          RedirectAttributes redirectAttributes) {
        try {
            fournisseurService.modifierFournisseur(id, fournisseur);
            redirectAttributes.addFlashAttribute("message", "Fournisseur modifié avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/fournisseurs";
    }
    
    @GetMapping("/{id}/supprimer")
    public String supprimerFournisseur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fournisseurService.supprimerFournisseur(id);
            redirectAttributes.addFlashAttribute("message", "Fournisseur supprimé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/fournisseurs";
    }
}
