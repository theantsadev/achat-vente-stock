package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Client;

/**
 * TODO.YML Ligne 30: Ventes > Avoirs
 * Créer avoirs (retour, erreur prix, casse) avec validation
 * 
 * Table: avoir_client (schema/04_ventes.sql)
 */
@Entity
@Table(name = "avoir_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvoirClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique de l'avoir (ex: AV00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Facture d'origine (si avoir sur facture) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_client_id")
    @ToString.Exclude
    private FactureClient factureClient;

    /** Client concerné */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    /** Date de l'avoir */
    @Column(name = "date_avoir")
    private LocalDate dateAvoir;

    /** Montant HT de l'avoir */
    @Column(name = "montant_ht", precision = 18, scale = 4)
    private BigDecimal montantHt;

    /** Montant TVA */
    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    /** Montant TTC */
    @Column(name = "montant_ttc", precision = 18, scale = 4)
    private BigDecimal montantTtc;

    /**
     * Motif de l'avoir:
     * - RETOUR: Retour marchandise
     * - ERREUR_PRIX: Erreur de facturation
     * - CASSE: Marchandise endommagée
     * - COMMERCIAL: Geste commercial
     * - AUTRE: Autre motif
     */
    private String motif;

    /**
     * Statut de l'avoir:
     * - BROUILLON: En cours de création
     * - EN_ATTENTE_VALIDATION: Attente validation responsable
     * - VALIDE: Avoir validé
     * - APPLIQUE: Avoir appliqué (utilisé)
     * - ANNULE: Avoir annulé
     */
    private String statut;

    /** Valideur de l'avoir (double validation) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_by")
    @ToString.Exclude
    private Utilisateur valideBy;
}
