package com.gestion.achat_vente_stock.vente.repository;

import com.gestion.achat_vente_stock.vente.model.LigneDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 20: Ventes > Devis > Lignes
 * Repository pour les lignes de devis
 */
@Repository
public interface LigneDevisRepository extends JpaRepository<LigneDevis, Long> {

    /** Rechercher les lignes par devis */
    List<LigneDevis> findByDevisId(Long devisId);

    /** Supprimer les lignes d'un devis */
    void deleteByDevisId(Long devisId);
}
