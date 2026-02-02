package com.gestion.achat_vente_stock.admin.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRoleRepository;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.admin.service.RoleService;
import com.gestion.achat_vente_stock.admin.service.UtilisateurService;
import com.gestion.achat_vente_stock.admin.repository.ServiceRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.referentiel.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 * Contrôleur pour la gestion des utilisateurs
 */
@Controller
@RequestMapping("/admin/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final RoleService roleService;
    private final ServiceRepository serviceRepository;
    private final SiteRepository siteRepository;
    private final DepotRepository depotRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final AuditService auditService;

    /**
     * Liste des utilisateurs
     */
    @GetMapping
    public String liste(Model model,
                       @RequestParam(required = false) Long serviceId,
                       @RequestParam(required = false) Long siteId,
                       @RequestParam(required = false) Boolean actif) {
        
        List<Utilisateur> utilisateurs;
        
        if (actif != null) {
            utilisateurs = utilisateurService.listerActifs();
        } else {
            utilisateurs = utilisateurService.listerTous();
        }
        
        // Filtrer par service ou site si spécifié
        if (serviceId != null) {
            utilisateurs = utilisateurs.stream()
                    .filter(u -> u.getService() != null && u.getService().getId().equals(serviceId))
                    .toList();
        }
        
        if (siteId != null) {
            utilisateurs = utilisateurs.stream()
                    .filter(u -> u.getSite() != null && u.getSite().getId().equals(siteId))
                    .toList();
        }
        
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("sites", siteRepository.findAll());
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("siteId", siteId);
        model.addAttribute("actif", actif);
        
        return "admin/utilisateurs/liste";
    }

    /**
     * Formulaire de création d'utilisateur
     */
    @GetMapping("/nouveau")
    public String formulaireNouveau(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("sites", siteRepository.findAll());
        model.addAttribute("managers", utilisateurService.listerActifs());
        return "admin/utilisateurs/formulaire";
    }

    /**
     * Formulaire de modification d'utilisateur
     */
    @GetMapping("/{id}/modifier")
    public String formulaireModifier(@PathVariable Long id, Model model) {
        Utilisateur utilisateur = utilisateurService.trouverParId(id);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("sites", siteRepository.findAll());
        model.addAttribute("managers", utilisateurService.listerActifs());
        return "admin/utilisateurs/formulaire";
    }

    /**
     * Enregistrer un utilisateur (création ou modification)
     */
    @PostMapping("/enregistrer")
    public String enregistrer(@ModelAttribute Utilisateur utilisateur,
                             @RequestParam(required = false) String nouveauMotDePasse,
                             RedirectAttributes redirectAttributes) {
        try {
            Utilisateur currentUser = sessionService.getUtilisateurConnecte();
            
            if (utilisateur.getId() == null) {
                // Création
                if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
                    nouveauMotDePasse = "password123"; // Mot de passe par défaut
                }
                utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
                utilisateur.setActif(true);
                utilisateur = utilisateurService.creerUtilisateur(utilisateur);
                
                auditService.logAction(currentUser, "utilisateur", utilisateur.getId(), 
                        "CREATE", null, utilisateur.getLogin(), null);
                
                redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès");
            } else {
                // Modification
                Utilisateur existant = utilisateurService.trouverParId(utilisateur.getId());
                
                // Mettre à jour le mot de passe si fourni
                if (nouveauMotDePasse != null && !nouveauMotDePasse.isBlank()) {
                    utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
                } else {
                    utilisateur.setPassword(existant.getPassword());
                }
                
                utilisateur = utilisateurService.modifierUtilisateur(utilisateur.getId(), utilisateur);
                
                auditService.logAction(currentUser, "utilisateur", utilisateur.getId(), 
                        "UPDATE", null, utilisateur.getLogin(), null);
                
                redirectAttributes.addFlashAttribute("success", "Utilisateur modifié avec succès");
            }
            
            return "redirect:/admin/utilisateurs/" + utilisateur.getId();
            
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'utilisateur", e);
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/utilisateurs";
        }
    }

    /**
     * Détail d'un utilisateur avec ses rôles
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Utilisateur utilisateur = utilisateurService.trouverParId(id);
        List<UtilisateurRole> roles = utilisateurRoleRepository.findByUtilisateurId(id);
        
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("rolesUtilisateur", roles);
        model.addAttribute("rolesDisponibles", roleService.listerTous());
        model.addAttribute("sites", siteRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        
        return "admin/utilisateurs/detail";
    }

    /**
     * Désactiver un utilisateur
     */
    @PostMapping("/{id}/desactiver")
    public String desactiver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur currentUser = sessionService.getUtilisateurConnecte();
            utilisateurService.desactiverUtilisateur(id);
            
            auditService.logAction(currentUser, "utilisateur", id, 
                    "DISABLE", "actif=true", "actif=false", null);
            
            redirectAttributes.addFlashAttribute("success", "Utilisateur désactivé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs/" + id;
    }

    /**
     * Réactiver un utilisateur
     */
    @PostMapping("/{id}/activer")
    public String activer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.trouverParId(id);
            utilisateur.setActif(true);
            utilisateurService.modifierUtilisateur(id, utilisateur);
            
            Utilisateur currentUser = sessionService.getUtilisateurConnecte();
            auditService.logAction(currentUser, "utilisateur", id, 
                    "ENABLE", "actif=false", "actif=true", null);
            
            redirectAttributes.addFlashAttribute("success", "Utilisateur réactivé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs/" + id;
    }

    /**
     * Ajouter un rôle à un utilisateur
     */
    @PostMapping("/{id}/roles/ajouter")
    public String ajouterRole(@PathVariable Long id,
                             @RequestParam Long roleId,
                             @RequestParam(required = false) Long siteId,
                             @RequestParam(required = false) Long depotId,
                             @RequestParam(required = false) BigDecimal montantMax,
                             @RequestParam(required = false) String dateDebut,
                             @RequestParam(required = false) String dateFin,
                             RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.trouverParId(id);
            
            UtilisateurRole ur = new UtilisateurRole();
            ur.setUtilisateur(utilisateur);
            ur.setRole(roleService.trouverParId(roleId));
            
            if (siteId != null) {
                ur.setSite(siteRepository.findById(siteId).orElse(null));
            }
            if (depotId != null) {
                ur.setDepot(depotRepository.findById(depotId).orElse(null));
            }
            ur.setMontantMax(montantMax);
            
            if (dateDebut != null && !dateDebut.isBlank()) {
                ur.setDateDebut(LocalDateTime.parse(dateDebut + "T00:00:00"));
            } else {
                ur.setDateDebut(LocalDateTime.now());
            }
            
            if (dateFin != null && !dateFin.isBlank()) {
                ur.setDateFin(LocalDateTime.parse(dateFin + "T23:59:59"));
            }
            
            utilisateurRoleRepository.save(ur);
            
            Utilisateur currentUser = sessionService.getUtilisateurConnecte();
            auditService.logAction(currentUser, "utilisateur_role", ur.getId(), 
                    "CREATE", null, "Role " + ur.getRole().getCode() + " ajouté à " + utilisateur.getLogin(), null);
            
            redirectAttributes.addFlashAttribute("success", "Rôle ajouté avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du rôle", e);
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs/" + id;
    }

    /**
     * Supprimer un rôle d'un utilisateur
     */
    @PostMapping("/{id}/roles/{roleId}/supprimer")
    public String supprimerRole(@PathVariable Long id, 
                               @PathVariable Long roleId,
                               RedirectAttributes redirectAttributes) {
        try {
            UtilisateurRole ur = utilisateurRoleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
            
            Utilisateur currentUser = sessionService.getUtilisateurConnecte();
            auditService.logAction(currentUser, "utilisateur_role", roleId, 
                    "DELETE", ur.getRole().getCode(), null, null);
            
            utilisateurRoleRepository.delete(ur);
            
            redirectAttributes.addFlashAttribute("success", "Rôle supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs/" + id;
    }
}
