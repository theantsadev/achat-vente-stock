package com.gestion.achat_vente_stock.inventaire.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Lignes 48-51: Inventaire > Saisie et Écarts
 * - Ligne 48: Afficher liste articles (théorique) par emplacement
 * - Ligne 49: Saisie quantités physiques (manuel/scan)
 * - Ligne 50: Calculer écarts (physique - théorique) en temps réel
 * - Ligne 51: Afficher écarts valorisés par emplacement/article
 * 
 * Table: ligne_inventaire (schema/06_inventaires.sql)
 */
@Entity
@Table(name = "ligne_inventaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneInventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    /**
     * TODO.YML Ligne 48: Par emplacement
     */
    @Column(name = "emplacement")
    private String emplacement;

    /**
     * Numéro de lot pour traçabilité
     */
    @Column(name = "lot_numero")
    private String lotNumero;

    /**
     * TODO.YML Ligne 46: Snapshot stock théorique à date T
     * Quantité théorique extraite du stock au moment de l'ouverture
     */
    @Column(name = "quantite_theorique", precision = 18, scale = 4)
    private BigDecimal quantiteTheorique;

    /**
     * TODO.YML Ligne 49: Saisie quantités physiques
     * Premier comptage
     */
    @Column(name = "quantite_comptee_1", precision = 18, scale = 4)
    private BigDecimal quantiteComptee1;

    /**
     * Second comptage (optionnel, pour double comptage)
     */
    @Column(name = "quantite_comptee_2", precision = 18, scale = 4)
    private BigDecimal quantiteComptee2;

    /**
     * Quantité finale retenue après validation
     */
    @Column(name = "quantite_retenue", precision = 18, scale = 4)
    private BigDecimal quantiteRetenue;

    /**
     * TODO.YML Ligne 50: Calculer écarts
     * Écart = quantité retenue - quantité théorique
     */
    @Column(name = "ecart_quantite", precision = 18, scale = 4)
    private BigDecimal ecartQuantite;

    /**
     * TODO.YML Ligne 51: Écarts valorisés
     * Valeur de l'écart = écart × coût unitaire moyen
     */
    @Column(name = "ecart_valeur", precision = 18, scale = 4)
    private BigDecimal ecartValeur;

    /**
     * Premier compteur (utilisateur)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_1_id")
    private Utilisateur compteur1;

    /**
     * Second compteur (utilisateur)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_2_id")
    private Utilisateur compteur2;

    /**
     * Date/heure du premier comptage
     */
    @Column(name = "comptage_1_at")
    private LocalDateTime comptage1At;

    /**
     * Date/heure du second comptage
     */
    @Column(name = "comptage_2_at")
    private LocalDateTime comptage2At;

    /**
     * Calcule l'écart de quantité
     */
    public void calculerEcart() {
        if (quantiteRetenue != null && quantiteTheorique != null) {
            this.ecartQuantite = quantiteRetenue.subtract(quantiteTheorique);
        }
    }
}
