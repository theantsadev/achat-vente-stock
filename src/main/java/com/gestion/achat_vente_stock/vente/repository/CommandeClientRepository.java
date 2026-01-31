package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.CommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 22-23: Ventes > Commande Client
 * Repository pour la gestion des commandes clients
 */
@Repository
public interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {

    /** Rechercher une commande par son numéro */
    Optional<CommandeClient> findByNumero(String numero);

    /** Rechercher les commandes par devis source */
    List<CommandeClient> findByDevisId(Long devisId);

    /** Rechercher les commandes par client */
    List<CommandeClient> findByClientId(Long clientId);

    /** Rechercher les commandes par commercial */
    List<CommandeClient> findByCommercialId(Long commercialId);

    /** Rechercher les commandes par statut */
    List<CommandeClient> findByStatut(String statut);

    /** Rechercher les commandes avec stock réservé */
    List<CommandeClient> findByStockReserve(Boolean stockReserve);

    /** Compter le nombre de commandes */
    long count();
}
