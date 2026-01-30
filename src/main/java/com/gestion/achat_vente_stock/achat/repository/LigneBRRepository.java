package com.gestion.achat_vente_stock.achat.repository;

import com.gestion.achat_vente_stock.achat.model.LigneBR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Lignes 14-16: Achats > Réception > Lignes
 */
@Repository
public interface LigneBRRepository extends JpaRepository<LigneBR, Long> {
    
    List<LigneBR> findByBonReceptionId(Long bonReceptionId);
    
    List<LigneBR> findByArticleId(Long articleId);
}
