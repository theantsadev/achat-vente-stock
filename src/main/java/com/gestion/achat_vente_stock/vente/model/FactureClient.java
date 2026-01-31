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

import com.gestion.achat_vente_stock.referentiel.model.Client;

/**
 * TODO.YML Lignes 27-28: Ventes > Facture Client
 * - Ligne 27: Générer facture client depuis livraison
 * - Ligne 28: Contrôler TVA et conformité (Comptable)
 * 
 * Table: facture_client (schema/04_ventes.sql)
 */
@Entity
@Table(name = "facture_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique de la facture (ex: FC00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Commande client source */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_client_id")
    @ToString.Exclude
    private CommandeClient commandeClient;

    /** Bon de livraison source */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_livraison_id")
    @ToString.Exclude
    private BonLivraison bonLivraison;

    /** Client facturé */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    /** Date de la facture */
    @Column(name = "date_facture")
    private LocalDate dateFacture;

    /** Date d'échéance de paiement */
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    /** Montant HT */
    @Column(name = "montant_ht", precision = 18, scale = 4)
    private BigDecimal montantHt;

    /** 
     * TODO.YML Ligne 28: Montant TVA à contrôler 
     */
    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    /** Montant TTC */
    @Column(name = "montant_ttc", precision = 18, scale = 4)
    private BigDecimal montantTtc;

    /**
     * Statut de la facture:
     * - BROUILLON: En cours de création
     * - VALIDEE: Validée, envoyée au client
     * - PAYEE_PARTIELLEMENT: Paiement partiel reçu
     * - PAYEE: Intégralement payée
     * - ANNULEE: Facture annulée
     */
    private String statut;

    /** Indicateur de paiement total */
    @Column(name = "est_payee")
    private Boolean estPayee = false;

    /** Lignes de la facture */
    @OneToMany(mappedBy = "factureClient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<LigneFactureClient> lignes = new ArrayList<>();

    /** Encaissements associés */
    @OneToMany(mappedBy = "factureClient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Encaissement> encaissements = new ArrayList<>();
}
