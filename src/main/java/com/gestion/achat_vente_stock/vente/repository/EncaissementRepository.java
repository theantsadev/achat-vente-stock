package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.Encaissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 29: Ventes > Encaissement
 * Repository pour la gestion des encaissements
 */
@Repository
public interface EncaissementRepository extends JpaRepository<Encaissement, Long> {

    /** Rechercher un encaissement par son numéro */
    Optional<Encaissement> findByNumero(String numero);

    /** Rechercher les encaissements par facture */
    List<Encaissement> findByFactureClientId(Long factureClientId);

    /** Rechercher les encaissements par mode de paiement */
    List<Encaissement> findByModePaiement(String modePaiement);

    /** Rechercher les encaissements par statut */
    List<Encaissement> findByStatut(String statut);

    /** Calculer le total encaissé pour une facture */
    @Query("SELECT COALESCE(SUM(e.montantEncaisse), 0) FROM Encaissement e WHERE e.factureClient.id = :factureId AND e.statut = 'VALIDE'")
    BigDecimal calculerTotalEncaisseParFacture(Long factureId);

    /** Compter le nombre d'encaissements */
    long count();
}
