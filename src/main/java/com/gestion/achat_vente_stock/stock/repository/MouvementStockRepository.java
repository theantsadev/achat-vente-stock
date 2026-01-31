package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 31-33: Stock > Mouvements
 * Repository pour la gestion des mouvements de stock
 */
@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    Optional<MouvementStock> findByNumero(String numero);

    List<MouvementStock> findByArticleId(Long articleId);

    List<MouvementStock> findByDepotId(Long depotId);

    List<MouvementStock> findByTypeMouvement(String typeMouvement);

    List<MouvementStock> findByLotNumero(String lotNumero);

    // Mouvements par article et dépôt
    List<MouvementStock> findByArticleIdAndDepotId(Long articleId, Long depotId);

    // Mouvements par période
    List<MouvementStock> findByDateMouvementBetween(LocalDate debut, LocalDate fin);

    // Mouvements par document source
    List<MouvementStock> findByTypeDocumentAndDocumentId(String typeDocument, Long documentId);

    // TODO.YML Ligne 40: Vérification mouvements rétrodatés
    @Query("SELECT m FROM MouvementStock m WHERE m.dateMouvement < :dateCloture AND m.createdAt > :dateCloture")
    List<MouvementStock> findMouvementsRetrodates(@Param("dateCloture") LocalDate dateCloture);

    // Dernier numéro généré
    @Query("SELECT MAX(m.numero) FROM MouvementStock m WHERE m.numero LIKE :prefix%")
    Optional<String> findLastNumeroByPrefix(@Param("prefix") String prefix);

    // Mouvements d'entrée par article/dépôt (pour FIFO)
    @Query("SELECT m FROM MouvementStock m WHERE m.article.id = :articleId AND m.depot.id = :depotId " +
           "AND m.typeMouvement LIKE 'ENTREE%' ORDER BY m.dateMouvement ASC, m.id ASC")
    List<MouvementStock> findEntreesFIFO(@Param("articleId") Long articleId, @Param("depotId") Long depotId);

    // Mouvements d'entrée par lot (pour FEFO basé sur DLUO)
    @Query("SELECT m FROM MouvementStock m WHERE m.article.id = :articleId AND m.depot.id = :depotId " +
           "AND m.typeMouvement LIKE 'ENTREE%' AND m.lotNumero IS NOT NULL ORDER BY m.dluo ASC NULLS LAST, m.id ASC")
    List<MouvementStock> findEntreesFEFO(@Param("articleId") Long articleId, @Param("depotId") Long depotId);

    // Somme des entrées par article/dépôt
    @Query("SELECT COALESCE(SUM(m.quantite), 0) FROM MouvementStock m " +
           "WHERE m.article.id = :articleId AND m.depot.id = :depotId AND m.typeMouvement LIKE 'ENTREE%'")
    java.math.BigDecimal sumEntreesByArticleAndDepot(@Param("articleId") Long articleId, @Param("depotId") Long depotId);

    // Somme des sorties par article/dépôt
    @Query("SELECT COALESCE(SUM(m.quantite), 0) FROM MouvementStock m " +
           "WHERE m.article.id = :articleId AND m.depot.id = :depotId AND m.typeMouvement LIKE 'SORTIE%'")
    java.math.BigDecimal sumSortiesByArticleAndDepot(@Param("articleId") Long articleId, @Param("depotId") Long depotId);
}
