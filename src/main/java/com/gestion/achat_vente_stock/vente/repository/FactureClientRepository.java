package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.FactureClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 27-28: Ventes > Facture Client
 * Repository pour la gestion des factures clients
 */
@Repository
public interface FactureClientRepository extends JpaRepository<FactureClient, Long> {

    /** Rechercher une facture par son numéro */
    Optional<FactureClient> findByNumero(String numero);

    /** Rechercher les factures par commande client */
    List<FactureClient> findByCommandeClientId(Long commandeClientId);

    /** Rechercher les factures par bon de livraison */
    List<FactureClient> findByBonLivraisonId(Long bonLivraisonId);

    /** Rechercher les factures par client */
    List<FactureClient> findByClientId(Long clientId);

    /** Rechercher les factures par statut */
    List<FactureClient> findByStatut(String statut);

    /** Rechercher les factures non payées */
    List<FactureClient> findByEstPayeeFalse();

    /** Compter le nombre de factures */
    long count();
}
