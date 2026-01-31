package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 20: Ventes > Devis > Lignes
 * Lignes du devis (articles, quantités, prix, remises)
 * 
 * Table: ligne_devis (schema/04_ventes.sql)
 */
@Entity
@Table(name = "ligne_devis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Devis parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id")
    @ToString.Exclude
    private Devis devis;

    /** Article concerné */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @ToString.Exclude
    private Article article;

    /** Quantité demandée */
    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    /** Prix unitaire HT */
    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;

    /** Remise en pourcentage sur la ligne */
    @Column(name = "remise_pourcent", precision = 9, scale = 4)
    private BigDecimal remisePourcent;

    /** Montant total HT de la ligne (après remise) */
    @Column(name = "montant_ligne_ht", precision = 18, scale = 4)
    private BigDecimal montantLigneHt;

    /**
     * Calcule le montant de la ligne avec remise
     */
    public void calculerMontantHt() {
        if (quantite != null && prixUnitaireHt != null) {
            BigDecimal montantBrut = quantite.multiply(prixUnitaireHt);
            if (remisePourcent != null && remisePourcent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remise = montantBrut.multiply(remisePourcent).divide(new BigDecimal("100"));
                this.montantLigneHt = montantBrut.subtract(remise);
            } else {
                this.montantLigneHt = montantBrut;
            }
        }
    }
}
