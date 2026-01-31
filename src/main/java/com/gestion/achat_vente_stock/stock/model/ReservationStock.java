package com.gestion.achat_vente_stock.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Ligne 36: Stock > Réservations
 * Allocation stock FIFO/FEFO selon nature produit
 * 
 * Table: reservation_stock (schema/05_stocks.sql)
 */
@Entity
@Table(name = "reservation_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(name = "lot_numero")
    private String lotNumero;

    @Column(name = "quantite_reservee", precision = 18, scale = 4)
    private BigDecimal quantiteReservee;

    // Référence à la commande client (FK ajoutée dans module ventes)
    @Column(name = "commande_client_id")
    private Long commandeClientId;

    @Column(name = "date_reservation")
    private LocalDate dateReservation;

    // Date d'expiration de la réservation (configurable)
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    // ACTIVE, CONSOMMEE, EXPIREE, ANNULEE
    private String statut;

    @PrePersist
    protected void onCreate() {
        if (dateReservation == null) {
            dateReservation = LocalDate.now();
        }
        if (statut == null) {
            statut = "ACTIVE";
        }
    }

    /**
     * Vérifie si la réservation est expirée
     */
    public boolean isExpiree() {
        return dateExpiration != null && LocalDate.now().isAfter(dateExpiration);
    }

    /**
     * Vérifie si la réservation est active et valide
     */
    public boolean isActive() {
        return "ACTIVE".equals(statut) && !isExpiree();
    }
}
