package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.FactureFournisseur;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 17-18: Achats > Facture Fournisseur
 */
@Repository
public interface FactureFournisseurRepository extends JpaRepository<FactureFournisseur, Long> {
    
    Optional<FactureFournisseur> findByNumero(String numero);
    
    List<FactureFournisseur> findByFournisseurId(Long fournisseurId);
    
    List<FactureFournisseur> findByBonCommandeId(Long bonCommandeId);
    
    List<FactureFournisseur> findByStatut(String statut);
    
    // TODO.YML Ligne 17: 3-way match - Factures avec écarts
    @Query("SELECT ff FROM FactureFournisseur ff WHERE ff.threeWayMatchOk = false")
    List<FactureFournisseur> findAvecEcartsThreeWay();
    
    // TODO.YML Ligne 18: Factures bloquées (paiement impossible)
    @Query("SELECT ff FROM FactureFournisseur ff WHERE ff.statut = 'BLOQUEE'")
    List<FactureFournisseur> findFacturesBloquees();
}
