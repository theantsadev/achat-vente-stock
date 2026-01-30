package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.referentiel.model.Site;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 * Créer/modifier/désactiver utilisateurs
 * 
 * Table: utilisateur (schema/01_admin_securite.sql)
 */
@Table(name = "utilisateur")
@Data
@Entity
@ToString(exclude = {"service"})  // Exclude the circular reference
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    private String nom;
    
    private String prenom;
    
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
    
    // TODO.YML Ligne 71: Multi-sites
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Utilisateur manager;
    
    private Boolean actif;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
