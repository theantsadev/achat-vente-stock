package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.BonLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 24-26: Ventes > Livraison
 * Repository pour la gestion des bons de livraison
 */
@Repository
public interface BonLivraisonRepository extends JpaRepository<BonLivraison, Long> {

    /** Rechercher un BL par son numéro */
    Optional<BonLivraison> findByNumero(String numero);

    /** Rechercher les BL par commande client */
    List<BonLivraison> findByCommandeClientId(Long commandeClientId);

    /** Rechercher les BL par magasinier */
    List<BonLivraison> findByMagasinierId(Long magasinierId);

    /** Rechercher les BL par dépôt */
    List<BonLivraison> findByDepotId(Long depotId);

    /** Rechercher les BL par statut */
    List<BonLivraison> findByStatut(String statut);

    /** Compter le nombre de BL */
    long count();
}
