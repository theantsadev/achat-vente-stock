package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 10: Achats > Pro-forma > Lignes
 * Lignes du pro-forma
 * 
 * Table: ligne_proforma (schema/03_achats.sql)
 */
@Entity
@Table(name = "ligne_proforma")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneProforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_id")
    @ToString.Exclude
    private Proforma proforma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;

    @Column(name = "remise_pourcent", precision = 9, scale = 4)
    private BigDecimal remisePourcent;
}
