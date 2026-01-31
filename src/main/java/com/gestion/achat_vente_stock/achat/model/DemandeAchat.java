package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.admin.model.Service;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;

/**
 * TODO.YML Lignes 7-9: Achats > Demande Achat
 * - Ligne 7: Créer demande d'achat (articles, quantités, fournisseur)
 * - Ligne 8: Workflow approbation N1/N2/N3 selon seuils montant
 * - Ligne 9: Bloquer auto-approbation (créateur ≠ approbateur)
 * 
 * Table: demande_achat (schema/03_achats.sql)
 */
@Entity
@Table(name = "demande_achat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    // TODO.YML Ligne 7: Demandeur de la DA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    // TODO.YML Ligne 56: ABAC - Restriction par service
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "date_besoin")
    private LocalDate dateBesoin;

    private String justification;

    // TODO.YML Ligne 8: Montant pour workflow d'approbation
    @Column(name = "montant_estime_ht", precision = 18, scale = 4)
    private BigDecimal montantEstimeHt;

    private String urgence; // "NORMALE", "URGENTE", "CRITIQUE"

    // TODO.YML Ligne 8: Statut pour workflow
    private String statut; // "BROUILLON", "EN_ATTENTE", "APPROUVEE", "REJETEE", "ANNULEE"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Lignes de la demande d'achat
    @OneToMany(mappedBy = "demandeAchat", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LigneDA> lignes = new ArrayList<>();
}
