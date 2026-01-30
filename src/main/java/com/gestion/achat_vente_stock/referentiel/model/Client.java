package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * TODO.YML Ligne 4: Référentiels > Clients
 * Créer/modifier clients (infos légales, adresses, tarifs)
 * 
 * Table: client (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(name = "raison_sociale")
    private String raisonSociale;
    
    private String adresse;
    
    private String email;
    
    @Column(name = "limite_credit", precision = 18, scale = 4)
    private BigDecimal limiteCredit;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarif_id")
    private Tarif tarif;
    
    @Column(name = "conditions_paiement")
    private String conditionsPaiement;
    
    private String statut; // "ACTIF", "BLOQUE", "SUSPENDU"
    
    @Column(name = "motif_blocage")
    private String motifBlocage;
}
