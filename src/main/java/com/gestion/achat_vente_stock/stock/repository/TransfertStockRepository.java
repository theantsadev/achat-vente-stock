package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.TransfertStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 34-35: Stock > Transferts
 * Repository pour la gestion des transferts inter-dépôts
 */
@Repository
public interface TransfertStockRepository extends JpaRepository<TransfertStock, Long> {

    Optional<TransfertStock> findByNumero(String numero);

    List<TransfertStock> findByStatut(String statut);

    List<TransfertStock> findByDepotSourceId(Long depotSourceId);

    List<TransfertStock> findByDepotDestinationId(Long depotDestinationId);

    List<TransfertStock> findByDemandeurId(Long demandeurId);

    // Transferts en attente de validation
    @Query("SELECT t FROM TransfertStock t WHERE t.statut = 'DEMANDE' AND t.depotSource.id = :depotId")
    List<TransfertStock> findTransfertsAValider(@Param("depotId") Long depotId);

    // Transferts en transit vers un dépôt
    @Query("SELECT t FROM TransfertStock t WHERE t.statut = 'EN_TRANSIT' AND t.depotDestination.id = :depotId")
    List<TransfertStock> findTransfertsEnTransitVers(@Param("depotId") Long depotId);

    // Transferts par période
    List<TransfertStock> findByDateDemandeBetween(LocalDate debut, LocalDate fin);

    // Dernier numéro de transfert
    @Query("SELECT MAX(t.numero) FROM TransfertStock t WHERE t.numero LIKE :prefix%")
    Optional<String> findLastNumeroByPrefix(@Param("prefix") String prefix);
}
