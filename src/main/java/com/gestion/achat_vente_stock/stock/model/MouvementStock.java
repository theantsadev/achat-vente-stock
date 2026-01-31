package com.gestion.achat_vente_stock.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;

/**
 * TODO.YML Lignes 31-33: Stock > Mouvements
 * - Ligne 31: Tracer tous mouvements (entrée, sortie, transfert, ajustement)
 * - Ligne 32: Journaliser date/heure, utilisateur, dépôt, emplacement, coût
 * - Ligne 33: Numérotation automatique et non réutilisable
 * 
 * Table: mouvement_stock (schema/05_stocks.sql)
 */
@Entity
@Table(name = "mouvement_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO.YML Ligne 33: Numérotation automatique et non réutilisable
    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    // TODO.YML Ligne 32: Dépôt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    // TODO.YML Ligne 31: Type de mouvement
    // ENTREE_RECEPTION, ENTREE_RETOUR_CLIENT, ENTREE_AJUSTEMENT, ENTREE_TRANSFERT
    // SORTIE_LIVRAISON, SORTIE_CONSOMMATION, SORTIE_REBUT, SORTIE_AJUSTEMENT, SORTIE_TRANSFERT
    @Column(name = "type_mouvement")
    private String typeMouvement;

    // TODO.YML Ligne 32: Quantités
    @Column(precision = 18, scale = 4)
    private BigDecimal quantite;

    private String unite;

    // TODO.YML Ligne 32: Coût
    @Column(name = "cout_unitaire", precision = 18, scale = 6)
    private BigDecimal coutUnitaire;

    @Column(name = "valeur_totale", precision = 18, scale = 4)
    private BigDecimal valeurTotale;

    // Référence au document source (BR, BL, Transfert, Ajustement)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "type_document")
    private String typeDocument; // BON_RECEPTION, BON_LIVRAISON, TRANSFERT, AJUSTEMENT

    // TODO.YML Ligne 32: Date/heure
    @Column(name = "date_mouvement")
    private LocalDate dateMouvement;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // TODO.YML Ligne 32: Utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Utilisateur createdBy;

    // TODO.YML Ligne 32: Emplacement
    private String emplacement;

    // TODO.YML Ligne 37: Traçabilité lot
    @Column(name = "lot_numero")
    private String lotNumero;

    // TODO.YML Ligne 38: DLUO pour contrôle péremption
    private LocalDate dluo;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (dateMouvement == null) {
            dateMouvement = LocalDate.now();
        }
    }
}
