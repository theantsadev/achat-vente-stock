package com.gestion.achat_vente_stock.achat.repository;

import com.gestion.achat_vente_stock.achat.model.LigneBC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Lignes 11-16: Achats > Bon Commande > Lignes
 * Repository pour les lignes de bon de commande
 */
@Repository
public interface LigneBCRepository extends JpaRepository<LigneBC, Long> {
    
    List<LigneBC> findByBonCommandeId(Long bonCommandeId);
    
    List<LigneBC> findByArticleId(Long articleId);
}
