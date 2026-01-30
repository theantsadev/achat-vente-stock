package com.gestion.achat_vente_stock.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.model.Site;

/**
 * TODO.YML Ligne 56: Sécurité > ABAC
 * Restrictions par site, dépôt, montant (attributs contextuels)
 * 
 * Table: utilisateur_role (schema/01_admin_securite.sql)
 */
@Entity
@Table(name = "utilisateur_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    
    // TODO.YML Ligne 56: ABAC - Restrictions par dépôt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;
    
    // TODO.YML Ligne 56: ABAC - Restrictions par site
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    
    // TODO.YML Ligne 56, 8: ABAC - Restriction par montant (seuils approbation)
    @Column(name = "montant_max", precision = 18, scale = 4)
    private BigDecimal montantMax;
    
    @Column(name = "date_debut")
    private LocalDateTime dateDebut;
    
    @Column(name = "date_fin")
    private LocalDateTime dateFin;
}
