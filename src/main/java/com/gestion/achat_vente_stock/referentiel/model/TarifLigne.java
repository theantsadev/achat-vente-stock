package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * TODO.YML Ligne 6: Référentiels > Taxes/Tarifs
 * Lignes de tarif par article
 * 
 * Table: tarif_ligne (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "tarif_ligne")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifLigne {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarif_id")
    private Tarif tarif;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
    
    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;
    
    @Column(name = "remise_pourcent", precision = 9, scale = 4)
    private BigDecimal remisePourcent;
}
