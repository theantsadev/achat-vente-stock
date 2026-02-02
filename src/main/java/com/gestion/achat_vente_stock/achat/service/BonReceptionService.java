package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.*;
import com.gestion.achat_vente_stock.achat.repository.BonReceptionRepository;
import com.gestion.achat_vente_stock.achat.repository.LigneBRRepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.service.MouvementStockService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Lignes 14-16: Achats > Réception
 * - Ligne 14: Enregistrer réception (contrôle quantités vs BC)
 * - Ligne 15: Générer bon de réception, scanner code-barres
 * - Ligne 16: Gérer réceptions partielles et reliquats
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BonReceptionService {

    private final BonReceptionRepository bonReceptionRepository;
    private final LigneBRRepository ligneBRRepository;
    private final BonCommandeService bonCommandeService;
    private final ArticleRepository articleRepository;
    private final AuditService auditService;
    private final MouvementStockService mouvementStockService;
    private final DepotRepository depotRepository;

    /**
     * TODO.YML Ligne 14-15: Créer bon de réception depuis BC
     */
    public BonReception creerBonReception(Long bcId, Utilisateur magasinier,
            String numeroBlFournisseur, LocalDate dateBlFournisseur) {
        BonCommande bc = bonCommandeService.trouverParId(bcId);

        if (!"ENVOYEE".equals(bc.getStatut()) && !"APPROUVEE".equals(bc.getStatut())) {
            throw new RuntimeException("Ce BC ne peut pas encore être réceptionné");
        }

        // TODO.YML Ligne 15: Générer bon de réception
        BonReception br = new BonReception();
        br.setNumero(genererNumeroBR());
        br.setBonCommande(bc);
        br.setMagasinier(magasinier);
        br.setNumeroBlFournisseur(numeroBlFournisseur);
        br.setDateBlFournisseur(dateBlFournisseur);
        br.setDateReception(LocalDate.now());
        br.setStatut("EN_COURS"); // Nouveau statut pour réception en cours de saisie

        BonReception brSaved = bonReceptionRepository.save(br);

        // TODO.YML Ligne 14: Pré-charger les lignes du BC pour contrôle
        List<LigneBC> lignesBC = bonCommandeService.getLignesBC(bcId);
        for (LigneBC ligneBC : lignesBC) {
            LigneBR ligneBR = new LigneBR();
            ligneBR.setBonReception(brSaved);
            ligneBR.setLigneBc(ligneBC);
            ligneBR.setArticle(ligneBC.getArticle());
            ligneBR.setQuantiteCommandee(ligneBC.getQuantite());
            ligneBR.setQuantiteRecue(BigDecimal.ZERO);
            ligneBR.setQuantiteConforme(BigDecimal.ZERO);
            ligneBR.setQuantiteNonConforme(BigDecimal.ZERO);
            ligneBRRepository.save(ligneBR);
        }

        // Audit
        auditService.logAction(magasinier, "bon_reception", brSaved.getId(),
                "CREATE", null, "BR créé depuis BC " + bc.getNumero(), null);

        return brSaved;
    }

    /**
     * TODO.YML Ligne 14: Mettre à jour une ligne de réception (contrôle quantités)
     */
    public LigneBR mettreAJourLigneReception(Long ligneBrId, BigDecimal quantiteRecue, 
            BigDecimal quantiteConforme, BigDecimal quantiteNonConforme, 
            String motifNonConformite, Utilisateur magasinier) {
        
        LigneBR ligneBR = ligneBRRepository.findById(ligneBrId)
                .orElseThrow(() -> new RuntimeException("Ligne BR non trouvée: " + ligneBrId));

        // Validation: qté conforme + non conforme = qté reçue
        if (quantiteConforme.add(quantiteNonConforme).compareTo(quantiteRecue) != 0) {
            throw new RuntimeException("Qté conforme + Qté non conforme doit égaler Qté reçue");
        }

        ligneBR.setQuantiteRecue(quantiteRecue);
        ligneBR.setQuantiteConforme(quantiteConforme);
        ligneBR.setQuantiteNonConforme(quantiteNonConforme);
        ligneBR.setMotifNonConformite(motifNonConformite);

        LigneBR saved = ligneBRRepository.save(ligneBR);

        // Mettre à jour le statut de la réception
        mettreAJourStatutReception(ligneBR.getBonReception().getId());

        // Audit
        auditService.logAction(magasinier, "ligne_br", saved.getId(),
                "UPDATE", null, "Qté reçue: " + quantiteRecue, null);

        return saved;
    }

    /**
     * TODO.YML Ligne 15: Rechercher article par code-barres (scan)
     */
    @Transactional(readOnly = true)
    public Optional<Article> rechercherParCodeBarre(String codeBarre) {
        return articleRepository.findByCodeBarre(codeBarre);
    }

    /**
     * TODO.YML Ligne 15: Rechercher article multicritère (code, code-barres, désignation)
     */
    @Transactional(readOnly = true)
    public Optional<Article> rechercherArticleMulticritere(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return Optional.empty();
        }
        String terme = recherche.trim().toLowerCase();
        
        // 1. Recherche exacte par code
        Optional<Article> parCode = articleRepository.findByCode(recherche.trim());
        if (parCode.isPresent()) {
            return parCode;
        }
        
        // 2. Recherche par code-barres
        Optional<Article> parCodeBarre = articleRepository.findByCodeBarre(recherche.trim());
        if (parCodeBarre.isPresent()) {
            return parCodeBarre;
        }
        
        // 3. Recherche par désignation (partielle)
        List<Article> articles = articleRepository.findAll();
        return articles.stream()
                .filter(a -> a.getDesignation() != null && 
                            a.getDesignation().toLowerCase().contains(terme))
                .findFirst();
    }

    /**
     * TODO.YML Ligne 15: Rechercher ligne BR par article - multicritère
     */
    @Transactional(readOnly = true)
    public Optional<LigneBR> trouverLigneBRParArticle(Long brId, String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return Optional.empty();
        }
        
        List<LigneBR> lignes = ligneBRRepository.findByBonReceptionId(brId);
        String terme = recherche.trim().toLowerCase();
        
        return lignes.stream()
                .filter(l -> {
                    Article article = l.getArticle();
                    if (article == null) return false;
                    
                    // Recherche par code exact
                    if (article.getCode() != null && article.getCode().equalsIgnoreCase(recherche.trim())) {
                        return true;
                    }
                    // Recherche par code-barres exact
                    if (article.getCodeBarre() != null && article.getCodeBarre().equals(recherche.trim())) {
                        return true;
                    }
                    // Recherche partielle dans le code
                    if (article.getCode() != null && article.getCode().toLowerCase().contains(terme)) {
                        return true;
                    }
                    // Recherche partielle dans la désignation
                    if (article.getDesignation() != null && article.getDesignation().toLowerCase().contains(terme)) {
                        return true;
                    }
                    return false;
                })
                .findFirst();
    }

    /**
     * TODO.YML Ligne 15: Rechercher toutes les lignes BR correspondant à un critère
     */
    @Transactional(readOnly = true)
    public List<LigneBR> rechercherLignesBR(Long brId, String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return ligneBRRepository.findByBonReceptionId(brId);
        }
        
        List<LigneBR> lignes = ligneBRRepository.findByBonReceptionId(brId);
        String terme = recherche.trim().toLowerCase();
        
        return lignes.stream()
                .filter(l -> {
                    Article article = l.getArticle();
                    if (article == null) return false;
                    
                    // Recherche par code
                    if (article.getCode() != null && article.getCode().toLowerCase().contains(terme)) {
                        return true;
                    }
                    // Recherche par code-barres
                    if (article.getCodeBarre() != null && article.getCodeBarre().toLowerCase().contains(terme)) {
                        return true;
                    }
                    // Recherche par désignation
                    if (article.getDesignation() != null && article.getDesignation().toLowerCase().contains(terme)) {
                        return true;
                    }
                    return false;
                })
                .toList();
    }

    /**
     * TODO.YML Ligne 16: Déterminer si la réception est partielle ou complète
     */
    private void mettreAJourStatutReception(Long brId) {
        BonReception br = trouverParId(brId);
        List<LigneBR> lignes = ligneBRRepository.findByBonReceptionId(brId);

        if (lignes.isEmpty()) {
            return;
        }

        boolean toutSaisi = true;
        boolean toutRecu = true;
        boolean avecEcart = false;

        for (LigneBR ligne : lignes) {
            // Vérifier si toutes les lignes ont été saisies
            if (ligne.getQuantiteRecue().compareTo(BigDecimal.ZERO) == 0) {
                toutSaisi = false;
            }
            // Vérifier si quantité reçue < commandée (partielle)
            if (ligne.getQuantiteRecue().compareTo(ligne.getQuantiteCommandee()) < 0) {
                toutRecu = false;
            }
            // Vérifier s'il y a des non-conformités
            if (ligne.getQuantiteNonConforme().compareTo(BigDecimal.ZERO) > 0) {
                avecEcart = true;
            }
        }

        // Déterminer le statut
        if (!toutSaisi) {
            br.setStatut("EN_COURS");
        } else if (avecEcart) {
            br.setStatut("AVEC_ECART");
        } else if (toutRecu) {
            br.setStatut("COMPLETE");
        } else {
            br.setStatut("PARTIELLE");
        }

        bonReceptionRepository.save(br);
    }

    /**
     * TODO.YML Ligne 16: Finaliser le bon de réception
     */
    public BonReception finaliserReception(Long brId, String observations, Utilisateur magasinier) {
        BonReception br = trouverParId(brId);
        
        // Vérifier que toutes les lignes ont été saisies
        List<LigneBR> lignes = ligneBRRepository.findByBonReceptionId(brId);
        boolean toutSaisi = lignes.stream()
                .allMatch(l -> l.getQuantiteRecue().compareTo(BigDecimal.ZERO) > 0 
                        || l.getQuantiteCommandee().compareTo(BigDecimal.ZERO) == 0);
        
        if (!toutSaisi) {
            throw new RuntimeException("Toutes les lignes doivent être contrôlées avant finalisation");
        }

        br.setObservations(observations);
        
        // Recalculer le statut final
        mettreAJourStatutReception(brId);
        
        BonReception saved = bonReceptionRepository.save(br);

        // *** CRÉATION DES MOUVEMENTS DE STOCK ***
        // Après finalisation, créer les entrées de stock pour les quantités conformes
        creerMouvementsStockReception(br, lignes, magasinier);

        // Audit
        auditService.logAction(magasinier, "bon_reception", brId,
                "FINALIZE", null, "Statut: " + br.getStatut() + " - Mouvements stock créés", null);

        return saved;
    }

    /**
     * Créer les mouvements de stock d'entrée pour une réception finalisée
     */
    private void creerMouvementsStockReception(BonReception br, List<LigneBR> lignes, Utilisateur magasinier) {
        // Récupérer le dépôt par défaut (ou le dépôt du BC si défini)
        Depot depotReception = getDepotReception(br);
        
        for (LigneBR ligne : lignes) {
            // Ne créer un mouvement que si quantité conforme > 0
            if (ligne.getQuantiteConforme() != null && 
                ligne.getQuantiteConforme().compareTo(BigDecimal.ZERO) > 0) {
                
                Article article = ligne.getArticle();
                
                // Récupérer le prix unitaire depuis la ligne BC
                BigDecimal coutUnitaire = BigDecimal.ZERO;
                if (ligne.getLigneBc() != null && ligne.getLigneBc().getPrixUnitaireHt() != null) {
                    coutUnitaire = ligne.getLigneBc().getPrixUnitaireHt();
                }
                
                // Créer le mouvement d'entrée
                mouvementStockService.creerMouvementEntree(
                    article,
                    depotReception,
                    MouvementStockService.ENTREE_RECEPTION,
                    ligne.getQuantiteConforme(),
                    coutUnitaire,
                    null, // emplacement
                    null, // lot numero
                    null, // dluo
                    br.getId(), // documentId
                    "BON_RECEPTION", // typeDocument
                    magasinier
                );
            }
        }
    }

    /**
     * Récupérer le dépôt de réception
     */
    private Depot getDepotReception(BonReception br) {
        // Chercher le dépôt principal (ou créer une logique pour le dépôt du BC)
        return depotRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun dépôt configuré pour la réception"));
    }

    /**
     * TODO.YML Ligne 16: Calculer le reliquat (quantité restante à recevoir)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerReliquat(Long bcId) {
        List<BonReception> receptions = bonReceptionRepository.findByBonCommandeId(bcId);
        List<LigneBC> lignesBC = bonCommandeService.getLignesBC(bcId);
        
        BigDecimal totalCommande = lignesBC.stream()
                .map(LigneBC::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRecu = BigDecimal.ZERO;
        for (BonReception br : receptions) {
            List<LigneBR> lignesBR = ligneBRRepository.findByBonReceptionId(br.getId());
            totalRecu = totalRecu.add(lignesBR.stream()
                    .map(LigneBR::getQuantiteConforme)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        
        return totalCommande.subtract(totalRecu);
    }

    /**
     * TODO.YML Ligne 16: Vérifier si le BC a des réceptions en cours
     */
    @Transactional(readOnly = true)
    public boolean aReceptionEnCours(Long bcId) {
        List<BonReception> receptions = bonReceptionRepository.findByBonCommandeId(bcId);
        return receptions.stream().anyMatch(br -> "EN_COURS".equals(br.getStatut()));
    }

    @Transactional(readOnly = true)
    public BonReception trouverParId(Long id) {
        return bonReceptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de réception non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<BonReception> listerTous() {
        return bonReceptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BonReception> listerParBC(Long bcId) {
        return bonReceptionRepository.findByBonCommandeId(bcId);
    }

    @Transactional(readOnly = true)
    public List<LigneBR> getLignesBR(Long brId) {
        return ligneBRRepository.findByBonReceptionId(brId);
    }

    @Transactional(readOnly = true)
    public LigneBR getLigneBR(Long ligneBrId) {
        return ligneBRRepository.findById(ligneBrId)
                .orElseThrow(() -> new RuntimeException("Ligne BR non trouvée: " + ligneBrId));
    }

    private String genererNumeroBR() {
        int year = Year.now().getValue();
        long count = bonReceptionRepository.count() + 1;
        return String.format("BR-%d-%05d", year, count);
    }
}
