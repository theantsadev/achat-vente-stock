package com.gestion.achat_vente_stock.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Ligne 36: Stock > Réservations
 * Vue consolidée du stock disponible par article/dépôt/emplacement/lot
 * 
 * Table: stock_disponible (schema/05_stocks.sql)
 */
@Entity
@Table(name = "stock_disponible")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    private String emplacement;

    @Column(name = "lot_numero")
    private String lotNumero;

    // Quantité physiquement présente
    @Column(name = "quantite_physique", precision = 18, scale = 4)
    private BigDecimal quantitePhysique;

    // Quantité réservée pour commandes clients
    @Column(name = "quantite_reservee", precision = 18, scale = 4)
    private BigDecimal quantiteReservee;

    // Quantité disponible = physique - réservée
    @Column(name = "quantite_disponible", precision = 18, scale = 4)
    private BigDecimal quantiteDisponible;

    // TODO.YML Ligne 39: Valeur stock (FIFO/CUMP)
    @Column(name = "valeur_stock", precision = 18, scale = 4)
    private BigDecimal valeurStock;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalDateTime.now();
        // Calcul automatique de la quantité disponible
        if (quantitePhysique != null && quantiteReservee != null) {
            quantiteDisponible = quantitePhysique.subtract(quantiteReservee);
        }
    }

    /**
     * Vérifie si le stock est suffisant pour une quantité demandée
     */
    public boolean hasSuffisantStock(BigDecimal quantiteDemandee) {
        return quantiteDisponible != null && 
               quantiteDisponible.compareTo(quantiteDemandee) >= 0;
    }
}
