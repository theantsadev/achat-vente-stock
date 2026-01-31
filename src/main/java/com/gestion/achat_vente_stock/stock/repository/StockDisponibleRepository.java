package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 36: Stock > Réservations
 * Repository pour la vue stock disponible
 */
@Repository
public interface StockDisponibleRepository extends JpaRepository<StockDisponible, Long> {

    // Stock par article et dépôt
    List<StockDisponible> findByArticleIdAndDepotId(Long articleId, Long depotId);

    // Stock par article
    List<StockDisponible> findByArticleId(Long articleId);

    // Stock par dépôt
    List<StockDisponible> findByDepotId(Long depotId);

    // Stock par lot
    Optional<StockDisponible> findByArticleIdAndDepotIdAndLotNumero(Long articleId, Long depotId, String lotNumero);

    // Stock par emplacement
    List<StockDisponible> findByDepotIdAndEmplacement(Long depotId, String emplacement);

    // Stocks avec quantité disponible positive
    @Query("SELECT s FROM StockDisponible s WHERE s.quantiteDisponible > 0")
    List<StockDisponible> findStocksDisponibles();

    // Stocks en alerte (quantité < seuil minimum de l'article)
    @Query("SELECT s FROM StockDisponible s WHERE s.quantitePhysique < s.article.stockMinimum")
    List<StockDisponible> findStocksEnAlerte();

    // Stocks en surstock (quantité > seuil maximum de l'article)
    @Query("SELECT s FROM StockDisponible s WHERE s.quantitePhysique > s.article.stockMaximum")
    List<StockDisponible> findStocksEnSurstock();

    // Somme stock par article (tous dépôts)
    @Query("SELECT COALESCE(SUM(s.quantitePhysique), 0) FROM StockDisponible s WHERE s.article.id = :articleId")
    BigDecimal sumQuantitePhysiqueByArticle(@Param("articleId") Long articleId);

    // Somme stock disponible par article (tous dépôts)
    @Query("SELECT COALESCE(SUM(s.quantiteDisponible), 0) FROM StockDisponible s WHERE s.article.id = :articleId")
    BigDecimal sumQuantiteDisponibleByArticle(@Param("articleId") Long articleId);

    // Valeur totale du stock
    @Query("SELECT COALESCE(SUM(s.valeurStock), 0) FROM StockDisponible s")
    BigDecimal sumValeurTotaleStock();

    // Valeur totale du stock par dépôt
    @Query("SELECT COALESCE(SUM(s.valeurStock), 0) FROM StockDisponible s WHERE s.depot.id = :depotId")
    BigDecimal sumValeurStockByDepot(@Param("depotId") Long depotId);

    // Mise à jour de la quantité réservée
    @Modifying
    @Query("UPDATE StockDisponible s SET s.quantiteReservee = s.quantiteReservee + :quantite, " +
           "s.quantiteDisponible = s.quantitePhysique - (s.quantiteReservee + :quantite) " +
           "WHERE s.id = :id")
    void incrementerReservation(@Param("id") Long id, @Param("quantite") BigDecimal quantite);

    @Modifying
    @Query("UPDATE StockDisponible s SET s.quantiteReservee = s.quantiteReservee - :quantite, " +
           "s.quantiteDisponible = s.quantitePhysique - (s.quantiteReservee - :quantite) " +
           "WHERE s.id = :id")
    void decrementerReservation(@Param("id") Long id, @Param("quantite") BigDecimal quantite);
}
