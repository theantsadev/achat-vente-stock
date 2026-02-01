package com.gestion.achat_vente_stock.inventaire.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.StatutInventaire;
import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import com.gestion.achat_vente_stock.inventaire.repository.InventaireRepository;
import com.gestion.achat_vente_stock.inventaire.repository.LigneInventaireRepository;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO.YML Lignes 48-51: Inventaire > Saisie et Écarts
 * Service de gestion des lignes d'inventaire et comptage
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LigneInventaireService {

    private final LigneInventaireRepository ligneInventaireRepository;
    private final InventaireRepository inventaireRepository;
    private final StockDisponibleRepository stockDisponibleRepository;

    /**
     * TODO.YML Ligne 49: Saisie quantités physiques (manuel/scan)
     * Enregistre le premier comptage
     */
    public LigneInventaire saisirComptage1(Long ligneId, BigDecimal quantite, Utilisateur compteur) {
        LigneInventaire ligne = ligneInventaireRepository.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne non trouvée: " + ligneId));

        // Vérifier que l'inventaire est en mode comptage
        Inventaire inventaire = ligne.getInventaire();
        if (inventaire.getStatut() != StatutInventaire.EN_COMPTAGE && 
            inventaire.getStatut() != StatutInventaire.OUVERT) {
            throw new IllegalStateException("L'inventaire n'est pas en mode comptage");
        }

        ligne.setQuantiteComptee1(quantite);
        ligne.setCompteur1(compteur);
        ligne.setComptage1At(LocalDateTime.now());

        // Si pas de double comptage, la quantité retenue = comptage 1
        ligne.setQuantiteRetenue(quantite);
        
        // TODO.YML Ligne 50: Calculer écarts en temps réel
        calculerEcart(ligne);

        ligne = ligneInventaireRepository.save(ligne);
        log.info("Comptage 1 enregistré pour ligne {}: {} unités", ligneId, quantite);
        
        return ligne;
    }

    /**
     * Enregistre le second comptage (double comptage)
     */
    public LigneInventaire saisirComptage2(Long ligneId, BigDecimal quantite, Utilisateur compteur) {
        LigneInventaire ligne = ligneInventaireRepository.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne non trouvée: " + ligneId));

        Inventaire inventaire = ligne.getInventaire();
        if (inventaire.getStatut() != StatutInventaire.EN_COMPTAGE) {
            throw new IllegalStateException("L'inventaire n'est pas en mode comptage");
        }

        if (ligne.getQuantiteComptee1() == null) {
            throw new IllegalStateException("Le premier comptage doit être effectué avant le second");
        }

        // Vérifier que ce n'est pas le même compteur (séparation des tâches)
        if (ligne.getCompteur1() != null && ligne.getCompteur1().getId().equals(compteur.getId())) {
            throw new IllegalStateException("Le second compteur doit être différent du premier");
        }

        ligne.setQuantiteComptee2(quantite);
        ligne.setCompteur2(compteur);
        ligne.setComptage2At(LocalDateTime.now());

        // Si les deux comptages sont identiques, quantité retenue = comptage
        // Sinon, nécessite arbitrage (on prend la moyenne pour simplifier)
        if (ligne.getQuantiteComptee1().compareTo(quantite) == 0) {
            ligne.setQuantiteRetenue(quantite);
        } else {
            // Moyenne des deux comptages (à valider manuellement si écart)
            BigDecimal moyenne = ligne.getQuantiteComptee1().add(quantite).divide(BigDecimal.valueOf(2));
            ligne.setQuantiteRetenue(moyenne);
            log.warn("Écart entre comptages pour ligne {}: {} vs {}", ligneId, ligne.getQuantiteComptee1(), quantite);
        }

        calculerEcart(ligne);

        ligne = ligneInventaireRepository.save(ligne);
        log.info("Comptage 2 enregistré pour ligne {}: {} unités", ligneId, quantite);
        
        return ligne;
    }

    /**
     * Valide/corrige manuellement la quantité retenue
     */
    public LigneInventaire validerQuantiteRetenue(Long ligneId, BigDecimal quantiteRetenue) {
        LigneInventaire ligne = ligneInventaireRepository.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne non trouvée: " + ligneId));

        ligne.setQuantiteRetenue(quantiteRetenue);
        calculerEcart(ligne);

        return ligneInventaireRepository.save(ligne);
    }

    /**
     * TODO.YML Ligne 50: Calculer écarts (physique - théorique) en temps réel
     */
    private void calculerEcart(LigneInventaire ligne) {
        if (ligne.getQuantiteRetenue() != null && ligne.getQuantiteTheorique() != null) {
            BigDecimal ecart = ligne.getQuantiteRetenue().subtract(ligne.getQuantiteTheorique());
            ligne.setEcartQuantite(ecart);

            // TODO.YML Ligne 51: Écarts valorisés
            // Récupérer le coût unitaire moyen pour valoriser l'écart
            BigDecimal coutUnitaire = getCoutUnitaireMoyen(ligne);
            if (coutUnitaire != null) {
                ligne.setEcartValeur(ecart.multiply(coutUnitaire));
            }
        }
    }

    /**
     * Récupère le coût unitaire moyen pour un article/dépôt
     */
    private BigDecimal getCoutUnitaireMoyen(LigneInventaire ligne) {
        return stockDisponibleRepository
                .findByArticleIdAndDepotId(ligne.getArticle().getId(), ligne.getInventaire().getDepot().getId())
                .stream()
                .findFirst()
                .map(sd -> sd.getValeurStock() != null && sd.getQuantitePhysique() != null 
                           && sd.getQuantitePhysique().compareTo(BigDecimal.ZERO) > 0
                        ? sd.getValeurStock().divide(sd.getQuantitePhysique(), 4, BigDecimal.ROUND_HALF_UP)
                        : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    // ===== Méthodes de consultation =====

    public Optional<LigneInventaire> findById(Long id) {
        return ligneInventaireRepository.findById(id);
    }

    public List<LigneInventaire> findByInventaire(Long inventaireId) {
        return ligneInventaireRepository.findByInventaireIdOrderByEmplacement(inventaireId);
    }

    public List<LigneInventaire> findLignesNonComptees(Long inventaireId) {
        return ligneInventaireRepository.findLignesNonComptees(inventaireId);
    }

    public List<LigneInventaire> findLignesAvecEcart(Long inventaireId) {
        return ligneInventaireRepository.findLignesAvecEcart(inventaireId);
    }

    public List<LigneInventaire> findLignesEcartSuperieurSeuil(Long inventaireId, BigDecimal seuil) {
        return ligneInventaireRepository.findLignesEcartSuperieurSeuil(inventaireId, seuil);
    }

    /**
     * Statistiques de progression du comptage
     */
    public Map<String, Long> getStatistiquesComptage(Long inventaireId) {
        Object[] result = ligneInventaireRepository.countStatutComptage(inventaireId);
        if (result != null && result.length == 3) {
            return Map.of(
                "nonComptees", ((Number) result[0]).longValue(),
                "comptage1Fait", ((Number) result[1]).longValue(),
                "comptage2Fait", ((Number) result[2]).longValue()
            );
        }
        return Map.of("nonComptees", 0L, "comptage1Fait", 0L, "comptage2Fait", 0L);
    }

    /**
     * Somme des écarts valorisés pour un inventaire
     */
    public BigDecimal getSommeEcartsValorises(Long inventaireId) {
        BigDecimal somme = ligneInventaireRepository.sumEcartValeurByInventaire(inventaireId);
        return somme != null ? somme : BigDecimal.ZERO;
    }
}
