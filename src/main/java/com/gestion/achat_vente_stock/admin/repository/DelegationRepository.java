package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 61: Sécurité > Délégation
 */
@Repository
public interface DelegationRepository extends JpaRepository<Delegation, Long> {
    
    List<Delegation> findByDelegantId(Long delegantId);
    
    List<Delegation> findByDelegataireId(Long delegataireId);
    
    @Query("SELECT d FROM Delegation d WHERE d.delegataire.id = :userId " +
           "AND d.statut = 'ACTIVE' " +
           "AND d.dateDebut <= CURRENT_TIMESTAMP " +
           "AND (d.dateFin IS NULL OR d.dateFin > CURRENT_TIMESTAMP)")
    List<Delegation> findActiveDelegationsByDelegataireId(@Param("userId") Long userId);
    
    /**
     * Trouver les délégations actives pour un utilisateur à une date donnée
     */
    @Query("SELECT d FROM Delegation d WHERE d.delegataire.id = :userId " +
           "AND d.statut = 'ACTIVE' " +
           "AND d.dateDebut <= :date " +
           "AND (d.dateFin IS NULL OR d.dateFin > :date)")
    List<Delegation> findActiveDelegations(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    /**
     * Trouver les délégations par statut
     */
    List<Delegation> findByStatut(String statut);
}
