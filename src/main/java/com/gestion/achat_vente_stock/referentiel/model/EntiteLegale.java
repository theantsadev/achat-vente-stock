package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 71: Admin > Multi-entités
 * Gérer multi-entités légales
 * 
 * Table: entite_legale (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "entite_legale")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntiteLegale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "raison_sociale")
    private String raisonSociale;
    
    private String siret;
    
    private String pays;
    
    @Column(name = "forme_juridique")
    private String formeJuridique;
}
