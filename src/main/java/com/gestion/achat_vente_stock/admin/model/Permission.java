package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO.YML Ligne 55, 69: Sécurité > RBAC > Permissions
 * Matrice rôles et permissions par module/action
 * 
 * Table: permission (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String module; // "ACHATS", "VENTES", "STOCK", etc.
    
    private String action; // "CREATE", "READ", "UPDATE", "DELETE", "APPROVE"
    
    private String description;
}
