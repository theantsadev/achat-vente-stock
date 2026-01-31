package com.gestion.achat_vente_stock.achat.controller;

import com.gestion.achat_vente_stock.achat.model.PaiementFournisseur;
import com.gestion.achat_vente_stock.achat.service.FactureFournisseurService;
import com.gestion.achat_vente_stock.achat.service.PaiementFournisseurService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Ligne 19: Achats > Paiement Fournisseur - Contrôleur web
 */
@Controller
@RequestMapping("/achats/paiements")
@RequiredArgsConstructor
public class PaiementFournisseurController {

    private final PaiementFournisseurService paiementFournisseurService;
    private final FactureFournisseurService factureFournisseurService;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * TODO.YML Ligne 19: Liste des paiements
     */
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("paiements", paiementFournisseurService.listerTous());
        return "achats/paiements/liste";
    }

    /**
     * TODO.YML Ligne 19: Formulaire nouveau paiement
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(@RequestParam(required = false) Long factureId, Model model) {
        PaiementFournisseur paiement = new PaiementFournisseur();

        if (factureId != null) {
            paiement.setFactureFournisseur(factureFournisseurService.trouverParId(factureId));
        }

        model.addAttribute("paiement", paiement);
        model.addAttribute("factures", factureFournisseurService.listerTous());
        return "achats/paiements/formulaire";
    }

    /**
     * TODO.YML Ligne 18-19: Créer paiement (vérifie blocage)
     */
    @PostMapping
    public String creer(@ModelAttribute PaiementFournisseur paiement,
            RedirectAttributes redirectAttributes) {
        Utilisateur tresorier = utilisateurRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            PaiementFournisseur created = paiementFournisseurService.creerPaiement(paiement, tresorier);
            redirectAttributes.addFlashAttribute("success", "Paiement créé");
            return "redirect:/achats/paiements/" + created.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/achats/paiements/nouveau";
        }
    }

    /**
     * Détail paiement
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("paiement", paiementFournisseurService.trouverParId(id));
        return "achats/paiements/detail";
    }

    /**
     * TODO.YML Ligne 19: Exécuter paiement
     */
    @PostMapping("/{id}/executer")
    public String executer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur tresorier = utilisateurRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            paiementFournisseurService.executerPaiement(id, tresorier);
            redirectAttributes.addFlashAttribute("success", "Paiement exécuté");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/paiements/" + id;
    }

    /**
     * Annuler paiement
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id,
            @RequestParam String motif,
            RedirectAttributes redirectAttributes) {
        Utilisateur tresorier = utilisateurRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            paiementFournisseurService.annulerPaiement(id, motif, tresorier);
            redirectAttributes.addFlashAttribute("success", "Paiement annulé");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/paiements/" + id;
    }
}
