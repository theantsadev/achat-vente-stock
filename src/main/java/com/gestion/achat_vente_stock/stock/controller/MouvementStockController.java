package com.gestion.achat_vente_stock.stock.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.model.MouvementStock;
import com.gestion.achat_vente_stock.stock.service.MouvementStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 31-33: Stock > Mouvements
 * Contrôleur pour la gestion des mouvements de stock
 */
@Controller
@RequestMapping("/stocks/mouvements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-MAGASINIER-REC', 'ROLE-MAGASINIER-SORT', 'ROLE-ADMIN')")
public class MouvementStockController {

    private final MouvementStockService mouvementStockService;
    private final ArticleRepository articleRepository;
    private final DepotRepository depotRepository;
    private final SessionService sessionService;

    /**
     * Liste des mouvements de stock
     */
    @GetMapping
    public String liste(Model model,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Long depotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        List<MouvementStock> mouvements;

        if (dateDebut != null && dateFin != null) {
            mouvements = mouvementStockService.listerParPeriode(dateDebut, dateFin);
        } else if (articleId != null) {
            mouvements = mouvementStockService.listerParArticle(articleId);
        } else if (depotId != null) {
            mouvements = mouvementStockService.listerParDepot(depotId);
        } else {
            mouvements = mouvementStockService.listerTous();
        }

        model.addAttribute("mouvements", mouvements);
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("articleId", articleId);
        model.addAttribute("depotId", depotId);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);

        return "stocks/mouvements/liste";
    }

    /**
     * Formulaire de création d'une entrée de stock
     */
    @GetMapping("/entree/nouveau")
    public String formulaireEntree(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("typesMouvement", getTypesEntree());
        return "stocks/mouvements/formulaire-entree";
    }

    /**
     * Créer une entrée de stock
     */
    @PostMapping("/entree")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-REC', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creerEntree(@RequestParam Long articleId,
            @RequestParam Long depotId,
            @RequestParam String typeMouvement,
            @RequestParam BigDecimal quantite,
            @RequestParam BigDecimal coutUnitaire,
            @RequestParam(required = false) String emplacement,
            @RequestParam(required = false) String lotNumero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dluo,
            RedirectAttributes redirectAttributes) {
        try {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));
            Depot depot = depotRepository.findById(depotId)
                    .orElseThrow(() -> new RuntimeException("Dépôt non trouvé"));

            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            MouvementStock mouvement = mouvementStockService.creerMouvementEntree(
                    article, depot, typeMouvement, quantite, coutUnitaire,
                    emplacement, lotNumero, dluo, null, null, utilisateur);

            redirectAttributes.addFlashAttribute("success",
                    "Mouvement d'entrée créé: " + mouvement.getNumero());
            return "redirect:/stocks/mouvements/" + mouvement.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/stocks/mouvements/entree/nouveau";
        }
    }

    /**
     * Formulaire de création d'une sortie de stock
     */
    @GetMapping("/sortie/nouveau")
    public String formulaireSortie(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("typesMouvement", getTypesSortie());
        return "stocks/mouvements/formulaire-sortie";
    }

    /**
     * Créer une sortie de stock
     */
    @PostMapping("/sortie")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creerSortie(@RequestParam Long articleId,
            @RequestParam Long depotId,
            @RequestParam String typeMouvement,
            @RequestParam BigDecimal quantite,
            @RequestParam(required = false) String emplacement,
            @RequestParam(required = false) String lotNumero,
            RedirectAttributes redirectAttributes) {
        try {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));
            Depot depot = depotRepository.findById(depotId)
                    .orElseThrow(() -> new RuntimeException("Dépôt non trouvé"));

            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            MouvementStock mouvement = mouvementStockService.creerMouvementSortie(
                    article, depot, typeMouvement, quantite,
                    emplacement, lotNumero, null, null, utilisateur);

            redirectAttributes.addFlashAttribute("success",
                    "Mouvement de sortie créé: " + mouvement.getNumero());
            return "redirect:/stocks/mouvements/" + mouvement.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/stocks/mouvements/sortie/nouveau";
        }
    }

    /**
     * Détail d'un mouvement - DOIT ÊTRE APRÈS les routes spécifiques
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        MouvementStock mouvement = mouvementStockService.trouverParId(id);
        model.addAttribute("mouvement", mouvement);
        return "stocks/mouvements/detail";
    }

    private List<String> getTypesEntree() {
        return List.of(
                MouvementStockService.ENTREE_RECEPTION,
                MouvementStockService.ENTREE_RETOUR_CLIENT,
                MouvementStockService.ENTREE_AJUSTEMENT,
                MouvementStockService.ENTREE_TRANSFERT);
    }

    private List<String> getTypesSortie() {
        return List.of(
                MouvementStockService.SORTIE_LIVRAISON,
                MouvementStockService.SORTIE_CONSOMMATION,
                MouvementStockService.SORTIE_REBUT,
                MouvementStockService.SORTIE_AJUSTEMENT,
                MouvementStockService.SORTIE_TRANSFERT);
    }
}
