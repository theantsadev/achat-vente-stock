package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 17: Achats > Facture Fournisseur > Lignes
 * Lignes de facture pour 3-way match
 * 
 * Table: ligne_facture_fournisseur (schema/03_achats.sql)
 */
@Entity
@Table(name = "ligne_facture_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_fournisseur_id")
    private FactureFournisseur factureFournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;

    @Column(name = "montant_ligne_ht", precision = 18, scale = 4)
    private BigDecimal montantLigneHt;
}
