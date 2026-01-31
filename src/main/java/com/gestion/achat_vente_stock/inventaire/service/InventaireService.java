package com.gestion.achat_vente_stock.inventaire.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.StatutInventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.TypeInventaire;
import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import com.gestion.achat_vente_stock.inventaire.repository.InventaireRepository;
import com.gestion.achat_vente_stock.inventaire.repository.LigneInventaireRepository;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.model.StockDisponible;
import com.gestion.achat_vente_stock.stock.repository.StockDisponibleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 42-47: Inventaire > Configuration et Création
 * Service de gestion des inventaires
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventaireService {

    private final InventaireRepository inventaireRepository;
    private final LigneInventaireRepository ligneInventaireRepository;
    private final StockDisponibleRepository stockDisponibleRepository;
    private final DepotRepository depotRepository;

    /**
     * Génère un numéro d'inventaire unique
     * Format: INV-YYYYMMDD-XXX
     */
    private String generateNumero() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long count = inventaireRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /**
     * TODO.YML Ligne 45: Créer écran création inventaire
     * Crée un nouvel inventaire en statut BROUILLON
     */
    public Inventaire creer(Long depotId, TypeInventaire type, Utilisateur responsable) {
        // Vérifier qu'il n'y a pas d'inventaire en cours pour ce dépôt
        if (inventaireRepository.existsInventaireEnCours(depotId)) {
            throw new IllegalStateException("Un inventaire est déjà en cours pour ce dépôt");
        }

        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new IllegalArgumentException("Dépôt non trouvé: " + depotId));

        Inventaire inventaire = new Inventaire();
        inventaire.setNumero(generateNumero());
        inventaire.setDepot(depot);
        inventaire.setType(type);
        inventaire.setDateDebut(LocalDate.now());
        inventaire.setStatut(StatutInventaire.BROUILLON);
        inventaire.setResponsable(responsable);
        inventaire.setBloqueMouvements(false);

        inventaire = inventaireRepository.save(inventaire);
        log.info("Inventaire créé: {} pour dépôt {}", inventaire.getNumero(), depot.getCode());
        
        return inventaire;
    }

    /**
     * TODO.YML Ligne 46: Générer snapshot stock théorique à date T
     * Ouvre l'inventaire et génère les lignes à partir du stock actuel
     */
    public Inventaire ouvrir(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        if (inventaire.getStatut() != StatutInventaire.BROUILLON) {
            throw new IllegalStateException("L'inventaire doit être en BROUILLON pour être ouvert");
        }

        // Générer les lignes à partir du stock disponible
        List<StockDisponible> stocks = stockDisponibleRepository.findByDepotId(inventaire.getDepot().getId());
        
        for (StockDisponible stock : stocks) {
            LigneInventaire ligne = new LigneInventaire();
            ligne.setInventaire(inventaire);
            ligne.setArticle(stock.getArticle());
            ligne.setLotNumero(stock.getLotNumero());
            ligne.setQuantiteTheorique(stock.getQuantitePhysique());
            ligne.setEmplacement(stock.getDepot().getCode()); // Simplification: utiliser le code dépôt
            ligneInventaireRepository.save(ligne);
        }

        // TODO.YML Ligne 47: Geler mouvements
        inventaire.setBloqueMouvements(true);
        inventaire.setStatut(StatutInventaire.OUVERT);
        inventaire = inventaireRepository.save(inventaire);
        
        log.info("Inventaire {} ouvert avec {} lignes", inventaire.getNumero(), stocks.size());
        
        return inventaire;
    }

    /**
     * Passe l'inventaire en mode comptage
     */
    public Inventaire demarrerComptage(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        if (inventaire.getStatut() != StatutInventaire.OUVERT) {
            throw new IllegalStateException("L'inventaire doit être OUVERT pour démarrer le comptage");
        }

        inventaire.setStatut(StatutInventaire.EN_COMPTAGE);
        return inventaireRepository.save(inventaire);
    }

    /**
     * Termine le comptage et passe en validation
     */
    public Inventaire terminerComptage(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        if (inventaire.getStatut() != StatutInventaire.EN_COMPTAGE) {
            throw new IllegalStateException("L'inventaire doit être EN_COMPTAGE pour terminer");
        }

        // Vérifier que toutes les lignes ont au moins un comptage
        List<LigneInventaire> nonComptees = ligneInventaireRepository.findLignesNonComptees(inventaireId);
        if (!nonComptees.isEmpty()) {
            throw new IllegalStateException(nonComptees.size() + " lignes n'ont pas été comptées");
        }

        inventaire.setStatut(StatutInventaire.EN_VALIDATION);
        return inventaireRepository.save(inventaire);
    }

    /**
     * Clôture l'inventaire après application des ajustements
     */
    public Inventaire cloturer(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        if (inventaire.getStatut() != StatutInventaire.EN_VALIDATION) {
            throw new IllegalStateException("L'inventaire doit être EN_VALIDATION pour être clôturé");
        }

        inventaire.setStatut(StatutInventaire.CLOTURE);
        inventaire.setDateFin(LocalDate.now());
        inventaire.setBloqueMouvements(false); // Débloquer les mouvements
        inventaire = inventaireRepository.save(inventaire);
        
        log.info("Inventaire {} clôturé", inventaire.getNumero());
        
        return inventaire;
    }

    /**
     * Annule un inventaire
     */
    public Inventaire annuler(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + inventaireId));

        if (inventaire.getStatut() == StatutInventaire.CLOTURE) {
            throw new IllegalStateException("Impossible d'annuler un inventaire clôturé");
        }

        inventaire.setStatut(StatutInventaire.ANNULE);
        inventaire.setBloqueMouvements(false);
        inventaire.setDateFin(LocalDate.now());
        
        log.info("Inventaire {} annulé", inventaire.getNumero());
        
        return inventaireRepository.save(inventaire);
    }

    // ===== Méthodes de consultation =====

    public Optional<Inventaire> findById(Long id) {
        return inventaireRepository.findById(id);
    }

    public Optional<Inventaire> findByNumero(String numero) {
        return inventaireRepository.findByNumero(numero);
    }

    public List<Inventaire> findAll() {
        return inventaireRepository.findAll();
    }

    public List<Inventaire> findByStatut(StatutInventaire statut) {
        return inventaireRepository.findByStatut(statut);
    }

    public List<Inventaire> findByDepot(Long depotId) {
        return inventaireRepository.findByDepotId(depotId);
    }

    public List<Inventaire> findByPeriode(LocalDate debut, LocalDate fin) {
        return inventaireRepository.findByPeriode(debut, fin);
    }

    /**
     * Vérifie si les mouvements sont bloqués pour un dépôt
     */
    public boolean isMouvementsBloquesForDepot(Long depotId) {
        return inventaireRepository.existsInventaireEnCours(depotId);
    }
}
