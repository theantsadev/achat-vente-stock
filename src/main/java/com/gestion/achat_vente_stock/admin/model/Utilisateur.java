package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.referentiel.model.Site;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 * Créer/modifier/désactiver utilisateurs
 * 
 * Table: utilisateur (schema/02_admin_securite.sql)
 */
@Table(name = "utilisateur")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"service", "manager", "roles"})
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    // Mot de passe BCrypt par défaut = 'password123'
    @Column(nullable = false)
    private String password = "$2a$10$N9qo8uLOickgx2ZMRZoMy.MqN8bLXABW6e/MvW9.9WKF/FO8VHzCy";
    
    private String nom;
    
    private String prenom;
    
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Utilisateur manager;
    
    @Column(nullable = false)
    private Boolean actif = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Relations pour récupérer les rôles de l'utilisateur
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private List<UtilisateurRole> roles = new ArrayList<>();
    
    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }
}
