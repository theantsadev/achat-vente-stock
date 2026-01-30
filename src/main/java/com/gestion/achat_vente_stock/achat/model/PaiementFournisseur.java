package com.gestion.achat_vente_stock.achat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TODO.YML Ligne 19: Achats > Paiement
 * Enregistrer paiements fournisseurs
 * 
 * Table: paiement_fournisseur (schema/03_achats.sql)
 */
@Entity
@Table(name = "paiement_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementFournisseur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_fournisseur_id")
    private FactureFournisseur factureFournisseur;
    
    @Column(name = "date_paiement")
    private LocalDate datePaiement;
    
    @Column(name = "montant_paye", precision = 18, scale = 4)
    private BigDecimal montantPaye;
    
    @Column(name = "mode_paiement")
    private String modePaiement; // "VIREMENT", "CHEQUE", "ESPECES", "CARTE"
    
    @Column(name = "reference_virement")
    private String referenceVirement;
    
    private String statut; // "EN_ATTENTE", "EXECUTE", "ANNULE"
}
