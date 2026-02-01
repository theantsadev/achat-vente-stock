package com.gestion.achat_vente_stock.inventaire.repository;

import com.gestion.achat_vente_stock.inventaire.model.Inventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.StatutInventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.TypeInventaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventaireRepository extends JpaRepository<Inventaire, Long> {

    Optional<Inventaire> findByNumero(String numero);

    List<Inventaire> findByStatut(StatutInventaire statut);

    List<Inventaire> findByType(TypeInventaire type);

    List<Inventaire> findByDepotId(Long depotId);

    @Query("SELECT i FROM Inventaire i WHERE i.depot.id = :depotId AND i.statut = :statut")
    List<Inventaire> findByDepotIdAndStatut(@Param("depotId") Long depotId, @Param("statut") StatutInventaire statut);

    /**
     * Vérifie si un inventaire est en cours (OUVERT ou EN_COMPTAGE) pour un dépôt
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventaire i " +
           "WHERE i.depot.id = :depotId AND i.statut IN ('OUVERT', 'EN_COMPTAGE', 'EN_VALIDATION')")
    boolean existsInventaireEnCours(@Param("depotId") Long depotId);

    /**
     * Inventaires à clôturer (en validation depuis plus de X jours)
     */
    @Query("SELECT i FROM Inventaire i WHERE i.statut = 'EN_VALIDATION' AND i.dateDebut < :dateLimite")
    List<Inventaire> findInventairesEnValidationDepuis(@Param("dateLimite") LocalDate dateLimite);

    /**
     * Historique des inventaires par dépôt
     */
    @Query("SELECT i FROM Inventaire i WHERE i.depot.id = :depotId ORDER BY i.dateDebut DESC")
    List<Inventaire> findHistoriqueByDepot(@Param("depotId") Long depotId);

    /**
     * Dernier inventaire clôturé pour un dépôt
     */
    @Query("SELECT i FROM Inventaire i WHERE i.depot.id = :depotId AND i.statut = 'CLOTURE' ORDER BY i.dateFin DESC")
    List<Inventaire> findDernierInventaireCloture(@Param("depotId") Long depotId);

    /**
     * Inventaires entre deux dates
     */
    @Query("SELECT i FROM Inventaire i WHERE i.dateDebut >= :debut AND i.dateDebut <= :fin")
    List<Inventaire> findByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    /**
     * Compte le nombre d'inventaires par statut
     */
    @Query("SELECT i.statut, COUNT(i) FROM Inventaire i GROUP BY i.statut")
    List<Object[]> countByStatut();
}
