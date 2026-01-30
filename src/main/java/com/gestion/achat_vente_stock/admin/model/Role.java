package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 55: Sécurité > RBAC > Rôles
 * Implémenter rôles (Acheteur, Magasinier, Vendeur, DAF, etc.)
 * 
 * Table: role (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String libelle;
    
    private String description;
}
