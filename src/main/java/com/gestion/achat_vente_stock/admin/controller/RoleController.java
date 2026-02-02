package com.gestion.achat_vente_stock.admin.controller;

import com.gestion.achat_vente_stock.admin.model.Role;
import com.gestion.achat_vente_stock.admin.model.Permission;
import com.gestion.achat_vente_stock.admin.model.RolePermission;
import com.gestion.achat_vente_stock.admin.repository.RolePermissionRepository;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.admin.service.PermissionService;
import com.gestion.achat_vente_stock.admin.service.RoleService;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * TODO.YML Ligne 55, 69: Admin > Rôles et Permissions
 * Contrôleur pour la gestion des rôles et permissions
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final RolePermissionRepository rolePermissionRepository;
    private final SessionService sessionService;
    private final AuditService auditService;

    /**
     * Liste des rôles
     */
    @GetMapping
    public String liste(Model model) {
        model.addAttribute("roles", roleService.listerTous());
        return "admin/roles/liste";
    }

    /**
     * Détail d'un rôle avec ses permissions
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Role role = roleService.trouverParId(id);
        List<Permission> permissionsRole = permissionService.getPermissionsRole(id);
        List<Permission> toutesPermissions = permissionService.listerTous();

        // Extraire les modules uniques pour grouper les permissions par module
        List<String> modules = toutesPermissions.stream()
                .map(Permission::getModule)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("role", role);
        model.addAttribute("permissionsRole", permissionsRole);
        model.addAttribute("toutesPermissions", toutesPermissions);
        model.addAttribute("modules", modules);

        return "admin/roles/detail";
    }

    /**
     * Formulaire de création de rôle
     */
    @GetMapping("/nouveau")
    public String formulaireNouveau(Model model) {
        model.addAttribute("role", new Role());
        return "admin/roles/formulaire";
    }

    /**
     * Formulaire de modification de rôle
     */
    @GetMapping("/{id}/modifier")
    public String formulaireModifier(@PathVariable Long id, Model model) {
        Role role = roleService.trouverParId(id);
        model.addAttribute("role", role);
        return "admin/roles/formulaire";
    }

    /**
     * Enregistrer un rôle
     */
    @PostMapping("/enregistrer")
    public String enregistrer(@ModelAttribute Role role, RedirectAttributes redirectAttributes) {
        try {
            if (role.getId() == null) {
                role = roleService.creerRole(role);

                auditService.logAction(sessionService.getUtilisateurConnecte(),
                        "role", role.getId(), "CREATE", null, role.getCode(), null);

                redirectAttributes.addFlashAttribute("success", "Rôle créé avec succès");
            } else {
                role = roleService.modifierRole(role.getId(), role);

                auditService.logAction(sessionService.getUtilisateurConnecte(),
                        "role", role.getId(), "UPDATE", null, role.getCode(), null);

                redirectAttributes.addFlashAttribute("success", "Rôle modifié avec succès");
            }
            return "redirect:/admin/roles/" + role.getId();

        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du rôle", e);
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/admin/roles";
        }
    }

    /**
     * Ajouter une permission à un rôle
     */
    @PostMapping("/{roleId}/permissions/ajouter")
    public String ajouterPermission(@PathVariable Long roleId,
            @RequestParam Long permissionId,
            RedirectAttributes redirectAttributes) {
        try {
            Role role = roleService.trouverParId(roleId);
            Permission permission = permissionService.listerTous().stream()
                    .filter(p -> p.getId().equals(permissionId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Permission non trouvée"));

            // Vérifier si le lien existe déjà
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(permission);
                rolePermissionRepository.save(rp);

                auditService.logAction(sessionService.getUtilisateurConnecte(),
                        "role_permission", rp.getId(), "CREATE",
                        null, role.getCode() + " + " + permission.getCode(), null);

                redirectAttributes.addFlashAttribute("success", "Permission ajoutée");
            } else {
                redirectAttributes.addFlashAttribute("warning", "Cette permission est déjà attribuée à ce rôle");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/roles/" + roleId;
    }

    /**
     * Supprimer une permission d'un rôle
     */
    @PostMapping("/{roleId}/permissions/{permissionId}/supprimer")
    public String supprimerPermission(@PathVariable Long roleId,
            @PathVariable Long permissionId,
            RedirectAttributes redirectAttributes) {
        try {
            RolePermission rp = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                    .orElseThrow(() -> new RuntimeException("Lien non trouvé"));

            auditService.logAction(sessionService.getUtilisateurConnecte(),
                    "role_permission", rp.getId(), "DELETE",
                    rp.getRole().getCode() + " + " + rp.getPermission().getCode(), null, null);

            rolePermissionRepository.delete(rp);

            redirectAttributes.addFlashAttribute("success", "Permission retirée");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/roles/" + roleId;
    }

    /**
     * Liste des permissions
     */
    @GetMapping("/permissions")
    public String listePermissions(Model model) {
        model.addAttribute("permissions", permissionService.listerTous());
        return "admin/permissions/liste";
    }
}
