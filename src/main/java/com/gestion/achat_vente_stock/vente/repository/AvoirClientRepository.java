package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.AvoirClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 30: Ventes > Avoirs
 * Repository pour la gestion des avoirs clients
 */
@Repository
public interface AvoirClientRepository extends JpaRepository<AvoirClient, Long> {

    /** Rechercher un avoir par son numéro */
    Optional<AvoirClient> findByNumero(String numero);

    /** Rechercher les avoirs par facture */
    List<AvoirClient> findByFactureClientId(Long factureClientId);

    /** Rechercher les avoirs par client */
    List<AvoirClient> findByClientId(Long clientId);

    /** Rechercher les avoirs par statut */
    List<AvoirClient> findByStatut(String statut);

    /** Rechercher les avoirs par motif */
    List<AvoirClient> findByMotif(String motif);

    /** Compter le nombre d'avoirs */
    long count();
}
