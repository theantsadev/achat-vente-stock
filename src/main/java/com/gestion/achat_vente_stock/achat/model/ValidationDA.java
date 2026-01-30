package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;


/**
 * TODO.YML Lignes 8-9: Achats > Demande Achat > Validation
 * - Ligne 8: Workflow approbation N1/N2/N3 selon seuils montant
 * - Ligne 9: Bloquer auto-approbation (créateur ≠ approbateur)
 * - Ligne 57: Séparation des tâches
 * 
 * Table: validation_da (schema/03_achats.sql)
 */
@Entity
@Table(name = "validation_da")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_achat_id")
    private DemandeAchat demandeAchat;
    
    // TODO.YML Ligne 9: Valideur ≠ créateur (règle métier)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valideur_id")
    private Utilisateur valideur;
    
    // TODO.YML Ligne 8: Niveau d'approbation (N1, N2, N3)
    private Integer niveau; // 1, 2, 3
    
    private String decision; // "APPROUVEE", "REJETEE", "EN_ATTENTE"
    
    private String commentaire;
    
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
}
