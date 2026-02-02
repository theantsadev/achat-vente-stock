package com.gestion.achat_vente_stock.inventaire.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.MotifAjustement;
import com.gestion.achat_vente_stock.inventaire.model.AjustementStock.StatutAjustement;
import com.gestion.achat_vente_stock.inventaire.service.AjustementStockService;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO.YML Lignes 52-54: Inventaire > Ajustements
 * Contrôleur pour la gestion des ajustements de stock
 */
@Controller
@RequestMapping("/inventaires/ajustements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-AUDITEUR', 'ROLE-ADMIN')")
public class AjustementStockController {

    private final AjustementStockService ajustementStockService;
    private final ArticleRepository articleRepository;
    private final DepotRepository depotRepository;
    private final SessionService sessionService;

    /**
     * Liste des ajustements
     */
    @GetMapping
    public String liste(Model model, @RequestParam(required = false) String statut) {
        List<AjustementStock> ajustements;

        if (statut != null && !statut.isEmpty()) {
            ajustements = ajustementStockService.findByStatut(StatutAjustement.valueOf(statut));
        } else {
            ajustements = ajustementStockService.findAll();
        }

        model.addAttribute("ajustements", ajustements);
        model.addAttribute("statuts", StatutAjustement.values());
        model.addAttribute("statut", statut);
        return "inventaires/ajustements/liste";
    }

    /**
     * Ajustements en attente de validation
     */
    @GetMapping("/en-attente")
    public String enAttente(Model model) {
        List<AjustementStock> ajustements = ajustementStockService.findEnAttenteValidation();
        model.addAttribute("ajustements", ajustements);
        model.addAttribute("titre", "Ajustements en attente de validation");
        model.addAttribute("seuilValidation", ajustementStockService.getSeuilValidation());
        return "inventaires/ajustements/liste";
    }

    /**
     * Formulaire de création manuel
     */
    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("motifs", MotifAjustement.values());
        return "inventaires/ajustements/formulaire";
    }

    /**
     * Création d'un ajustement manuel
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creer(@RequestParam Long articleId,
            @RequestParam Long depotId,
            @RequestParam BigDecimal quantiteApres,
            @RequestParam String motif,
            @RequestParam String justification,
            @RequestParam(required = false) String lotNumero,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur demandeur = sessionService.getUtilisateurConnecte();

            AjustementStock ajustement = ajustementStockService.creerManuel(
                    articleId, depotId, quantiteApres,
                    MotifAjustement.valueOf(motif), justification,
                    lotNumero, demandeur);

            redirectAttributes.addFlashAttribute("success",
                    "Ajustement " + ajustement.getNumero() + " créé - En attente de validation");
            return "redirect:/inventaires/ajustements/" + ajustement.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventaires/ajustements/nouveau";
        }
    }

    /**
     * Détail d'un ajustement
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AjustementStock ajustement = ajustementStockService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ajustement non trouvé: " + id));

        model.addAttribute("ajustement", ajustement);
        return "inventaires/ajustements/detail";
    }

    /**
     * Valider un ajustement
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-AUDITEUR', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur valideur = sessionService.getUtilisateurConnecte();
            ajustementStockService.valider(id, valideur);
            redirectAttributes.addFlashAttribute("success", "Ajustement validé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/ajustements/" + id;
    }

    /**
     * Refuser un ajustement
     */
    @PostMapping("/{id}/refuser")
    @PreAuthorize("hasAnyAuthority('ROLE-AUDITEUR', 'ROLE-ADMIN')")
    public String refuser(@PathVariable Long id,
            @RequestParam String motifRefus,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur valideur = sessionService.getUtilisateurConnecte();
            ajustementStockService.refuser(id, valideur, motifRefus);
            redirectAttributes.addFlashAttribute("success", "Ajustement refusé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/ajustements/" + id;
    }

    /**
     * Appliquer un ajustement au stock
     */
    @PostMapping("/{id}/appliquer")
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String appliquer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            ajustementStockService.appliquer(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Ajustement appliqué au stock");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/ajustements/" + id;
    }
}
