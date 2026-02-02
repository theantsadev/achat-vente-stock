package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 55, 69: Sécurité > Permissions
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByCode(String code);
    
    List<Permission> findByModule(String module);
    
    /**
     * Trouver les permissions d'un rôle
     */
    @Query("SELECT p FROM Permission p " +
           "JOIN RolePermission rp ON rp.permission.id = p.id " +
           "WHERE rp.role.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Lister toutes les permissions avec leurs rôles associés
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "LEFT JOIN FETCH p.rolePermissions rp " +
           "LEFT JOIN FETCH rp.role")
    List<Permission> findAllWithRoles();
    
    /**
     * Vérifier si un rôle a une permission
     */
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp " +
           "WHERE rp.role.id = :roleId AND rp.permission.code = :permissionCode")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("permissionCode") String permissionCode);
}
