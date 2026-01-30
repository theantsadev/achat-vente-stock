package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.FamilleArticle;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 1: Référentiels > Familles d'articles
 */
@Repository
public interface FamilleArticleRepository extends JpaRepository<FamilleArticle, Long> {
    
    Optional<FamilleArticle> findByCode(String code);
    
    List<FamilleArticle> findByParentId(Long parentId);
    
    List<FamilleArticle> findByTracabiliteLotDefaut(Boolean tracabiliteLot);
}
