package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TODO.YML Ligne 29: Ventes > Encaissement
 * Enregistrer encaissements clients
 * 
 * Table: encaissement (schema/04_ventes.sql)
 */
@Entity
@Table(name = "encaissement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Encaissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique de l'encaissement (ex: ENC00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Facture concernée par l'encaissement */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_client_id")
    @ToString.Exclude
    private FactureClient factureClient;

    /** Date de l'encaissement */
    @Column(name = "date_encaissement")
    private LocalDate dateEncaissement;

    /** Montant encaissé */
    @Column(name = "montant_encaisse", precision = 18, scale = 4)
    private BigDecimal montantEncaisse;

    /**
     * Mode de paiement:
     * - ESPECES: Paiement en espèces
     * - CHEQUE: Paiement par chèque
     * - VIREMENT: Virement bancaire
     * - CARTE: Paiement par carte
     * - TRAITE: Effet de commerce
     */
    @Column(name = "mode_paiement")
    private String modePaiement;

    /** Référence du paiement (n° chèque, n° virement...) */
    private String reference;

    /**
     * Statut de l'encaissement:
     * - EN_ATTENTE: En attente de validation
     * - VALIDE: Encaissement validé
     * - REJETE: Encaissement rejeté (chèque impayé...)
     */
    private String statut;
}
