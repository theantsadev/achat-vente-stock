package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 20-21: Ventes > Devis
 * Repository pour la gestion des devis clients
 */
@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {

    /** Rechercher un devis par son numéro */
    Optional<Devis> findByNumero(String numero);

    /** Rechercher les devis par client */
    List<Devis> findByClientId(Long clientId);

    /** Rechercher les devis par commercial */
    List<Devis> findByCommercialId(Long commercialId);

    /** Rechercher les devis par statut */
    List<Devis> findByStatut(String statut);

    /** Rechercher les devis expirés (date validité dépassée) */
    List<Devis> findByDateValiditeBeforeAndStatutNot(LocalDate date, String statut);

    /** Compter le nombre de devis */
    long count();
}
