package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * TODO.YML Ligne 61: Sécurité > Délégation
 * Gestion accès temporaires avec expiration et justification
 * 
 * Table: delegation (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "delegation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delegation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegant_id")
    private Utilisateur delegant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegataire_id")
    private Utilisateur delegataire;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;
    
    @Column(name = "date_fin")
    private LocalDateTime dateFin;
    
    private String justification;
    
    private String statut; // "ACTIF", "EXPIRE", "REVOQUE"
}
