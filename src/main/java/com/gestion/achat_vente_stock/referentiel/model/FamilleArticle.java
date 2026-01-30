package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 1: Référentiels > Articles > Famille
 * Entity pour les familles d'articles
 * 
 * Table: famille_article (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "famille_article")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilleArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String libelle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FamilleArticle parent;
    
    // TODO.YML Ligne 43: Familles soumises à inventaire obligatoire
    @Column(name = "tracabilite_lot_defaut")
    private Boolean tracabiliteLotDefaut;
    
    @Column(name = "methode_valorisation_defaut")
    private String methodeValorisationDefaut;
}
