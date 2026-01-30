package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * TODO.YML Ligne 60: Sécurité > Audit
 * Journalisation complète non modifiable (qui/quoi/quand)
 * 
 * Table: audit_log (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
    
    @Column(name = "table_name")
    private String tableName;
    
    @Column(name = "record_id")
    private Long recordId;
    
    private String action; // "CREATE", "UPDATE", "DELETE", "APPROVE"
    
    @Column(columnDefinition = "TEXT")
    private String avant; // État avant modification (JSON)
    
    @Column(columnDefinition = "TEXT")
    private String apres; // État après modification (JSON)
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
