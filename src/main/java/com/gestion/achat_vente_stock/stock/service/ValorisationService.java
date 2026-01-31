package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.stock.model.MouvementStock;
import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.repository.MouvementStockRepository;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * TODO.YML Lignes 39-41: Stock > Valorisation
 * - Ligne 39: Implémenter FIFO et CUMP (coût moyen pondéré)
 * - Ligne 40: Clôture mensuelle : gel coûts et contrôle rétrodatation
 * - Ligne 41: Rapports variation coût et écarts valorisation
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ValorisationService {

    private final MouvementStockRepository mouvementStockRepository;
    private final StockDisponibleRepository stockDisponibleRepository;
    private final ArticleRepository articleRepository;

    /**
     * TODO.YML Ligne 39: Obtenir le coût de sortie selon la méthode de valorisation
     */
    public BigDecimal getCoutSortie(Article article, Depot depot, BigDecimal quantiteDemandee) {
        String methode = article.getMethodeValorisation();
        
        if ("FIFO".equals(methode)) {
            return getCoutFIFO(article, depot, quantiteDemandee);
        } else if ("CUMP".equals(methode)) {
            return getCoutCUMP(article);
        } else {
            // Par défaut, utiliser le prix d'achat moyen de l'article
            return article.getPrixAchatMoyen() != null ? article.getPrixAchatMoyen() : BigDecimal.ZERO;
        }
    }

    /**
     * TODO.YML Ligne 39: Méthode FIFO (First In, First Out)
     * Retourne le coût unitaire moyen pondéré des entrées les plus anciennes
     */
    private BigDecimal getCoutFIFO(Article article, Depot depot, BigDecimal quantiteDemandee) {
        List<MouvementStock> entrees = mouvementStockRepository.findEntreesFIFO(article.getId(), depot.getId());
        
        if (entrees.isEmpty()) {
            return article.getPrixAchatMoyen() != null ? article.getPrixAchatMoyen() : BigDecimal.ZERO;
        }

        BigDecimal quantiteRestante = quantiteDemandee;
        BigDecimal valeurTotale = BigDecimal.ZERO;
        BigDecimal quantiteUtilisee = BigDecimal.ZERO;

        for (MouvementStock entree : entrees) {
            if (quantiteRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal qteDisponible = entree.getQuantite();
            BigDecimal qteAPrendre = qteDisponible.min(quantiteRestante);

            valeurTotale = valeurTotale.add(qteAPrendre.multiply(entree.getCoutUnitaire()));
            quantiteUtilisee = quantiteUtilisee.add(qteAPrendre);
            quantiteRestante = quantiteRestante.subtract(qteAPrendre);
        }

        if (quantiteUtilisee.compareTo(BigDecimal.ZERO) == 0) {
            return article.getPrixAchatMoyen() != null ? article.getPrixAchatMoyen() : BigDecimal.ZERO;
        }

        return valeurTotale.divide(quantiteUtilisee, 6, RoundingMode.HALF_UP);
    }

    /**
     * TODO.YML Ligne 39: Méthode CUMP (Coût Unitaire Moyen Pondéré)
     * Retourne le CUMP actuel de l'article
     */
    private BigDecimal getCoutCUMP(Article article) {
        return article.getPrixAchatMoyen() != null ? article.getPrixAchatMoyen() : BigDecimal.ZERO;
    }

    /**
     * TODO.YML Ligne 39: Recalculer le CUMP après une entrée
     * CUMP = (Valeur stock existant + Valeur nouvelle entrée) / (Qté existante + Qté entrée)
     */
    public void recalculerCUMP(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé: " + articleId));

        // Somme des valeurs et quantités de tout le stock
        List<StockDisponible> stocks = stockDisponibleRepository.findByArticleId(articleId);
        
        BigDecimal valeurTotale = BigDecimal.ZERO;
        BigDecimal quantiteTotale = BigDecimal.ZERO;

        for (StockDisponible stock : stocks) {
            if (stock.getQuantitePhysique() != null && stock.getQuantitePhysique().compareTo(BigDecimal.ZERO) > 0) {
                valeurTotale = valeurTotale.add(stock.getValeurStock() != null ? stock.getValeurStock() : BigDecimal.ZERO);
                quantiteTotale = quantiteTotale.add(stock.getQuantitePhysique());
            }
        }

        if (quantiteTotale.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal nouveauCUMP = valeurTotale.divide(quantiteTotale, 4, RoundingMode.HALF_UP);
            article.setPrixAchatMoyen(nouveauCUMP);
            articleRepository.save(article);
        }
    }

    /**
     * TODO.YML Ligne 41: Calculer la valeur totale du stock
     */
    @Transactional(readOnly = true)
    public BigDecimal getValeurTotaleStock() {
        return stockDisponibleRepository.sumValeurTotaleStock();
    }

    /**
     * TODO.YML Ligne 41: Calculer la valeur du stock par dépôt
     */
    @Transactional(readOnly = true)
    public BigDecimal getValeurStockParDepot(Long depotId) {
        return stockDisponibleRepository.sumValeurStockByDepot(depotId);
    }

    /**
     * TODO.YML Ligne 41: Calculer la valeur du stock par article
     */
    @Transactional(readOnly = true)
    public BigDecimal getValeurStockParArticle(Long articleId) {
        List<StockDisponible> stocks = stockDisponibleRepository.findByArticleId(articleId);
        return stocks.stream()
                .map(s -> s.getValeurStock() != null ? s.getValeurStock() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * TODO.YML Ligne 41: Rapport de variation de coût
     * Compare le coût actuel avec le coût de la période précédente
     */
    @Transactional(readOnly = true)
    public BigDecimal getVariationCout(Article article, BigDecimal ancienCout) {
        BigDecimal coutActuel = article.getPrixAchatMoyen();
        if (coutActuel == null || ancienCout == null || ancienCout.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return coutActuel.subtract(ancienCout)
                .divide(ancienCout, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // En pourcentage
    }
}
