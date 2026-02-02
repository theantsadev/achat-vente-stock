package com.gestion.achat_vente_stock.achat.controller;

import com.gestion.achat_vente_stock.achat.model.BonCommande;
import com.gestion.achat_vente_stock.achat.service.BonCommandeService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Lignes 11-13: Achats > Bon Commande - Contrôleur web
 */
@Controller
@RequestMapping("/achats/bons-commande")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-ACHETEUR', 'ROLE-RESP-ACHATS', 'ROLE-DAF', 'ROLE-ADMIN')")
public class BonCommandeController {

    private final BonCommandeService bonCommandeService;
    private final UtilisateurRepository utilisateurRepository;
    private final SessionService sessionService;

    /**
     * TODO.YML Ligne 11: Liste des bons de commande
     */
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("bonsCommande", bonCommandeService.listerTous());
        return "achats/bons-commande/liste";
    }

    /**
     * TODO.YML Ligne 11: Créer BC depuis Proforma
     */
    @PostMapping("/depuis-proforma/{proformaId}")
    @PreAuthorize("hasAnyAuthority('ROLE-ACHETEUR', 'ROLE-RESP-ACHATS', 'ROLE-ADMIN')")
    public String creerDepuisProforma(@PathVariable Long proformaId, RedirectAttributes redirectAttributes) {
        Utilisateur acheteur = sessionService.getUtilisateurConnecte();

        try {
            BonCommande bc = bonCommandeService.creerBonCommandeDepuisProforma(proformaId, acheteur);
            redirectAttributes.addFlashAttribute("success", "Bon de commande créé depuis la Pro-forma");
            return "redirect:/achats/bons-commande/" + bc.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/achats/pro-formas/" + proformaId;
        }
    }

    /**
     * TODO.YML Ligne 11-13: Détail BC
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BonCommande bc = bonCommandeService.trouverParId(id);
        model.addAttribute("bonCommande", bc);
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "achats/bons-commande/detail";
    }

    /**
     * TODO.YML Ligne 12: Soumettre pour validation
     */
    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE-ACHETEUR', 'ROLE-ADMIN')")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur acheteur = sessionService.getUtilisateurConnecte();

        bonCommandeService.soumettrePourValidation(id, acheteur);
        redirectAttributes.addFlashAttribute("success", "BC soumis pour validation");
        return "redirect:/achats/bons-commande/" + id;
    }

    /**
     * TODO.YML Ligne 12: Valider BC (responsable achats)
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-RESP-ACHATS', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id,
            @RequestParam boolean approuve,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        Utilisateur responsable = sessionService.getUtilisateurConnecte();

        try {
            bonCommandeService.validerBonCommande(id, responsable, approuve, commentaire);
            redirectAttributes.addFlashAttribute("success", approuve ? "BC validé" : "BC rejeté");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/bons-commande/" + id;
    }

    /**
     * TODO.YML Ligne 13: Approuver pour signature (DG/DAF)
     */
    @PostMapping("/{id}/approuver")
    @PreAuthorize("hasAnyAuthority('ROLE-DAF', 'ROLE-ADMIN')")
    public String approuver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur approbateur = sessionService.getUtilisateurConnecte();

        try {
            bonCommandeService.approuverPourSignature(id, approbateur);
            redirectAttributes.addFlashAttribute("success", "BC approuvé pour signature");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/bons-commande/" + id;
    }

    /**
     * Envoyer au fournisseur
     */
    @PostMapping("/{id}/envoyer")
    @PreAuthorize("hasAnyAuthority('ROLE-ACHETEUR', 'ROLE-RESP-ACHATS', 'ROLE-ADMIN')")
    public String envoyer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur acheteur = sessionService.getUtilisateurConnecte();

        try {
            bonCommandeService.envoyerBonCommande(id, acheteur);
            redirectAttributes.addFlashAttribute("success", "BC envoyé au fournisseur");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/bons-commande/" + id;
    }
}
