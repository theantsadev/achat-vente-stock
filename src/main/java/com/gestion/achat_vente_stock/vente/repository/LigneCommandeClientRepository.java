package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Lignes 22-24: Ventes > Commande Client > Lignes
 * Repository pour les lignes de commandes clients
 */
@Repository
public interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Long> {

    /** Rechercher les lignes par commande */
    List<LigneCommandeClient> findByCommandeClientId(Long commandeClientId);

    /** Supprimer les lignes d'une commande */
    void deleteByCommandeClientId(Long commandeClientId);
}
