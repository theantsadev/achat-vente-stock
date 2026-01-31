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
 * TODO.YML Lignes 20-21: Ventes > Devis
 * - Ligne 20: Créer devis/pro-forma client (articles, prix, remises)
 * - Ligne 21: Valider remises > plafond par responsable ventes
 * 
 * Table: devis (schema/04_ventes.sql)
 */
@Entity
@Table(name = "devis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique du devis (ex: DV00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Client destinataire du devis */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    /** Commercial créateur du devis */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_id")
    @ToString.Exclude
    private Utilisateur commercial;

    /** Date de création du devis */
    @Column(name = "date_devis")
    private LocalDate dateDevis;

    /** Date de validité du devis */
    @Column(name = "date_validite")
    private LocalDate dateValidite;

    /** Montant total HT du devis */
    @Column(name = "montant_total_ht", precision = 18, scale = 4)
    private BigDecimal montantTotalHt;

    /** 
     * TODO.YML Ligne 21: Remise globale à valider si > plafond 
     */
    @Column(name = "remise_globale_pourcent", precision = 9, scale = 4)
    private BigDecimal remiseGlobalePourcent;

    /** Montant TVA calculé */
    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    /** Montant TTC calculé */
    @Column(name = "montant_total_ttc", precision = 18, scale = 4)
    private BigDecimal montantTotalTtc;

    /** 
     * Statut du devis:
     * - BROUILLON: En cours de création
     * - EN_ATTENTE_VALIDATION: Remise > plafond, attente validation
     * - VALIDE: Approuvé, peut être transformé en commande
     * - ACCEPTE: Accepté par le client
     * - REFUSE: Refusé par le client
     * - EXPIRE: Date de validité dépassée
     */
    private String statut;

    /** Conditions commerciales du devis */
    @Column(name = "conditions_commerciales")
    private String conditionsCommerciales;

    /** Lignes du devis */
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<LigneDevis> lignes = new ArrayList<>();
}
