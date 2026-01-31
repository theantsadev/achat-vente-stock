package com.gestion.achat_vente_stock.stock.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.stock.model.ReservationStock;
import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.repository.ReservationStockRepository;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Ligne 36: Stock > Réservations
 * Allocation stock FIFO/FEFO selon nature produit
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationStockService {

    private final ReservationStockRepository reservationStockRepository;
    private final StockDisponibleRepository stockDisponibleRepository;
    private final LotService lotService;
    private final AuditService auditService;

    // Statuts de réservation
    public static final String STATUT_ACTIVE = "ACTIVE";
    public static final String STATUT_CONSOMMEE = "CONSOMMEE";
    public static final String STATUT_EXPIREE = "EXPIREE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    // Durée par défaut d'une réservation (en jours)
    private static final int DUREE_RESERVATION_DEFAUT = 7;

    /**
     * TODO.YML Ligne 36: Créer une réservation de stock
     * Allocation FIFO/FEFO selon nature produit (périssables → FEFO)
     */
    public ReservationStock creerReservation(Article article, Depot depot, BigDecimal quantite,
                                              Long commandeClientId, Utilisateur utilisateur) {
        
        // Vérifier le stock disponible
        BigDecimal stockDispo = getQuantiteDisponible(article.getId(), depot.getId());
        if (stockDispo.compareTo(quantite) < 0) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + stockDispo + ", Demandé: " + quantite);
        }

        // TODO.YML Ligne 36: Sélectionner le lot selon FIFO ou FEFO
        String lotNumero = null;
        if (Boolean.TRUE.equals(article.getTracabiliteLot())) {
            lotNumero = selectionnerLot(article, quantite);
        }

        ReservationStock reservation = new ReservationStock();
        reservation.setArticle(article);
        reservation.setDepot(depot);
        reservation.setLotNumero(lotNumero);
        reservation.setQuantiteReservee(quantite);
        reservation.setCommandeClientId(commandeClientId);
        reservation.setDateReservation(LocalDate.now());
        reservation.setDateExpiration(LocalDate.now().plusDays(DUREE_RESERVATION_DEFAUT));
        reservation.setStatut(STATUT_ACTIVE);

        ReservationStock saved = reservationStockRepository.save(reservation);

        // Mettre à jour le stock disponible
        mettreAJourReservationStock(article.getId(), depot.getId(), lotNumero, quantite, true);

        // Audit
        auditService.logAction(utilisateur, "reservation_stock", saved.getId(),
                "CREATE", null, saved.toString(), null);

        return saved;
    }

    /**
     * TODO.YML Ligne 36: Sélectionner le lot selon FIFO ou FEFO
     */
    private String selectionnerLot(Article article, BigDecimal quantiteDemandee) {
        // Si article périssable (DLUO obligatoire), utiliser FEFO
        if (Boolean.TRUE.equals(article.getDluoObligatoire())) {
            List<com.gestion.achat_vente_stock.stock.model.Lot> lots = lotService.getLotsActifsFEFO(article.getId());
            for (com.gestion.achat_vente_stock.stock.model.Lot lot : lots) {
                if (lot.isDisponible()) {
                    return lot.getNumero();
                }
            }
        } else {
            // Sinon utiliser FIFO
            List<com.gestion.achat_vente_stock.stock.model.Lot> lots = lotService.getLotsActifsFIFO(article.getId());
            for (com.gestion.achat_vente_stock.stock.model.Lot lot : lots) {
                if (lot.isDisponible()) {
                    return lot.getNumero();
                }
            }
        }
        return null;
    }

    /**
     * Consommer une réservation (lors de la livraison)
     */
    public ReservationStock consommerReservation(Long reservationId, Utilisateur utilisateur) {
        ReservationStock reservation = trouverParId(reservationId);
        
        if (!STATUT_ACTIVE.equals(reservation.getStatut())) {
            throw new RuntimeException("Cette réservation ne peut pas être consommée");
        }

        reservation.setStatut(STATUT_CONSOMMEE);
        ReservationStock saved = reservationStockRepository.save(reservation);

        // Libérer la réservation dans le stock disponible
        mettreAJourReservationStock(reservation.getArticle().getId(), 
                reservation.getDepot().getId(),
                reservation.getLotNumero(),
                reservation.getQuantiteReservee(), 
                false);

        // Audit
        auditService.logAction(utilisateur, "reservation_stock", saved.getId(),
                "CONSOMMER", STATUT_ACTIVE, STATUT_CONSOMMEE, null);

        return saved;
    }

    /**
     * Annuler une réservation
     */
    public ReservationStock annulerReservation(Long reservationId, Utilisateur utilisateur) {
        ReservationStock reservation = trouverParId(reservationId);
        
        if (!STATUT_ACTIVE.equals(reservation.getStatut())) {
            throw new RuntimeException("Cette réservation ne peut pas être annulée");
        }

        reservation.setStatut(STATUT_ANNULEE);
        ReservationStock saved = reservationStockRepository.save(reservation);

        // Libérer la réservation dans le stock disponible
        mettreAJourReservationStock(reservation.getArticle().getId(), 
                reservation.getDepot().getId(),
                reservation.getLotNumero(),
                reservation.getQuantiteReservee(), 
                false);

        // Audit
        auditService.logAction(utilisateur, "reservation_stock", saved.getId(),
                "ANNULER", STATUT_ACTIVE, STATUT_ANNULEE, null);

        return saved;
    }

    /**
     * Tâche planifiée pour expirer les réservations
     */
    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 1h du matin
    public void expirerReservations() {
        LocalDate today = LocalDate.now();
        List<ReservationStock> reservationsExpirees = reservationStockRepository.findReservationsExpirees(today);
        
        for (ReservationStock reservation : reservationsExpirees) {
            reservation.setStatut(STATUT_EXPIREE);
            reservationStockRepository.save(reservation);
            
            // Libérer la réservation dans le stock disponible
            mettreAJourReservationStock(reservation.getArticle().getId(), 
                    reservation.getDepot().getId(),
                    reservation.getLotNumero(),
                    reservation.getQuantiteReservee(), 
                    false);
        }
    }

    /**
     * Mettre à jour la quantité réservée dans le stock disponible
     */
    private void mettreAJourReservationStock(Long articleId, Long depotId, String lotNumero,
                                              BigDecimal quantite, boolean ajouter) {
        List<StockDisponible> stocks;
        if (lotNumero != null) {
            stocks = stockDisponibleRepository.findByArticleIdAndDepotId(articleId, depotId);
            stocks = stocks.stream()
                    .filter(s -> lotNumero.equals(s.getLotNumero()))
                    .toList();
        } else {
            stocks = stockDisponibleRepository.findByArticleIdAndDepotId(articleId, depotId);
        }

        if (!stocks.isEmpty()) {
            StockDisponible stock = stocks.get(0);
            BigDecimal qteReservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : BigDecimal.ZERO;
            
            if (ajouter) {
                stock.setQuantiteReservee(qteReservee.add(quantite));
            } else {
                stock.setQuantiteReservee(qteReservee.subtract(quantite));
            }
            
            stock.setQuantiteDisponible(stock.getQuantitePhysique().subtract(stock.getQuantiteReservee()));
            stockDisponibleRepository.save(stock);
        }
    }

    /**
     * Obtenir la quantité disponible (non réservée)
     */
    public BigDecimal getQuantiteDisponible(Long articleId, Long depotId) {
        BigDecimal qteTotale = stockDisponibleRepository.sumQuantiteDisponibleByArticle(articleId);
        return qteTotale != null ? qteTotale : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public ReservationStock trouverParId(Long id) {
        return reservationStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée: " + id));
    }

    @Transactional(readOnly = true)
    public List<ReservationStock> listerTous() {
        return reservationStockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReservationStock> listerParCommande(Long commandeClientId) {
        return reservationStockRepository.findByCommandeClientId(commandeClientId);
    }

    @Transactional(readOnly = true)
    public List<ReservationStock> listerActives() {
        return reservationStockRepository.findByStatut(STATUT_ACTIVE);
    }
}
