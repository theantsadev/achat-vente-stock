package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Article;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 1-2: Référentiels > Articles
 * Repository pour la gestion des articles
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    Optional<Article> findByCode(String code);
    
    // TODO.YML Ligne 15: Recherche par code-barres pour scan réception
    Optional<Article> findByCodeBarre(String codeBarre);
    
    List<Article> findByFamilleId(Long familleId);
    
    List<Article> findByStatut(String statut);
    
    List<Article> findByAchetable(Boolean achetable);
    
    List<Article> findByVendable(Boolean vendable);
    
    List<Article> findByTracabiliteLot(Boolean tracabiliteLot);
}
