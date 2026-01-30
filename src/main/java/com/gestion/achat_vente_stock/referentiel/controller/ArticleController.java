package com.gestion.achat_vente_stock.referentiel.controller;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.service.ArticleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Ligne 1-2: Référentiels > Articles
 * Page "Gestion articles"
 * Créer/modifier/supprimer articles (code, nom, famille, unité)
 */
@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * TODO.YML Ligne 1: Liste des articles
     */
    @GetMapping
    public String listerArticles(Model model) {
        model.addAttribute("articles", articleService.listerTous());
        model.addAttribute("titre", "Gestion des Articles");
        return "referentiel/articles/liste";
    }
    
    /**
     * TODO.YML Ligne 1: Formulaire création article
     */
    @GetMapping("/nouveau")
    public String nouveauArticle(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("titre", "Nouvel Article");
        model.addAttribute("action", "Créer");
        return "referentiel/articles/formulaire";
    }
    
    /**
     * TODO.YML Ligne 1: Créer article
     */
    @PostMapping
    public String creerArticle(@ModelAttribute Article article, RedirectAttributes redirectAttributes) {
        try {
            articleService.creerArticle(article);
            redirectAttributes.addFlashAttribute("message", "Article créé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/articles";
    }
    
    /**
     * TODO.YML Ligne 1: Formulaire modification article
     */
    @GetMapping("/{id}/modifier")
    public String modifierArticle(@PathVariable Long id, Model model) {
        model.addAttribute("article", articleService.trouverParId(id));
        model.addAttribute("titre", "Modifier Article");
        model.addAttribute("action", "Modifier");
        return "referentiel/articles/formulaire";
    }
    
    /**
     * TODO.YML Ligne 1: Mettre à jour article
     */
    @PostMapping("/{id}")
    public String mettreAJourArticle(@PathVariable Long id, @ModelAttribute Article article, 
                                      RedirectAttributes redirectAttributes) {
        try {
            articleService.modifierArticle(id, article);
            redirectAttributes.addFlashAttribute("message", "Article modifié avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/articles";
    }
    
    /**
     * TODO.YML Ligne 1: Supprimer article
     */
    @GetMapping("/{id}/supprimer")
    public String supprimerArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.supprimerArticle(id);
            redirectAttributes.addFlashAttribute("message", "Article supprimé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/articles";
    }
    
    /**
     * TODO.YML Ligne 2: Articles avec traçabilité lots/séries
     */
    @GetMapping("/tracabilite")
    public String articlesAvecTracabilite(Model model) {
        model.addAttribute("articles", articleService.listerArticlesAvecTracabilite());
        model.addAttribute("titre", "Articles avec Traçabilité Lot");
        return "referentiel/articles/liste";
    }
}
