package com.gestion.achat_vente_stock.stock.repository;

import com.gestion.achat_vente_stock.stock.model.LigneTransfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 34: Stock > Transferts
 * Repository pour les lignes de transfert
 */
@Repository
public interface LigneTransfertRepository extends JpaRepository<LigneTransfert, Long> {

    List<LigneTransfert> findByTransfertId(Long transfertId);

    List<LigneTransfert> findByArticleId(Long articleId);

    // Lignes avec écarts (quantité reçue != quantité expédiée)
    @Query("SELECT l FROM LigneTransfert l WHERE l.transfert.id = :transfertId " +
           "AND l.quantiteRecue IS NOT NULL AND l.quantiteExpedie IS NOT NULL " +
           "AND l.quantiteRecue != l.quantiteExpedie")
    List<LigneTransfert> findLignesAvecEcarts(@Param("transfertId") Long transfertId);

    // Lignes non complètement expédiées
    @Query("SELECT l FROM LigneTransfert l WHERE l.transfert.id = :transfertId " +
           "AND (l.quantiteExpedie IS NULL OR l.quantiteExpedie < l.quantiteDemandee)")
    List<LigneTransfert> findLignesNonExpediees(@Param("transfertId") Long transfertId);

    // Lignes non complètement reçues
    @Query("SELECT l FROM LigneTransfert l WHERE l.transfert.id = :transfertId " +
           "AND (l.quantiteRecue IS NULL OR l.quantiteRecue < l.quantiteExpedie)")
    List<LigneTransfert> findLignesNonRecues(@Param("transfertId") Long transfertId);
}
