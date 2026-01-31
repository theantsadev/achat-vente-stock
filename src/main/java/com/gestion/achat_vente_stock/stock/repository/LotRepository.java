package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 37-38: Stock > Lots/Séries
 * Repository pour la gestion des lots
 */
@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    Optional<Lot> findByNumero(String numero);

    List<Lot> findByArticleId(Long articleId);

    List<Lot> findByFournisseurId(Long fournisseurId);

    List<Lot> findByStatut(String statut);

    // TODO.YML Ligne 38: Lots expirés (DLC dépassée)
    @Query("SELECT l FROM Lot l WHERE l.dlc IS NOT NULL AND l.dlc < :today AND l.statut != 'EXPIRE'")
    List<Lot> findLotsExpiresDLC(@Param("today") LocalDate today);

    // TODO.YML Ligne 38: Lots expirés (DLUO dépassée)
    @Query("SELECT l FROM Lot l WHERE l.dluo IS NOT NULL AND l.dluo < :today AND l.statut != 'EXPIRE'")
    List<Lot> findLotsExpiresDLUO(@Param("today") LocalDate today);

    // Lots bientôt expirés (alerte)
    @Query("SELECT l FROM Lot l WHERE l.dlc IS NOT NULL AND l.dlc BETWEEN :today AND :dateAlerte AND l.statut = 'ACTIF'")
    List<Lot> findLotsBientotExpiresDLC(@Param("today") LocalDate today, @Param("dateAlerte") LocalDate dateAlerte);

    @Query("SELECT l FROM Lot l WHERE l.dluo IS NOT NULL AND l.dluo BETWEEN :today AND :dateAlerte AND l.statut = 'ACTIF'")
    List<Lot> findLotsBientotExpiresDLUO(@Param("today") LocalDate today, @Param("dateAlerte") LocalDate dateAlerte);

    // Lots actifs pour un article (pour sélection FIFO/FEFO)
    @Query("SELECT l FROM Lot l WHERE l.article.id = :articleId AND l.statut = 'ACTIF' ORDER BY l.dluo ASC NULLS LAST")
    List<Lot> findLotsActifsByArticleFEFO(@Param("articleId") Long articleId);

    @Query("SELECT l FROM Lot l WHERE l.article.id = :articleId AND l.statut = 'ACTIF' ORDER BY l.dateFabrication ASC NULLS LAST")
    List<Lot> findLotsActifsByArticleFIFO(@Param("articleId") Long articleId);

    // Dernière numéro de lot
    @Query("SELECT MAX(l.numero) FROM Lot l WHERE l.numero LIKE :prefix%")
    Optional<String> findLastNumeroByPrefix(@Param("prefix") String prefix);
}
