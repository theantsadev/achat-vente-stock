package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.DemandeAchat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 7-9: Achats > Demande Achat
 */
@Repository
public interface DemandeAchatRepository extends JpaRepository<DemandeAchat, Long> {
    
    Optional<DemandeAchat> findByNumero(String numero);
    
    List<DemandeAchat> findByStatut(String statut);
    
    List<DemandeAchat> findByDemandeurId(Long demandeurId);
    
    List<DemandeAchat> findByServiceId(Long serviceId);
    
    // TODO.YML Ligne 8: Workflow - Trouver DA par seuil de montant
    @Query("SELECT da FROM DemandeAchat da WHERE da.montantEstimeHt >= :montantMin " +
           "AND da.statut = 'EN_ATTENTE'")
    List<DemandeAchat> findEnAttenteParMontantMin(@Param("montantMin") BigDecimal montantMin);
}
