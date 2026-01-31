package com.gestion.achat_vente_stock.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import com.gestion.achat_vente_stock.referentiel.model.Article;

/**
 * TODO.YML Ligne 34: Stock > Transferts
 * Ligne de détail d'un transfert inter-dépôts
 * 
 * Table: ligne_transfert (schema/05_stocks.sql)
 */
@Entity
@Table(name = "ligne_transfert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"transfert"})
public class LigneTransfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id")
    private TransfertStock transfert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "quantite_demandee", precision = 18, scale = 4)
    private BigDecimal quantiteDemandee;

    @Column(name = "quantite_expedie", precision = 18, scale = 4)
    private BigDecimal quantiteExpedie;

    @Column(name = "quantite_recue", precision = 18, scale = 4)
    private BigDecimal quantiteRecue;

    @Column(name = "lot_numero")
    private String lotNumero;

    /**
     * Vérifie si la ligne a été complètement expédiée
     */
    public boolean isExpedieComplet() {
        return quantiteExpedie != null && 
               quantiteDemandee != null &&
               quantiteExpedie.compareTo(quantiteDemandee) >= 0;
    }

    /**
     * Vérifie si la ligne a été complètement reçue
     */
    public boolean isRecuComplet() {
        return quantiteRecue != null && 
               quantiteExpedie != null &&
               quantiteRecue.compareTo(quantiteExpedie) >= 0;
    }

    /**
     * Calcule l'écart entre expédié et reçu
     */
    public BigDecimal getEcart() {
        if (quantiteExpedie == null || quantiteRecue == null) {
            return BigDecimal.ZERO;
        }
        return quantiteExpedie.subtract(quantiteRecue);
    }
}
