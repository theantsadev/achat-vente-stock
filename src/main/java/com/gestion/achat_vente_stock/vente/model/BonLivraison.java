package com.gestion.achat_vente_stock.vente.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Lignes 24-26: Ventes > Livraison
 * - Ligne 24: Préparer livraison (picking : article, qté, emplacement, lot)
 * - Ligne 25: Confirmer picking et générer bon de livraison
 * - Ligne 26: Bloquer livraison si stock insuffisant
 * 
 * Table: bon_livraison (schema/04_ventes.sql)
 */
@Entity
@Table(name = "bon_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique du bon de livraison (ex: BL00001) */
    @Column(nullable = false, unique = true)
    private String numero;

    /** Commande client source */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_client_id")
    @ToString.Exclude
    private CommandeClient commandeClient;

    /** Magasinier responsable du picking */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magasinier_id")
    @ToString.Exclude
    private Utilisateur magasinier;

    /** Dépôt de sortie */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    @ToString.Exclude
    private Depot depot;

    /** Date de livraison */
    @Column(name = "date_livraison")
    private LocalDate dateLivraison;

    /**
     * Statut du bon de livraison:
     * - EN_PREPARATION: Picking en cours
     * - PRET: Préparation terminée
     * - EXPEDIE: Marchandises expédiées
     * - LIVRE: Livraison confirmée
     * - ANNULE: Bon annulé
     */
    private String statut;

    /** Observations / commentaires */
    private String observations;

    /** Lignes du bon de livraison */
    @OneToMany(mappedBy = "bonLivraison", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<LigneBL> lignes = new ArrayList<>();
}
