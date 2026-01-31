package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.LigneFactureClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 27: Ventes > Facture Client > Lignes
 * Repository pour les lignes de factures clients
 */
@Repository
public interface LigneFactureClientRepository extends JpaRepository<LigneFactureClient, Long> {

    /** Rechercher les lignes par facture */
    List<LigneFactureClient> findByFactureClientId(Long factureClientId);

    /** Supprimer les lignes d'une facture */
    void deleteByFactureClientId(Long factureClientId);
}
