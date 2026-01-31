package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.ReservationStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Ligne 36: Stock > Réservations
 * Repository pour la gestion des réservations de stock
 */
@Repository
public interface ReservationStockRepository extends JpaRepository<ReservationStock, Long> {

    List<ReservationStock> findByArticleId(Long articleId);

    List<ReservationStock> findByDepotId(Long depotId);

    List<ReservationStock> findByCommandeClientId(Long commandeClientId);

    List<ReservationStock> findByStatut(String statut);

    // Réservations actives pour un article/dépôt
    List<ReservationStock> findByArticleIdAndDepotIdAndStatut(Long articleId, Long depotId, String statut);

    // Réservations expirées à annuler
    @Query("SELECT r FROM ReservationStock r WHERE r.dateExpiration < :today AND r.statut = 'ACTIVE'")
    List<ReservationStock> findReservationsExpirees(@Param("today") LocalDate today);

    // Somme des réservations actives par article/dépôt
    @Query("SELECT COALESCE(SUM(r.quantiteReservee), 0) FROM ReservationStock r " +
           "WHERE r.article.id = :articleId AND r.depot.id = :depotId AND r.statut = 'ACTIVE'")
    BigDecimal sumReservationsActives(@Param("articleId") Long articleId, @Param("depotId") Long depotId);

    // Réservations par lot
    List<ReservationStock> findByLotNumeroAndStatut(String lotNumero, String statut);
}
