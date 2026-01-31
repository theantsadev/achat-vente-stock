package com.gestion.achat_vente_stock.stock.model;

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
 * TODO.YML Lignes 34-35: Stock > Transferts
 * - Ligne 34: Gérer transferts entre dépôts/sites
 * - Ligne 35: Validation transferts par chef magasin
 * 
 * Table: transfert_stock (schema/05_stocks.sql)
 */
@Entity
@Table(name = "transfert_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"lignes"})
public class TransfertStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    // TODO.YML Ligne 34: Dépôt source
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_source_id")
    private Depot depotSource;

    // TODO.YML Ligne 34: Dépôt destination
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_destination_id")
    private Depot depotDestination;

    // Utilisateur qui a demandé le transfert
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    @Column(name = "date_demande")
    private LocalDate dateDemande;

    @Column(name = "date_expedition")
    private LocalDate dateExpedition;

    @Column(name = "date_reception")
    private LocalDate dateReception;

    // BROUILLON, DEMANDE, VALIDEE, EN_TRANSIT, RECUE, ANNULEE
    private String statut;

    // TODO.YML Ligne 35: Chef magasin qui valide
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_by")
    private Utilisateur valideBy;

    @OneToMany(mappedBy = "transfert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneTransfert> lignes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateDemande == null) {
            dateDemande = LocalDate.now();
        }
        if (statut == null) {
            statut = "BROUILLON";
        }
    }

    /**
     * Ajouter une ligne au transfert
     */
    public void addLigne(LigneTransfert ligne) {
        lignes.add(ligne);
        ligne.setTransfert(this);
    }

    /**
     * Supprimer une ligne du transfert
     */
    public void removeLigne(LigneTransfert ligne) {
        lignes.remove(ligne);
        ligne.setTransfert(null);
    }
}
