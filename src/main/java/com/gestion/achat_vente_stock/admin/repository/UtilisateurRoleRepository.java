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
}
