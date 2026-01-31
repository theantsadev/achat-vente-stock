package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 27: Ventes > Facture Client > Lignes
 * Lignes de la facture client
 * 
 * Table: ligne_facture_client (schema/04_ventes.sql)
 */
@Entity
@Table(name = "ligne_facture_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Facture parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_client_id")
    @ToString.Exclude
    private FactureClient factureClient;

    /** Article facturé */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @ToString.Exclude
    private Article article;

    /** Quantité facturée */
    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    /** Prix unitaire HT */
    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;

    /** Montant total HT de la ligne */
    @Column(name = "montant_ligne_ht", precision = 18, scale = 4)
    private BigDecimal montantLigneHt;

    /**
     * Calcule le montant de la ligne
     */
    public void calculerMontant() {
        if (quantite != null && prixUnitaireHt != null) {
            this.montantLigneHt = quantite.multiply(prixUnitaireHt);
        }
    }
}
