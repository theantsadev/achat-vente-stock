package com.gestion.achat_vente_stock.admin.controller;

import com.gestion.achat_vente_stock.admin.model.Delegation;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.DelegationService;
import com.gestion.achat_vente_stock.admin.service.RoleService;
import com.gestion.achat_vente_stock.admin.service.UtilisateurService;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 61: Sécurité > Délégation
 * Contrôleur pour la gestion des délégations
 */
@Controller
@RequestMapping("/admin/delegations")
@RequiredArgsConstructor
@Slf4j
public class DelegationController {

    private final DelegationService delegationService;
    private final UtilisateurService utilisateurService;
    private final RoleService roleService;
    private final SessionService sessionService;

    /**
     * Liste des délégations
     */
    @GetMapping
    public String liste(Model model) {
        Utilisateur currentUser = sessionService.getUtilisateurConnecte();
        
        List<Delegation> delegationsDonnees = delegationService.listerDelegationsDonnees(currentUser.getId());
        List<Delegation> delegationsRecues = delegationService.listerDelegationsRecues(currentUser.getId());
        
        model.addAttribute("delegationsDonnees", delegationsDonnees);
        model.addAttribute("delegationsRecues", delegationsRecues);
        
        // Pour les admins, afficher toutes les délégations
        if (sessionService.hasRole("ROLE-ADMIN")) {
            model.addAttribute("toutesDelegations", delegationService.listerToutes());
        }
        
        return "admin/delegations/liste";
    }

    /**
     * Formulaire de création de délégation
     */
    @GetMapping("/nouvelle")
    public String formulaireNouvelle(Model model) {
        Utilisateur currentUser = sessionService.getUtilisateurConnecte();
        
        // Utilisateurs actifs (sauf l'utilisateur courant)
        model.addAttribute("utilisateurs", utilisateurService.listerActifs().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .toList());
        
        // Les rôles que l'utilisateur possède et peut déléguer
        model.addAttribute("mesRoles", roleService.listerRolesUtilisateur(currentUser.getId()));
        
        return "admin/delegations/formulaire";
    }

    /**
     * Créer une nouvelle délégation
     */
    @PostMapping
    public String creer(@RequestParam Long delegataireId,
                       @RequestParam Long roleId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                       @RequestParam String justification,
                       RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime debut = dateDebut.atStartOfDay();
            LocalDateTime fin = dateFin != null ? dateFin.atTime(23, 59, 59) : null;
            
            Delegation delegation = delegationService.creerDelegation(delegataireId, roleId, debut, fin, justification);
            
            redirectAttributes.addFlashAttribute("success", "Délégation créée avec succès");
            return "redirect:/admin/delegations";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de la délégation", e);
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/delegations/nouvelle";
        }
    }

    /**
     * Détail d'une délégation
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Delegation delegation = delegationService.trouverParId(id);
        model.addAttribute("delegation", delegation);
        return "admin/delegations/detail";
    }

    /**
     * Annuler une délégation
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            delegationService.annulerDelegation(id);
            redirectAttributes.addFlashAttribute("success", "Délégation annulée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/delegations";
    }
}
