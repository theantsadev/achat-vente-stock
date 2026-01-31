package com.gestion.achat_vente_stock.inventaire.repository;

import com.gestion.achat_vente_stock.inventaire.model.AjustementStock;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.MotifAjustement;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.StatutAjustement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AjustementStockRepository extends JpaRepository<AjustementStock, Long> {

    Optional<AjustementStock> findByNumero(String numero);

    List<AjustementStock> findByStatut(StatutAjustement statut);

    List<AjustementStock> findByMotif(MotifAjustement motif);

    List<AjustementStock> findByInventaireId(Long inventaireId);

    List<AjustementStock> findByArticleId(Long articleId);

    List<AjustementStock> findByDepotId(Long depotId);

    /**
     * Ajustements en attente de validation
     */
    @Query("SELECT a FROM AjustementStock a WHERE a.statut = 'EN_ATTENTE' ORDER BY a.demandeAt ASC")
    List<AjustementStock> findEnAttenteValidation();

    /**
     * Ajustements avec écart supérieur à un seuil (nécessite validation chef)
     */
    @Query("SELECT a FROM AjustementStock a WHERE a.statut = 'EN_ATTENTE' AND ABS(a.valeurAjustement) > :seuil")
    List<AjustementStock> findEnAttenteAvecSeuilDepasse(@Param("seuil") BigDecimal seuil);

    /**
     * Ajustements par période
     */
    @Query("SELECT a FROM AjustementStock a WHERE a.demandeAt >= :debut AND a.demandeAt <= :fin")
    List<AjustementStock> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Somme des ajustements par motif
     */
    @Query("SELECT a.motif, SUM(a.valeurAjustement) FROM AjustementStock a " +
           "WHERE a.statut = 'APPLIQUE' GROUP BY a.motif")
    List<Object[]> sumValeurByMotif();

    /**
     * Somme des ajustements pour un inventaire
     */
    @Query("SELECT SUM(a.valeurAjustement) FROM AjustementStock a WHERE a.inventaire.id = :inventaireId AND a.statut = 'APPLIQUE'")
    BigDecimal sumValeurByInventaire(@Param("inventaireId") Long inventaireId);

    /**
     * TODO.YML Ligne 54: Vérifier que le valideur n'est pas le compteur
     * Ajustements où le demandeur est différent du valideur
     */
    @Query("SELECT a FROM AjustementStock a WHERE a.inventaire.id = :inventaireId " +
           "AND a.demandeBy.id = :utilisateurId")
    List<AjustementStock> findByInventaireAndDemandeur(@Param("inventaireId") Long inventaireId, 
                                                        @Param("utilisateurId") Long utilisateurId);

    /**
     * Historique des ajustements pour un article
     */
    @Query("SELECT a FROM AjustementStock a WHERE a.article.id = :articleId ORDER BY a.demandeAt DESC")
    List<AjustementStock> findHistoriqueByArticle(@Param("articleId") Long articleId);

    /**
     * Statistiques des ajustements
     */
    @Query("SELECT a.statut, COUNT(a), SUM(a.valeurAjustement) FROM AjustementStock a GROUP BY a.statut")
    List<Object[]> getStatistiques();
}
