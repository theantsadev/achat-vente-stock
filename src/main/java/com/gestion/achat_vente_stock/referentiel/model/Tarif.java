package com.gestion.achat_vente_stock.referentiel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * TODO.YML Ligne 6: Référentiels > Taxes/Tarifs
 * Configurer tarifs par article/client
 * 
 * Table: tarif (schema/02_referentiels.sql)
 */
@Entity
@Table(name = "tarif")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarif {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String libelle;
    
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;
    
    @Column(name = "date_fin")
    private LocalDateTime dateFin;
}
