package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TODO.YML Ligne 1-2: Référentiels > Articles
 * Entity pour la gestion des articles (code, nom, famille, unité)
 * avec gestion des lots/séries et dates (DLUO, DLC, traçabilité)
 * 
 * Table: article (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "article")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_id")
    private FamilleArticle famille;

    @Column(name = "unite_mesure")
    private String uniteMesure;

    private Boolean achetable;

    private Boolean vendable;

    private Boolean stockable;

    // TODO.YML Ligne 2: Gestion traçabilité lots/séries
    @Column(name = "tracabilite_lot")
    private Boolean tracabiliteLot;

    @Column(name = "dluo_obligatoire")
    private Boolean dluoObligatoire;

    // TODO.YML Ligne 39: Valorisation (FIFO/CUMP)
    @Column(name = "methode_valorisation")
    private String methodeValorisation; // "FIFO" ou "CUMP"

    @Column(name = "prix_achat_moyen", precision = 18, scale = 4)
    private BigDecimal prixAchatMoyen;

    @Column(name = "prix_vente_public", precision = 18, scale = 4)
    private BigDecimal prixVentePublic;

    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTva = new BigDecimal("20"); // TVA par défaut 20%

    @Column(name = "stock_minimum")
    private Integer stockMinimum;

    @Column(name = "stock_maximum")
    private Integer stockMaximum;

    private String statut; // "ACTIF", "BLOQUE", "OBSOLETE"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
