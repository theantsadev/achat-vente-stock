package com.gestion.achat_vente_stock.admin.service;

import com.gestion.achat_vente_stock.admin.model.Role;
import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import com.gestion.achat_vente_stock.admin.repository.RoleRepository;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO.YML Ligne 55: Sécurité > RBAC > Rôles
 * Service de gestion des rôles
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;

    /**
     * Lister tous les rôles
     */
    @Transactional(readOnly = true)
    public List<Role> listerTous() {
        return roleRepository.findAll();
    }

    /**
     * Trouver un rôle par ID
     */
    @Transactional(readOnly = true)
    public Role trouverParId(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + id));
    }

    /**
     * Trouver un rôle par code
     */
    @Transactional(readOnly = true)
    public Role trouverParCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + code));
    }

    /**
     * Créer un nouveau rôle
     */
    public Role creerRole(Role role) {
        // Vérifier que le code est unique
        if (roleRepository.findByCode(role.getCode()).isPresent()) {
            throw new RuntimeException("Un rôle avec ce code existe déjà: " + role.getCode());
        }
        return roleRepository.save(role);
    }

    /**
     * Modifier un rôle existant
     */
    public Role modifierRole(Long id, Role role) {
        Role existant = trouverParId(id);
        existant.setLibelle(role.getLibelle());
        existant.setDescription(role.getDescription());
        // Le code ne doit pas être modifié
        return roleRepository.save(existant);
    }

    /**
     * Supprimer un rôle
     * Note: vérifie qu'aucun utilisateur n'est associé
     */
    public void supprimerRole(Long id) {
        Role role = trouverParId(id);
        // La contrainte FK empêchera la suppression si des utilisateurs sont associés
        roleRepository.delete(role);
    }

    /**
     * Lister les rôles actifs d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Role> listerRolesUtilisateur(Long utilisateurId) {
        return utilisateurRoleRepository.findActiveRolesByUserId(utilisateurId)
                .stream()
                .map(UtilisateurRole::getRole)
                .distinct()
                .toList();
    }
}
