package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.stock.model.MouvementStock;
import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.repository.MouvementStockRepository;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 31-33: Stock > Mouvements
 * - Ligne 31: Tracer tous mouvements (entrée, sortie, transfert, ajustement)
 * - Ligne 32: Journaliser date/heure, utilisateur, dépôt, emplacement, coût
 * - Ligne 33: Numérotation automatique et non réutilisable
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MouvementStockService {

    private final MouvementStockRepository mouvementStockRepository;
    private final StockDisponibleRepository stockDisponibleRepository;
    private final AuditService auditService;
    private final ValorisationService valorisationService;

    // Types de mouvement
    public static final String ENTREE_RECEPTION = "ENTREE_RECEPTION";
    public static final String ENTREE_RETOUR_CLIENT = "ENTREE_RETOUR_CLIENT";
    public static final String ENTREE_AJUSTEMENT = "ENTREE_AJUSTEMENT";
    public static final String ENTREE_TRANSFERT = "ENTREE_TRANSFERT";
    public static final String SORTIE_LIVRAISON = "SORTIE_LIVRAISON";
    public static final String SORTIE_CONSOMMATION = "SORTIE_CONSOMMATION";
    public static final String SORTIE_REBUT = "SORTIE_REBUT";
    public static final String SORTIE_AJUSTEMENT = "SORTIE_AJUSTEMENT";
    public static final String SORTIE_TRANSFERT = "SORTIE_TRANSFERT";

    /**
     * TODO.YML Ligne 31: Créer un mouvement d'entrée de stock
     */
    public MouvementStock creerMouvementEntree(Article article, Depot depot, String typeMouvement,
                                                BigDecimal quantite, BigDecimal coutUnitaire,
                                                String emplacement, String lotNumero, LocalDate dluo,
                                                Long documentId, String typeDocument,
                                                Utilisateur utilisateur) {
        
        validateTypeMouvementEntree(typeMouvement);
        
        MouvementStock mouvement = new MouvementStock();
        mouvement.setNumero(genererNumeroMouvement("MVT"));
        mouvement.setArticle(article);
        mouvement.setDepot(depot);
        mouvement.setTypeMouvement(typeMouvement);
        mouvement.setQuantite(quantite);
        mouvement.setUnite(article.getUniteMesure());
        mouvement.setCoutUnitaire(coutUnitaire);
        mouvement.setValeurTotale(quantite.multiply(coutUnitaire).setScale(4, RoundingMode.HALF_UP));
        mouvement.setEmplacement(emplacement);
        mouvement.setLotNumero(lotNumero);
        mouvement.setDluo(dluo);
        mouvement.setDocumentId(documentId);
        mouvement.setTypeDocument(typeDocument);
        mouvement.setDateMouvement(LocalDate.now());
        mouvement.setCreatedAt(LocalDateTime.now());
        mouvement.setCreatedBy(utilisateur);

        MouvementStock saved = mouvementStockRepository.save(mouvement);

        // Mettre à jour le stock disponible
        mettreAJourStockEntree(article, depot, emplacement, lotNumero, quantite, coutUnitaire);

        // TODO.YML Ligne 39: Mettre à jour le CUMP si méthode valorisation = CUMP
        if ("CUMP".equals(article.getMethodeValorisation())) {
            valorisationService.recalculerCUMP(article.getId());
        }

        // Audit
        auditService.logAction(utilisateur, "mouvement_stock", saved.getId(),
                "CREATE_ENTREE", null, saved.toString(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 31: Créer un mouvement de sortie de stock
     */
    public MouvementStock creerMouvementSortie(Article article, Depot depot, String typeMouvement,
                                                BigDecimal quantite, String emplacement, String lotNumero,
                                                Long documentId, String typeDocument,
                                                Utilisateur utilisateur) {
        
        validateTypeMouvementSortie(typeMouvement);
        
        // Vérifier le stock disponible
        BigDecimal stockDispo = getQuantiteDisponible(article.getId(), depot.getId(), lotNumero);
        if (stockDispo.compareTo(quantite) < 0) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + stockDispo + ", Demandé: " + quantite);
        }

        // TODO.YML Ligne 39: Obtenir le coût unitaire selon méthode valorisation
        BigDecimal coutUnitaire = valorisationService.getCoutSortie(article, depot, quantite);

        MouvementStock mouvement = new MouvementStock();
        mouvement.setNumero(genererNumeroMouvement("MVT"));
        mouvement.setArticle(article);
        mouvement.setDepot(depot);
        mouvement.setTypeMouvement(typeMouvement);
        mouvement.setQuantite(quantite);
        mouvement.setUnite(article.getUniteMesure());
        mouvement.setCoutUnitaire(coutUnitaire);
        mouvement.setValeurTotale(quantite.multiply(coutUnitaire).setScale(4, RoundingMode.HALF_UP));
        mouvement.setEmplacement(emplacement);
        mouvement.setLotNumero(lotNumero);
        mouvement.setDocumentId(documentId);
        mouvement.setTypeDocument(typeDocument);
        mouvement.setDateMouvement(LocalDate.now());
        mouvement.setCreatedAt(LocalDateTime.now());
        mouvement.setCreatedBy(utilisateur);

        MouvementStock saved = mouvementStockRepository.save(mouvement);

        // Mettre à jour le stock disponible
        mettreAJourStockSortie(article, depot, emplacement, lotNumero, quantite, coutUnitaire);

        // Audit
        auditService.logAction(utilisateur, "mouvement_stock", saved.getId(),
                "CREATE_SORTIE", null, saved.toString(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 33: Générer un numéro de mouvement unique et non réutilisable
     */
    private String genererNumeroMouvement(String prefix) {
        String datePrefix = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Optional<String> lastNumero = mouvementStockRepository.findLastNumeroByPrefix(datePrefix);
        
        int sequence = 1;
        if (lastNumero.isPresent()) {
            String last = lastNumero.get();
            String seqStr = last.substring(last.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(seqStr) + 1;
        }
        
        return datePrefix + String.format("%05d", sequence);
    }

    /**
     * Mettre à jour le stock disponible après une entrée
     */
    private void mettreAJourStockEntree(Article article, Depot depot, String emplacement,
                                         String lotNumero, BigDecimal quantite, BigDecimal coutUnitaire) {
        Optional<StockDisponible> stockOpt = stockDisponibleRepository
                .findByArticleIdAndDepotIdAndLotNumero(article.getId(), depot.getId(), lotNumero);

        StockDisponible stock;
        if (stockOpt.isPresent()) {
            stock = stockOpt.get();
            // Recalculer la valeur moyenne
            BigDecimal ancienneValeur = stock.getValeurStock() != null ? stock.getValeurStock() : BigDecimal.ZERO;
            BigDecimal nouvelleValeur = quantite.multiply(coutUnitaire);
            BigDecimal ancienneQte = stock.getQuantitePhysique() != null ? stock.getQuantitePhysique() : BigDecimal.ZERO;
            BigDecimal nouvelleQte = ancienneQte.add(quantite);
            
            stock.setQuantitePhysique(nouvelleQte);
            stock.setValeurStock(ancienneValeur.add(nouvelleValeur));
        } else {
            stock = new StockDisponible();
            stock.setArticle(article);
            stock.setDepot(depot);
            stock.setEmplacement(emplacement);
            stock.setLotNumero(lotNumero);
            stock.setQuantitePhysique(quantite);
            stock.setQuantiteReservee(BigDecimal.ZERO);
            stock.setValeurStock(quantite.multiply(coutUnitaire));
        }
        
        // Recalculer quantité disponible
        stock.setQuantiteDisponible(stock.getQuantitePhysique().subtract(
                stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : BigDecimal.ZERO));
        
        stockDisponibleRepository.save(stock);
    }

    /**
     * Mettre à jour le stock disponible après une sortie
     */
    private void mettreAJourStockSortie(Article article, Depot depot, String emplacement,
                                         String lotNumero, BigDecimal quantite, BigDecimal coutUnitaire) {
        Optional<StockDisponible> stockOpt = stockDisponibleRepository
                .findByArticleIdAndDepotIdAndLotNumero(article.getId(), depot.getId(), lotNumero);

        if (stockOpt.isEmpty()) {
            throw new RuntimeException("Stock non trouvé pour cet article/dépôt/lot");
        }

        StockDisponible stock = stockOpt.get();
        stock.setQuantitePhysique(stock.getQuantitePhysique().subtract(quantite));
        stock.setValeurStock(stock.getValeurStock().subtract(quantite.multiply(coutUnitaire)));
        stock.setQuantiteDisponible(stock.getQuantitePhysique().subtract(
                stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : BigDecimal.ZERO));
        
        stockDisponibleRepository.save(stock);
    }

    /**
     * Obtenir la quantité disponible
     */
    public BigDecimal getQuantiteDisponible(Long articleId, Long depotId, String lotNumero) {
        if (lotNumero != null) {
            Optional<StockDisponible> stock = stockDisponibleRepository
                    .findByArticleIdAndDepotIdAndLotNumero(articleId, depotId, lotNumero);
            return stock.map(StockDisponible::getQuantiteDisponible).orElse(BigDecimal.ZERO);
        } else {
            List<StockDisponible> stocks = stockDisponibleRepository.findByArticleIdAndDepotId(articleId, depotId);
            return stocks.stream()
                    .map(s -> s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void validateTypeMouvementEntree(String type) {
        if (!type.startsWith("ENTREE_")) {
            throw new RuntimeException("Type de mouvement invalide pour une entrée: " + type);
        }
    }

    private void validateTypeMouvementSortie(String type) {
        if (!type.startsWith("SORTIE_")) {
            throw new RuntimeException("Type de mouvement invalide pour une sortie: " + type);
        }
    }

    @Transactional(readOnly = true)
    public MouvementStock trouverParId(Long id) {
        return mouvementStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mouvement non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<MouvementStock> listerTous() {
        return mouvementStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MouvementStock> listerParArticle(Long articleId) {
        return mouvementStockRepository.findByArticleId(articleId);
    }

    @Transactional(readOnly = true)
    public List<MouvementStock> listerParDepot(Long depotId) {
        return mouvementStockRepository.findByDepotId(depotId);
    }

    @Transactional(readOnly = true)
    public List<MouvementStock> listerParPeriode(LocalDate debut, LocalDate fin) {
        return mouvementStockRepository.findByDateMouvementBetween(debut, fin);
    }

    // TODO.YML Ligne 40: Vérifier les mouvements rétrodatés
    @Transactional(readOnly = true)
    public List<MouvementStock> getMouvementsRetrodates(LocalDate dateCloture) {
        return mouvementStockRepository.findMouvementsRetrodates(dateCloture);
    }

    /**
     * Récupère la quantité disponible pour un article dans un dépôt
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockDisponible(Long articleId, Long depotId) {
        return stockDisponibleRepository.findByArticleIdAndDepotId(articleId, depotId)
                .stream()
                .map(sd -> sd.getQuantitePhysique() != null ? sd.getQuantitePhysique() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
