package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;

/**
 * TODO.YML Ligne 10: Achats > Pro-forma
 * - Générer pro-forma depuis DA approuvée
 * - Étape intermédiaire avant transformation en BC
 * - Permet la négociation avec fournisseur (prix, délais)
 * 
 * Table: proforma (schema/03_achats.sql)
 */
@Entity
@Table(name = "proforma")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Proforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    // Lien vers DA approuvée
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_achat_id", nullable = false)
    @ToString.Exclude
    private DemandeAchat demandeAchat;

    // Fournisseur proposé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    // Créée par (acheteur)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    @ToString.Exclude
    private Utilisateur createur;

    // Dates
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_validite")
    private LocalDate dateValidite;

    @Column(name = "delai_livraison_jours")
    private Integer delaiLivraisonJours; // en jours

    // Montants
    @Column(name = "montant_total_ht", precision = 18, scale = 4)
    private BigDecimal montantTotalHt;

    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    @Column(name = "montant_total_ttc", precision = 18, scale = 4)
    private BigDecimal montantTotalTtc;

    // Statut workflow
    @Column(name = "statut", nullable = false)
    private String statut; // "BROUILLON", "ACCEPTE", "REJETEE", "TRANSFORMEE_EN_BC"

    // Conditions commerciales
    @Column(columnDefinition = "TEXT")
    private String conditionsCommerciales;

    @Column(columnDefinition = "TEXT")
    private String remarques;

    // Historique approbation
    @Column(name = "valide_at")
    private LocalDateTime valideAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_by")
    @ToString.Exclude
    private Utilisateur valideBy;

    // Lignes de la proforma
    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<LigneProforma> lignes = new ArrayList<>();
}
