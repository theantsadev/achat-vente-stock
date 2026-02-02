package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 56: Sécurité > ABAC
 * Utilisateurs avec restrictions contextuelles
 */
@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {
    
    List<UtilisateurRole> findByUtilisateurId(Long utilisateurId);
    
    List<UtilisateurRole> findByRoleId(Long roleId);
    
    @Query("SELECT ur FROM UtilisateurRole ur WHERE ur.utilisateur.id = :userId " +
           "AND (ur.dateFin IS NULL OR ur.dateFin > CURRENT_TIMESTAMP)")
    List<UtilisateurRole> findActiveRolesByUserId(@Param("userId") Long userId);
    
    /**
     * Trouver les rôles d'un utilisateur par code de rôle
     */
    @Query("SELECT ur FROM UtilisateurRole ur " +
           "JOIN ur.role r " +
           "WHERE ur.utilisateur.id = :userId AND r.code = :roleCode " +
           "AND (ur.dateDebut IS NULL OR ur.dateDebut <= CURRENT_TIMESTAMP) " +
           "AND (ur.dateFin IS NULL OR ur.dateFin > CURRENT_TIMESTAMP)")
    List<UtilisateurRole> findByUtilisateurIdAndRoleCode(@Param("userId") Long userId, @Param("roleCode") String roleCode);
    
    /**
     * Vérifier si un utilisateur a un rôle actif sur un dépôt
     */
    @Query("SELECT COUNT(ur) > 0 FROM UtilisateurRole ur " +
           "WHERE ur.utilisateur.id = :userId " +
           "AND (ur.depot.id = :depotId OR ur.depot IS NULL) " +
           "AND (ur.dateDebut IS NULL OR ur.dateDebut <= CURRENT_TIMESTAMP) " +
           "AND (ur.dateFin IS NULL OR ur.dateFin > CURRENT_TIMESTAMP)")
    boolean hasAccessToDepot(@Param("userId") Long userId, @Param("depotId") Long depotId);
    
    /**
     * Vérifier si un utilisateur a un rôle actif sur un site
     */
    @Query("SELECT COUNT(ur) > 0 FROM UtilisateurRole ur " +
           "WHERE ur.utilisateur.id = :userId " +
           "AND (ur.site.id = :siteId OR ur.site IS NULL) " +
           "AND (ur.dateDebut IS NULL OR ur.dateDebut <= CURRENT_TIMESTAMP) " +
           "AND (ur.dateFin IS NULL OR ur.dateFin > CURRENT_TIMESTAMP)")
    boolean hasAccessToSite(@Param("userId") Long userId, @Param("siteId") Long siteId);
}
