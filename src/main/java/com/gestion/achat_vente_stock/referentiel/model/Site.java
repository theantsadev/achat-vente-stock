package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 5, 71: Référentiels > Sites
 * Gestion multi-sites/multi-entités légales
 * 
 * Table: site (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "site")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Site {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String libelle;
    
    private String ville;
    
    private String pays;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_legale_id")
    private EntiteLegale entiteLegale;
}
