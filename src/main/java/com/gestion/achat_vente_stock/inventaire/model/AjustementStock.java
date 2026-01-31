package com.gestion.achat_vente_stock.inventaire.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Lignes 52-54: Inventaire > Ajustements
 * - Ligne 52: Valider ajustements si écart > seuil (chef magasin)
 * - Ligne 53: Appliquer ajustements au stock et journaliser
 * - Ligne 54: Interdire : même personne comptage + validation ajustement
 * 
 * Table: ajustement_stock (schema/06_inventaires.sql)
 */
@Entity
@Table(name = "ajustement_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjustementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(name = "lot_numero")
    private String lotNumero;

    /**
     * Quantité avant ajustement (théorique)
     */
    @Column(name = "quantite_avant", precision = 18, scale = 4)
    private BigDecimal quantiteAvant;

    /**
     * Quantité après ajustement (comptée)
     */
    @Column(name = "quantite_apres", precision = 18, scale = 4)
    private BigDecimal quantiteApres;

    /**
     * Écart = quantité après - quantité avant
     */
    @Column(name = "ecart", precision = 18, scale = 4)
    private BigDecimal ecart;

    /**
     * Valeur monétaire de l'ajustement
     */
    @Column(name = "valeur_ajustement", precision = 18, scale = 4)
    private BigDecimal valeurAjustement;

    /**
     * Motif de l'ajustement
     * - INVENTAIRE: écart constaté lors de l'inventaire
     * - CASSE: marchandise cassée
     * - VOL: vol constaté
     * - PEREMPTION: produit périmé
     * - ERREUR_SAISIE: correction d'erreur
     * - AUTRE: autre motif
     */
    @Column(name = "motif")
    @Enumerated(EnumType.STRING)
    private MotifAjustement motif;

    /**
     * Justification détaillée
     */
    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    /**
     * Utilisateur ayant demandé l'ajustement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_by")
    private Utilisateur demandeBy;

    /**
     * TODO.YML Ligne 52: Valider ajustements si écart > seuil
     * Utilisateur ayant validé l'ajustement (chef magasin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_by")
    private Utilisateur valideBy;

    @Column(name = "demande_at")
    private LocalDateTime demandeAt;

    @Column(name = "valide_at")
    private LocalDateTime valideAt;

    /**
     * Statut de l'ajustement:
     * - EN_ATTENTE: créé, en attente de validation
     * - VALIDE: validé par chef magasin
     * - APPLIQUE: appliqué au stock
     * - REFUSE: refusé
     * - ANNULE: annulé
     */
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutAjustement statut;

    // Enum pour les motifs
    public enum MotifAjustement {
        INVENTAIRE,
        CASSE,
        VOL,
        PEREMPTION,
        ERREUR_SAISIE,
        AUTRE
    }

    // Enum pour les statuts
    public enum StatutAjustement {
        EN_ATTENTE,
        VALIDE,
        APPLIQUE,
        REFUSE,
        ANNULE
    }

    /**
     * Calcule l'écart
     */
    public void calculerEcart() {
        if (quantiteApres != null && quantiteAvant != null) {
            this.ecart = quantiteApres.subtract(quantiteAvant);
        }
    }
}
