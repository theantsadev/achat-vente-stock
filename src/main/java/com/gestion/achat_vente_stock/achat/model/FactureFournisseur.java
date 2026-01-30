package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;


/**
 * TODO.YML Lignes 17-18: Achats > Facture Fournisseur
 * - Ligne 17: Rapprocher facture vs réception vs BC (3-way match)
 * - Ligne 18: Bloquer paiement si écart non résolu
 * - Ligne 58: Séparation tâches (réception ≠ validation facture)
 * 
 * Table: facture_fournisseur (schema/03_achats.sql)
 */
@Entity
@Table(name = "facture_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureFournisseur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String numero;
    
    @Column(name = "numero_facture_fournisseur")
    private String numeroFactureFournisseur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_commande_id")
    private BonCommande bonCommande;
    
    @Column(name = "date_facture")
    private LocalDate dateFacture;
    
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
    
    @Column(name = "montant_ht", precision = 18, scale = 4)
    private BigDecimal montantHt;
    
    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;
    
    @Column(name = "montant_ttc", precision = 18, scale = 4)
    private BigDecimal montantTtc;
    
    // TODO.YML Ligne 18: Statut pour blocage paiement
    private String statut; // "EN_ATTENTE", "VALIDEE", "BLOQUEE", "PAYEE"
    
    // TODO.YML Ligne 17: 3-way match (facture vs réception vs BC)
    @Column(name = "three_way_match_ok")
    private Boolean threeWayMatchOk;
    
    @Column(name = "ecarts_three_way", columnDefinition = "TEXT")
    private String ecartsThreeWay;
    
    // TODO.YML Ligne 58: Valideur ≠ réceptionnaire
    @Column(name = "validee_at")
    private LocalDateTime valideeAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validee_by")
    private Utilisateur valideeBy;
}
