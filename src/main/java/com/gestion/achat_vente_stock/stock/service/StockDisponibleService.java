package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service pour la consultation du stock disponible
 * Vue consolidée pour les KPIs et tableaux de bord
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockDisponibleService {

    private final StockDisponibleRepository stockDisponibleRepository;

    /**
     * Obtenir tout le stock disponible
     */
    public List<StockDisponible> listerTous() {
        return stockDisponibleRepository.findAll();
    }

    /**
     * Obtenir le stock par article
     */
    public List<StockDisponible> listerParArticle(Long articleId) {
        return stockDisponibleRepository.findByArticleId(articleId);
    }

    /**
     * Obtenir le stock par dépôt
     */
    public List<StockDisponible> listerParDepot(Long depotId) {
        return stockDisponibleRepository.findByDepotId(depotId);
    }

    /**
     * Obtenir le stock par article et dépôt
     */
    public List<StockDisponible> listerParArticleEtDepot(Long articleId, Long depotId) {
        return stockDisponibleRepository.findByArticleIdAndDepotId(articleId, depotId);
    }

    /**
     * Obtenir les stocks avec quantité disponible positive
     */
    public List<StockDisponible> listerStocksDisponibles() {
        return stockDisponibleRepository.findStocksDisponibles();
    }

    /**
     * KPI: Stocks en alerte (sous le minimum)
     */
    public List<StockDisponible> getStocksEnAlerte() {
        return stockDisponibleRepository.findStocksEnAlerte();
    }

    /**
     * KPI: Stocks en surstock (au-dessus du maximum)
     */
    public List<StockDisponible> getStocksEnSurstock() {
        return stockDisponibleRepository.findStocksEnSurstock();
    }

    /**
     * KPI: Quantité totale par article (tous dépôts)
     */
    public BigDecimal getQuantiteTotaleParArticle(Long articleId) {
        return stockDisponibleRepository.sumQuantitePhysiqueByArticle(articleId);
    }

    /**
     * KPI: Quantité disponible totale par article (tous dépôts)
     */
    public BigDecimal getQuantiteDisponibleTotaleParArticle(Long articleId) {
        return stockDisponibleRepository.sumQuantiteDisponibleByArticle(articleId);
    }

    /**
     * KPI: Valeur totale du stock
     */
    public BigDecimal getValeurTotaleStock() {
        return stockDisponibleRepository.sumValeurTotaleStock();
    }

    /**
     * KPI: Valeur du stock par dépôt
     */
    public BigDecimal getValeurStockParDepot(Long depotId) {
        return stockDisponibleRepository.sumValeurStockByDepot(depotId);
    }

    /**
     * Trouver un stock spécifique
     */
    public StockDisponible trouverParId(Long id) {
        return stockDisponibleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé: " + id));
    }
}
