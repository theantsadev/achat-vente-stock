package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * TODO.YML Ligne 3: Référentiels > Fournisseurs
 * Créer/modifier fournisseurs (raison sociale, contacts, conditions)
 * 
 * Table: fournisseur (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(name = "raison_sociale")
    private String raisonSociale;
    
    private String adresse;
    
    private String email;
    
    private String telephone;
    
    @Column(name = "conditions_paiement")
    private String conditionsPaiement;
    
    @Column(name = "delai_livraison_jours")
    private Integer delaiLivraisonJours;
    
    // TODO.YML Ligne 63: KPI > OTD fournisseur (On-Time Delivery)
    private String statut; // "ACTIF", "BLOQUE", "SUSPENDU"
    
    @Column(name = "motif_blocage")
    private String motifBlocage;
    
    @Column(name = "date_blocage")
    private LocalDateTime dateBlocage;
}
