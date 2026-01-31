package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Lignes 22-24: Ventes > Commande Client > Lignes
 * - Ligne 22: Lignes issues du devis transformé
 * - Ligne 24: Quantités préparées pour le picking
 * 
 * Table: ligne_commande_client (schema/04_ventes.sql)
 */
@Entity
@Table(name = "ligne_commande_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Commande parent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_client_id")
    @ToString.Exclude
    private CommandeClient commandeClient;

    /** Article concerné */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @ToString.Exclude
    private Article article;

    /** Quantité commandée */
    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    /** Prix unitaire HT */
    @Column(name = "prix_unitaire_ht", precision = 18, scale = 4)
    private BigDecimal prixUnitaireHt;

    /** Remise en pourcentage */
    @Column(name = "remise_pourcent", precision = 9, scale = 4)
    private BigDecimal remisePourcent;

    /** Montant total HT de la ligne */
    @Column(name = "montant_ligne_ht", precision = 18, scale = 4)
    private BigDecimal montantLigneHt;

    /** 
     * TODO.YML Ligne 24: Quantité préparée lors du picking 
     */
    @Column(name = "quantite_preparee", precision = 18, scale = 4)
    private BigDecimal quantitePreparee;

    /** Quantité effectivement livrée */
    @Column(name = "quantite_livree", precision = 18, scale = 4)
    private BigDecimal quantiteLivree;

    /**
     * Calcule le montant de la ligne avec remise
     */
    public void calculerMontant() {
        if (quantite != null && prixUnitaireHt != null) {
            BigDecimal montantBrut = quantite.multiply(prixUnitaireHt);
            if (remisePourcent != null && remisePourcent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remise = montantBrut.multiply(remisePourcent).divide(new BigDecimal("100"));
                this.montantLigneHt = montantBrut.subtract(remise);
            } else {
                this.montantLigneHt = montantBrut;
            }
        }
    }
}
