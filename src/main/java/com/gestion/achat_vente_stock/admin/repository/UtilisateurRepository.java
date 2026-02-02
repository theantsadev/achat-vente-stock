package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByLogin(String login);
    
    /**
     * Charge un utilisateur avec ses rôles (pour l'authentification)
     * Utilise JOIN FETCH pour éviter le problème N+1
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.roles ur " +
           "LEFT JOIN FETCH ur.role " +
           "WHERE u.login = :login")
    Optional<Utilisateur> findByLoginWithRoles(@Param("login") String login);
    
    /**
     * Charge un utilisateur par ID avec ses rôles
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.roles ur " +
           "LEFT JOIN FETCH ur.role " +
           "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithRoles(@Param("id") Long id);
    
    List<Utilisateur> findByActif(Boolean actif);
    
    List<Utilisateur> findByServiceId(Long serviceId);
    
    List<Utilisateur> findBySiteId(Long siteId);
    
    /**
     * Recherche par nom ou prénom
     */
    @Query("SELECT u FROM Utilisateur u WHERE " +
           "LOWER(u.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(u.login) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Utilisateur> rechercher(@Param("terme") String terme);
}
