package com.gestion.achat_vente_stock.referentiel.service;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 1-2: Référentiels > Articles
 * Service pour la gestion des articles (code, nom, famille, unité)
 * avec gestion des lots/séries et dates (DLUO, DLC, traçabilité)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    
    /**
     * TODO.YML Ligne 1: Créer/modifier/supprimer articles
     */
    public Article creerArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        if (article.getStatut() == null) {
            article.setStatut("ACTIF");
        }
        return articleRepository.save(article);
    }
    
    public Article modifierArticle(Long id, Article article) {
        Article existant = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé: " + id));
        
        existant.setCode(article.getCode());
        existant.setDesignation(article.getDesignation());
        existant.setFamille(article.getFamille());
        existant.setUniteMesure(article.getUniteMesure());
        existant.setAchetable(article.getAchetable());
        existant.setVendable(article.getVendable());
        existant.setStockable(article.getStockable());
        existant.setTracabiliteLot(article.getTracabiliteLot());
        existant.setDluoObligatoire(article.getDluoObligatoire());
        existant.setMethodeValorisation(article.getMethodeValorisation());
        existant.setPrixAchatMoyen(article.getPrixAchatMoyen());
        existant.setPrixVentePublic(article.getPrixVentePublic());
        existant.setStockMinimum(article.getStockMinimum());
        existant.setStockMaximum(article.getStockMaximum());
        existant.setStatut(article.getStatut());
        
        return articleRepository.save(existant);
    }
    
    public void supprimerArticle(Long id) {
        articleRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Article trouverParId(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé: " + id));
    }
    
    @Transactional(readOnly = true)
    public Article trouverParCode(String code) {
        return articleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Article non trouvé: " + code));
    }
    
    @Transactional(readOnly = true)
    public List<Article> listerTous() {
        return articleRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Article> listerParFamille(Long familleId) {
        return articleRepository.findByFamilleId(familleId);
    }
    
    /**
     * TODO.YML Ligne 2: Gestion traçabilité lots/séries
     */
    @Transactional(readOnly = true)
    public List<Article> listerArticlesAvecTracabilite() {
        return articleRepository.findByTracabiliteLot(true);
    }
}
