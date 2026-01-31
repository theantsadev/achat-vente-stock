package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.LigneBL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 24: Ventes > Livraison > Lignes
 * Repository pour les lignes de bons de livraison
 */
@Repository
public interface LigneBLRepository extends JpaRepository<LigneBL, Long> {

    /** Rechercher les lignes par bon de livraison */
    List<LigneBL> findByBonLivraisonId(Long bonLivraisonId);

    /** Supprimer les lignes d'un BL */
    void deleteByBonLivraisonId(Long bonLivraisonId);
}
