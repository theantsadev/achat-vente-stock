package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByLogin(String login);
    
    List<Utilisateur> findByActif(Boolean actif);
    
    List<Utilisateur> findByServiceId(Long serviceId);
    
    List<Utilisateur> findBySiteId(Long siteId);
}
