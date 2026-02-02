package com.gestion.achat_vente_stock.stock.controller;

import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.service.StockDisponibleService;
import com.gestion.achat_vente_stock.stock.service.ValorisationService;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur pour la consultation du stock disponible
 * Vue consolidée et KPIs
 */
@Controller
@RequestMapping("/stocks/disponible")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-MAGASINIER-REC', 'ROLE-MAGASINIER-SORT', 'ROLE-COMMERCIAL', 'ROLE-ACHETEUR', 'ROLE-ADMIN')")
public class StockDisponibleController {

    private final StockDisponibleService stockDisponibleService;
    private final ValorisationService valorisationService;
    private final ArticleRepository articleRepository;
    private final DepotRepository depotRepository;

    /**
     * Liste du stock disponible
     */
    @GetMapping
    public String liste(Model model,
                        @RequestParam(required = false) Long articleId,
                        @RequestParam(required = false) Long depotId) {
        
        List<StockDisponible> stocks;
        
        if (articleId != null && depotId != null) {
            stocks = stockDisponibleService.listerParArticleEtDepot(articleId, depotId);
        } else if (articleId != null) {
            stocks = stockDisponibleService.listerParArticle(articleId);
        } else if (depotId != null) {
            stocks = stockDisponibleService.listerParDepot(depotId);
        } else {
            stocks = stockDisponibleService.listerTous();
        }
        
        // KPIs
        BigDecimal valeurTotale = valorisationService.getValeurTotaleStock();
        
        model.addAttribute("stocks", stocks);
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("articleId", articleId);
        model.addAttribute("depotId", depotId);
        model.addAttribute("valeurTotale", valeurTotale);
        
        return "stocks/disponible/liste";
    }

    /**
     * Détail d'un stock
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        StockDisponible stock = stockDisponibleService.trouverParId(id);
        model.addAttribute("stock", stock);
        return "stocks/disponible/detail";
    }

    /**
     * Stocks en alerte (sous le minimum)
     */
    @GetMapping("/alertes")
    public String alertes(Model model) {
        List<StockDisponible> stocksEnAlerte = stockDisponibleService.getStocksEnAlerte();
        model.addAttribute("stocks", stocksEnAlerte);
        model.addAttribute("titre", "Stocks en alerte (sous le minimum)");
        return "stocks/disponible/alertes";
    }

    /**
     * Stocks en surstock (au-dessus du maximum)
     */
    @GetMapping("/surstocks")
    public String surstocks(Model model) {
        List<StockDisponible> stocksEnSurstock = stockDisponibleService.getStocksEnSurstock();
        model.addAttribute("stocks", stocksEnSurstock);
        model.addAttribute("titre", "Stocks en surstock (au-dessus du maximum)");
        return "stocks/disponible/alertes";
    }
}
