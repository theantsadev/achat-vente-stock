package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Site;

import java.util.Optional;

/**
 * TODO.YML Ligne 5, 71: Référentiels > Sites
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    
    Optional<Site> findByCode(String code);
}
