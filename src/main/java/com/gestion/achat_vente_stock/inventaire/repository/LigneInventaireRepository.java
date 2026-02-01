package com.gestion.achat_vente_stock.inventaire.repository;

import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {

    List<LigneInventaire> findByInventaireId(Long inventaireId);

    List<LigneInventaire> findByInventaireIdAndArticleId(Long inventaireId, Long articleId);

    /**
     * Lignes par emplacement pour un inventaire
     */
    @Query("SELECT li FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId ORDER BY li.emplacement, li.article.code")
    List<LigneInventaire> findByInventaireIdOrderByEmplacement(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes non comptées (comptage 1 non fait)
     */
    @Query("SELECT li FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId AND li.quantiteComptee1 IS NULL")
    List<LigneInventaire> findLignesNonComptees(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes comptées une fois (comptage 1 fait, comptage 2 non fait)
     */
    @Query("SELECT li FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId " +
           "AND li.quantiteComptee1 IS NOT NULL AND li.quantiteComptee2 IS NULL")
    List<LigneInventaire> findLignesComptage1Fait(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes avec écart (écart != 0)
     */
    @Query("SELECT li FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId " +
           "AND li.ecartQuantite IS NOT NULL AND li.ecartQuantite <> 0")
    List<LigneInventaire> findLignesAvecEcart(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes avec écart supérieur à un seuil (en valeur absolue)
     */
    @Query("SELECT li FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId " +
           "AND ABS(li.ecartQuantite) > :seuil")
    List<LigneInventaire> findLignesEcartSuperieurSeuil(@Param("inventaireId") Long inventaireId, @Param("seuil") BigDecimal seuil);

    /**
     * Somme des écarts valorisés pour un inventaire
     */
    @Query("SELECT SUM(li.ecartValeur) FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId")
    BigDecimal sumEcartValeurByInventaire(@Param("inventaireId") Long inventaireId);

    /**
     * Nombre de lignes par statut de comptage
     */
    @Query("SELECT " +
           "SUM(CASE WHEN li.quantiteComptee1 IS NULL THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN li.quantiteComptee1 IS NOT NULL AND li.quantiteComptee2 IS NULL THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN li.quantiteComptee2 IS NOT NULL THEN 1 ELSE 0 END) " +
           "FROM LigneInventaire li WHERE li.inventaire.id = :inventaireId")
    Object[] countStatutComptage(@Param("inventaireId") Long inventaireId);

    /**
     * Lignes pour un lot spécifique
     */
    List<LigneInventaire> findByInventaireIdAndLotNumero(Long inventaireId, String lotNumero);

    /**
     * Supprime toutes les lignes d'un inventaire
     */
    void deleteByInventaireId(Long inventaireId);
}
