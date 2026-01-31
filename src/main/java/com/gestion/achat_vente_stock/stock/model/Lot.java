package com.gestion.achat_vente_stock.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;

/**
 * TODO.YML Lignes 37-38: Stock > Lots/Séries
 * - Ligne 37: Traçabilité lot obligatoire sur familles définies
 * - Ligne 38: Bloquer automatiquement lots expirés/non conformes
 * 
 * Table: lot (schema/05_stocks.sql)
 */
@Entity
@Table(name = "lot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO.YML Ligne 37: Numéro de lot unique
    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @Column(name = "date_fabrication")
    private LocalDate dateFabrication;

    // TODO.YML Ligne 38: DLUO - Date Limite d'Utilisation Optimale
    private LocalDate dluo;

    // TODO.YML Ligne 38: DLC - Date Limite de Consommation
    private LocalDate dlc;

    // TODO.YML Ligne 38: Statut du lot (ACTIF, BLOQUE, EXPIRE, NON_CONFORME)
    private String statut;

    // TODO.YML Ligne 38: Motif de blocage si lot non conforme
    @Column(name = "motif_blocage")
    private String motifBlocage;

    @Column(name = "bloque_at")
    private LocalDateTime bloqueAt;

    /**
     * Vérifie si le lot est expiré (basé sur DLC ou DLUO)
     */
    public boolean isExpire() {
        LocalDate today = LocalDate.now();
        if (dlc != null && dlc.isBefore(today)) {
            return true;
        }
        return dluo != null && dluo.isBefore(today);
    }

    /**
     * Vérifie si le lot est disponible pour utilisation
     */
    public boolean isDisponible() {
        return "ACTIF".equals(statut) && !isExpire();
    }
}
