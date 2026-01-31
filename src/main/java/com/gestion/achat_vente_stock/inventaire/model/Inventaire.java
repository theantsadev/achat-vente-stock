package com.gestion.achat_vente_stock.inventaire.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Lignes 42-47: Inventaire > Configuration et Création
 * - Ligne 42: Types inventaire (tournant, annuel, exceptionnel)
 * - Ligne 45: Écran création inventaire (site, dépôt, type, date)
 * - Ligne 46: Générer snapshot stock théorique à date T
 * - Ligne 47: Geler mouvements sur périmètre inventorié
 * 
 * Table: inventaire (schema/06_inventaires.sql)
 */
@Entity
@Table(name = "inventaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    /**
     * TODO.YML Ligne 42: Types inventaire
     * - TOURNANT: inventaire cyclique sur une partie du stock
     * - ANNUEL: inventaire complet annuel obligatoire
     * - EXCEPTIONNEL: inventaire suite à anomalie, vol, etc.
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TypeInventaire type;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    /**
     * Statuts du workflow inventaire:
     * - BROUILLON: en cours de création
     * - OUVERT: snapshot généré, comptage en cours
     * - EN_COMPTAGE: comptage 1 ou 2 en cours
     * - EN_VALIDATION: écarts calculés, en attente de validation
     * - CLOTURE: ajustements appliqués, inventaire terminé
     * - ANNULE: inventaire annulé
     */
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInventaire statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    /**
     * TODO.YML Ligne 47: Geler mouvements sur périmètre inventorié
     * Si true, les mouvements de stock sont bloqués pour ce dépôt
     */
    @Column(name = "bloque_mouvements")
    private Boolean bloqueMouvements = false;

    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneInventaire> lignes = new ArrayList<>();

    // Enum pour les types d'inventaire
    public enum TypeInventaire {
        TOURNANT,
        ANNUEL,
        EXCEPTIONNEL
    }

    // Enum pour les statuts
    public enum StatutInventaire {
        BROUILLON,
        OUVERT,
        EN_COMPTAGE,
        EN_VALIDATION,
        CLOTURE,
        ANNULE
    }
}
