package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;


/**
 * TODO.YML Lignes 11-13: Achats > Bon Commande > Lignes
 * Lignes du bon de commande
 * 
 * Table: ligne_bc (schema/03_achats.sql)
 */
@Entity
@Table(name = "ligne_bc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBC {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_commande_id")
    private BonCommande bonCommande;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
    
    @Column(name = "reference_fournisseur")
    private String referenceFournisseur;
    
    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;
    
    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;
    
    @Column(name = "remise_pourcent", precision = 9, scale = 4)
    private BigDecimal remisePourcent;
    
    @Column(name = "montant_ligne_ht", precision = 18, scale = 4)
    private BigDecimal montantLigneHt;
}
