package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;


/**
 * TODO.YML Lignes 14-16: Achats > Réception > Lignes
 * - Ligne 14: Contrôle quantités vs BC
 * - Ligne 16: Réceptions partielles et reliquats
 * 
 * Table: ligne_br (schema/03_achats.sql)
 */
@Entity
@Table(name = "ligne_br")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBR {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_reception_id")
    private BonReception bonReception;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_bc_id")
    private LigneBC ligneBc;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
    
    // TODO.YML Ligne 14: Contrôle quantités
    @Column(name = "quantite_commandee", precision = 18, scale = 4)
    private BigDecimal quantiteCommandee;
    
    @Column(name = "quantite_recue", precision = 18, scale = 4)
    private BigDecimal quantiteRecue;
    
    @Column(name = "quantite_conforme", precision = 18, scale = 4)
    private BigDecimal quantiteConforme;
    
    @Column(name = "quantite_non_conforme", precision = 18, scale = 4)
    private BigDecimal quantiteNonConforme;
    
    @Column(name = "motif_non_conformite")
    private String motifNonConformite;
}
