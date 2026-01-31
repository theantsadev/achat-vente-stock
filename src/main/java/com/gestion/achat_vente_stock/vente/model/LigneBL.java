package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 24: Ventes > Livraison > Lignes
 * Lignes du bon de livraison avec traçabilité lot
 * 
 * Table: ligne_bl (schema/04_ventes.sql)
 */
@Entity
@Table(name = "ligne_bl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bon de livraison parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_livraison_id")
    @ToString.Exclude
    private BonLivraison bonLivraison;

    /** Ligne de commande source */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_commande_id")
    @ToString.Exclude
    private LigneCommandeClient ligneCommande;

    /** Article concerné */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @ToString.Exclude
    private Article article;

    /** Quantité livrée */
    @Column(name = "quantite_livree", precision = 18, scale = 4)
    private BigDecimal quantiteLivree;

    /** Numéro de lot (traçabilité) */
    @Column(name = "lot_numero")
    private String lotNumero;

    /** Date Limite d'Utilisation Optimale */
    private LocalDate dluo;
}
