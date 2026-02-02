package com.gestion.achat_vente_stock.admin.service;

import com.gestion.achat_vente_stock.admin.model.Permission;
import com.gestion.achat_vente_stock.admin.model.Role;
import com.gestion.achat_vente_stock.admin.model.RolePermission;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import com.gestion.achat_vente_stock.admin.repository.PermissionRepository;
import com.gestion.achat_vente_stock.admin.repository.RoleRepository;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRoleRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO.YML Ligne 55-58: Sécurité > RBAC/ABAC > Permissions
 * Service de gestion des permissions et vérification des droits
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final SessionService sessionService;

    /**
     * Lister toutes les permissions avec leurs rôles associés
     */
    public List<Permission> listerTous() {
        return permissionRepository.findAllWithRoles();
    }

    /**
     * Lister les permissions par module
     */
    public List<Permission> listerParModule(String module) {
        return permissionRepository.findByModule(module);
    }

    /**
     * Trouver une permission par code
     */
    public Permission trouverParCode(String code) {
        return permissionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée: " + code));
    }

    /**
     * Obtenir les permissions d'un rôle
     */
    public List<Permission> getPermissionsRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleId));
        
        return permissionRepository.findByRoleId(roleId);
    }

    /**
     * Obtenir toutes les permissions de l'utilisateur connecté
     */
    public List<Permission> getPermissionsUtilisateurConnecte() {
        Utilisateur user = sessionService.getUtilisateurConnecte();
        if (user == null) return List.of();
        
        return getPermissionsUtilisateur(user.getId());
    }

    /**
     * Obtenir toutes les permissions d'un utilisateur
     */
    public List<Permission> getPermissionsUtilisateur(Long utilisateurId) {
        List<UtilisateurRole> rolesActifs = utilisateurRoleRepository.findActiveRolesByUserId(utilisateurId);
        
        return rolesActifs.stream()
                .filter(this::isRoleActif)
                .flatMap(ur -> permissionRepository.findByRoleId(ur.getRole().getId()).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Vérifier si l'utilisateur connecté a une permission
     */
    public boolean hasPermission(String permissionCode) {
        Utilisateur user = sessionService.getUtilisateurConnecte();
        if (user == null) return false;
        
        // Admin a toutes les permissions
        if (sessionService.hasRole("ROLE-ADMIN")) return true;
        
        return hasPermission(user.getId(), permissionCode);
    }

    /**
     * Vérifier si un utilisateur a une permission
     */
    public boolean hasPermission(Long utilisateurId, String permissionCode) {
        List<UtilisateurRole> rolesActifs = utilisateurRoleRepository.findActiveRolesByUserId(utilisateurId);
        
        return rolesActifs.stream()
                .filter(this::isRoleActif)
                .anyMatch(ur -> permissionRepository.hasPermission(ur.getRole().getId(), permissionCode));
    }

    /**
     * Vérifier si l'utilisateur connecté peut effectuer une action sur un module
     */
    public boolean canPerformAction(String module, String action) {
        Utilisateur user = sessionService.getUtilisateurConnecte();
        if (user == null) return false;
        
        // Admin peut tout faire
        if (sessionService.hasRole("ROLE-ADMIN")) return true;
        
        List<Permission> permissions = getPermissionsUtilisateurConnecte();
        return permissions.stream()
                .anyMatch(p -> module.equals(p.getModule()) && action.equals(p.getAction()));
    }

    /**
     * Vérifie si un rôle utilisateur est actuellement actif
     */
    private boolean isRoleActif(UtilisateurRole ur) {
        LocalDateTime now = LocalDateTime.now();
        
        if (ur.getDateDebut() != null && now.isBefore(ur.getDateDebut())) {
            return false;
        }
        
        if (ur.getDateFin() != null && now.isAfter(ur.getDateFin())) {
            return false;
        }
        
        return true;
    }
}
