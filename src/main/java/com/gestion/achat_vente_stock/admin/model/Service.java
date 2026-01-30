package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * TODO.YML Ligne 56: Sécurité > ABAC
 * Restrictions par département/service
 * 
 * Table: service (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "service")
@Data
@ToString(exclude = { "responsable" }) // Exclude the circular reference
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Service parent;
}
