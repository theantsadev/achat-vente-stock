package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;

/**
 * TODO.YML Lignes 11-13: Achats > Bon Commande
 * - Ligne 11: Transformer DA en BC (acheteur uniquement)
 * - Ligne 12: Validation BC par responsable achats si > seuil
 * - Ligne 13: Approbation finale DG/DAF pour signature légale
 * 
 * Table: bon_commande (schema/03_achats.sql)
 */
@Entity
@Table(name = "bon_commande")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_achat_id")
    private DemandeAchat demandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    // TODO.YML Ligne 11: Acheteur uniquement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id")
    private Utilisateur acheteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_livraison_id")
    private Depot depotLivraison;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;

    // TODO.YML Ligne 12: Montant pour validation par responsable
    @Column(name = "montant_total_ht", precision = 18, scale = 4)
    private BigDecimal montantTotalHt;

    @Column(name = "montant_tva", precision = 18, scale = 4)
    private BigDecimal montantTva;

    @Column(name = "montant_total_ttc", precision = 18, scale = 4)
    private BigDecimal montantTotalTtc;

    @Column(name = "conditions_paiement")
    private String conditionsPaiement;

    // TODO.YML Lignes 12-13: Statut pour workflow validation/approbation
    private String statut; // "BROUILLON", "EN_ATTENTE_VALIDATION", "VALIDEE", "APPROUVEE", "ENVOYEE",
                           // "ANNULEE"

    // TODO.YML Ligne 13: Approbation finale DG/DAF
    @Column(name = "approuve_at")
    private LocalDateTime approuveAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approuve_by")
    private Utilisateur approuveBy;
}
