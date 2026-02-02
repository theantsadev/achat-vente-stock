package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la table de liaison role_permission
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    List<RolePermission> findByRoleId(Long roleId);
    
    List<RolePermission> findByPermissionId(Long permissionId);
    
    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);
    
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
    
    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
