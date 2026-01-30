package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.BonCommande;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 11-13: Achats > Bon Commande
 */
@Repository
public interface BonCommandeRepository extends JpaRepository<BonCommande, Long> {
    
    Optional<BonCommande> findByNumero(String numero);
    
    List<BonCommande> findByStatut(String statut);
    
    List<BonCommande> findByFournisseurId(Long fournisseurId);
    
    List<BonCommande> findByAcheteurId(Long acheteurId);
    
    List<BonCommande> findByDemandeAchatId(Long demandeAchatId);
    
    // TODO.YML Ligne 12: BC nécessitant validation (montant > seuil)
    @Query("SELECT bc FROM BonCommande bc WHERE bc.montantTotalHt >= :seuil " +
           "AND bc.statut = 'EN_ATTENTE_VALIDATION'")
    List<BonCommande> findEnAttenteValidationParSeuil(@Param("seuil") BigDecimal seuil);
}
