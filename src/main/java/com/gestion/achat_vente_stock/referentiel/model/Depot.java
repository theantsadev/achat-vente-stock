package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 5: Référentiels > Dépôts
 * Créer dépôts/emplacements (site, zone, adresse)
 * 
 * Table: depot (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "depot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Depot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String libelle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    
    private String adresse;
    
    private String type; // "PRINCIPAL", "SECONDAIRE", "TRANSIT"
    
    private Boolean actif;
}
