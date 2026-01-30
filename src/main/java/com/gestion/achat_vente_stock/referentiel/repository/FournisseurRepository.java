package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 3: Référentiels > Fournisseurs
 */
@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    
    Optional<Fournisseur> findByCode(String code);
    
    List<Fournisseur> findByStatut(String statut);
}
