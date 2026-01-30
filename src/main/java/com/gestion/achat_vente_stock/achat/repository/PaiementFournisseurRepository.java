package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.PaiementFournisseur;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 19: Achats > Paiement
 */
@Repository
public interface PaiementFournisseurRepository extends JpaRepository<PaiementFournisseur, Long> {
    
    Optional<PaiementFournisseur> findByNumero(String numero);
    
    List<PaiementFournisseur> findByFactureFournisseurId(Long factureFournisseurId);
    
    List<PaiementFournisseur> findByStatut(String statut);
    
    List<PaiementFournisseur> findByModePaiement(String modePaiement);
}
