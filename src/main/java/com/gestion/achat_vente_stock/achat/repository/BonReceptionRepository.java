package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.BonReception;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 14-16: Achats > Réception
 */
@Repository
public interface BonReceptionRepository extends JpaRepository<BonReception, Long> {
    
    Optional<BonReception> findByNumero(String numero);
    
    List<BonReception> findByBonCommandeId(Long bonCommandeId);
    
    List<BonReception> findByMagasinierId(Long magasinierId);
    
    // TODO.YML Ligne 16: Réceptions partielles
    List<BonReception> findByStatut(String statut);
}
