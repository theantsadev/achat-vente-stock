package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Tarif;

import java.util.Optional;

/**
 * TODO.YML Ligne 6: Référentiels > Tarifs
 */
@Repository
public interface TarifRepository extends JpaRepository<Tarif, Long> {
    
    Optional<Tarif> findByCode(String code);
}
