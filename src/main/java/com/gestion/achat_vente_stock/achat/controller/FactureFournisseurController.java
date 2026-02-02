package com.gestion.achat_vente_stock.achat.controller;

import com.gestion.achat_vente_stock.achat.model.FactureFournisseur;
import com.gestion.achat_vente_stock.achat.service.BonCommandeService;
import com.gestion.achat_vente_stock.achat.service.FactureFournisseurService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

/**
 * TODO.YML Lignes 17-18: Achats > Facture Fournisseur - Contrôleur web
 */
@Controller
@RequestMapping("/achats/factures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-FOUR', 'ROLE-DAF', 'ROLE-ADMIN')")
public class FactureFournisseurController {

    private final FactureFournisseurService factureFournisseurService;
    private final BonCommandeService bonCommandeService;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SessionService sessionService;

    /**
     * TODO.YML Ligne 17: Liste des factures
     */
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("factures", factureFournisseurService.listerTous());
        return "achats/factures/liste";
    }

    /**
     * TODO.YML Ligne 17: Liste factures bloquées
     */
    @GetMapping("/bloquees")
    public String listerBloquees(Model model) {
        model.addAttribute("factures", factureFournisseurService.listerFacturesBloquees());
        model.addAttribute("titre", "Factures bloquées (écarts 3-way match)");
        return "achats/factures/liste";
    }

    /**
     * TODO.YML Ligne 17: Formulaire nouvelle facture
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(Model model) {
        model.addAttribute("facture", new FactureFournisseur());
        model.addAttribute("bonsCommande", bonCommandeService.listerTous());
        model.addAttribute("fournisseurs", fournisseurRepository.findAll());
        return "achats/factures/formulaire";
    }

    /**
     * TODO.YML Ligne 17: Créer facture avec 3-way match
     */
    @PostMapping
    public String creer(@ModelAttribute FactureFournisseur facture,
            RedirectAttributes redirectAttributes) {
        FactureFournisseur created = factureFournisseurService.creerFactureFournisseur(
                facture, new ArrayList<>());

        redirectAttributes.addFlashAttribute("success", "Facture créée");
        if (!created.getThreeWayMatchOk()) {
            redirectAttributes.addFlashAttribute("warning",
                    "Attention: Écarts détectés - " + created.getEcartsThreeWay());
        }

        return "redirect:/achats/factures/" + created.getId();
    }

    /**
     * TODO.YML Ligne 17-18: Détail facture avec résultat 3-way match
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        FactureFournisseur facture = factureFournisseurService.trouverParId(id);
        model.addAttribute("facture", facture);
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "achats/factures/detail";
    }

    /**
     * TODO.YML Ligne 17: Relancer 3-way match
     */
    @PostMapping("/{id}/three-way-match")
    public String relancerThreeWayMatch(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        factureFournisseurService.effectuerThreeWayMatch(id);
        redirectAttributes.addFlashAttribute("success", "3-way match effectué");
        return "redirect:/achats/factures/" + id;
    }

    /**
     * TODO.YML Ligne 58: Valider facture (séparation des tâches)
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-FOUR', 'ROLE-DAF', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        // Utiliser l'utilisateur connecté comme valideur
        Utilisateur valideur = sessionService.getUtilisateurConnecte();

        try {
            factureFournisseurService.validerFacture(id, valideur);
            redirectAttributes.addFlashAttribute("success", "Facture validée");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/factures/" + id;
    }

    /**
     * TODO.YML Ligne 18: Débloquer facture
     */
    @PostMapping("/{id}/debloquer")
    @PreAuthorize("hasAnyAuthority('ROLE-DAF', 'ROLE-ADMIN')")
    public String debloquer(@PathVariable Long id,
            @RequestParam String justification,
            RedirectAttributes redirectAttributes) {
        // Utiliser l'utilisateur connecté pour le déblocage
        Utilisateur valideur = sessionService.getUtilisateurConnecte();

        try {
            factureFournisseurService.debloquerFacture(id, justification, valideur);
            redirectAttributes.addFlashAttribute("success", "Facture débloquée");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/factures/" + id;
    }
}
