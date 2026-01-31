package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 7: Achats > Demande Achat > Lignes
 * Lignes de la demande d'achat (articles, quantités)
 * 
 * Table: ligne_da (schema/03_achats.sql)
 */
@Entity
@Table(name = "ligne_da")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneDA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_achat_id")
    private DemandeAchat demandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    @Column(name = "prix_estime_ht", precision = 18, scale = 4)
    private BigDecimal prixEstimeHt;

    private String commentaire;
}
