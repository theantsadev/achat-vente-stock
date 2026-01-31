package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Client;

/**
 * TODO.YML Lignes 22-23: Ventes > Commande Client
 * - Ligne 22: Transformer devis en commande client
 * - Ligne 23: Réserver stock à la commande (configurable)
 * 
 * Table: commande_client (schema/04_ventes.sql)
 */
@Entity
@Table(name = "commande_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique de la commande (ex: CC00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Devis source (optionnel) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id")
    @ToString.Exclude
    private Devis devis;

    /** Client de la commande */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    /** Commercial responsable */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_id")
    @ToString.Exclude
    private Utilisateur commercial;

    /** Date de la commande */
    @Column(name = "date_commande")
    private LocalDate dateCommande;

    /** Date de livraison prévue */
    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;

    /** Montant total HT */
    @Column(name = "montant_total_ht", precision = 18, scale = 4)
    private BigDecimal montantTotalHt;

    /** Montant TVA */
    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    /** Montant total TTC */
    @Column(name = "montant_total_ttc", precision = 18, scale = 4)
    private BigDecimal montantTotalTtc;

    /**
     * Statut de la commande:
     * - BROUILLON: En cours de création
     * - CONFIRMEE: Confirmée, prête pour préparation
     * - EN_PREPARATION: En cours de picking
     * - PREPAREE: Picking terminé
     * - LIVREE: Livraison effectuée
     * - FACTUREE: Facture émise
     * - ANNULEE: Commande annulée
     */
    private String statut;

    /**
     * TODO.YML Ligne 23: Indicateur de réservation de stock
     */
    @Column(name = "stock_reserve")
    private Boolean stockReserve;

    /** Lignes de la commande */
    @OneToMany(mappedBy = "commandeClient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<LigneCommandeClient> lignes = new ArrayList<>();
}
