package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;



/**
 * TODO.YML Lignes 14-16: Achats > Réception
 * - Ligne 14: Enregistrer réception (contrôle quantités vs BC)
 * - Ligne 15: Générer bon de réception, scanner code-barres
 * - Ligne 16: Gérer réceptions partielles et reliquats
 * 
 * Table: bon_reception (schema/03_achats.sql)
 */
@Entity
@Table(name = "bon_reception")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonReception {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_commande_id")
    private BonCommande bonCommande;
    
    // TODO.YML Ligne 14: Magasinier réceptionnaire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magasinier_id")
    private Utilisateur magasinier;
    
    @Column(name = "numero_bl_fournisseur")
    private String numeroBlFournisseur;
    
    @Column(name = "date_bl_fournisseur")
    private LocalDate dateBlFournisseur;
    
    @Column(name = "date_reception")
    private LocalDate dateReception;
    
    private String observations;
    
    // TODO.YML Ligne 16: Statut pour réceptions partielles
    private String statut; // "PARTIELLE", "COMPLETE", "AVEC_ECART"
}
