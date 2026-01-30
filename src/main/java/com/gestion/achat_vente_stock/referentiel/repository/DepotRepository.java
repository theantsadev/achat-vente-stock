package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Depot;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 5: Référentiels > Dépôts
 */
@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    
    Optional<Depot> findByCode(String code);
    
    List<Depot> findBySiteId(Long siteId);
    
    List<Depot> findByActif(Boolean actif);
}
