package com.gestion.achat_vente_stock.stock.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;
import com.gestion.achat_vente_stock.stock.model.Lot;
import com.gestion.achat_vente_stock.stock.service.LotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 37-38: Stock > Lots/Séries
 * Contrôleur pour la gestion des lots
 */
@Controller
@RequestMapping("/stocks/lots")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-MAGASINIER-REC', 'ROLE-ADMIN')")
public class LotController {

    private final LotService lotService;
    private final ArticleRepository articleRepository;
    private final FournisseurRepository fournisseurRepository;
    private final SessionService sessionService;

    /**
     * Liste des lots
     */
    @GetMapping
    public String liste(Model model,
                        @RequestParam(required = false) Long articleId,
                        @RequestParam(required = false) String statut) {
        
        List<Lot> lots;
        
        if (articleId != null) {
            lots = lotService.listerParArticle(articleId);
        } else if (statut != null && !statut.isEmpty()) {
            lots = lotService.listerParStatut(statut);
        } else {
            lots = lotService.listerTous();
        }
        
        model.addAttribute("lots", lots);
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("articleId", articleId);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", List.of("ACTIF", "BLOQUE", "EXPIRE", "NON_CONFORME"));
        
        return "stocks/lots/liste";
    }

    /**
     * Détail d'un lot
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Lot lot = lotService.trouverParId(id);
        model.addAttribute("lot", lot);
        return "stocks/lots/detail";
    }

    /**
     * Formulaire de création d'un lot
     */
    @GetMapping("/nouveau")
    public String formulaire(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("fournisseurs", fournisseurRepository.findAll());
        return "stocks/lots/formulaire";
    }

    /**
     * Créer un lot
     */
    @PostMapping
    public String creer(@RequestParam Long articleId,
                        @RequestParam(required = false) Long fournisseurId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFabrication,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dluo,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dlc,
                        RedirectAttributes redirectAttributes) {
        try {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));
            Fournisseur fournisseur = null;
            if (fournisseurId != null) {
                fournisseur = fournisseurRepository.findById(fournisseurId).orElse(null);
            }

            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            Lot lot = lotService.creerLot(article, fournisseur, dateFabrication, dluo, dlc, utilisateur);

            redirectAttributes.addFlashAttribute("success", "Lot créé: " + lot.getNumero());
            return "redirect:/stocks/lots/" + lot.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/stocks/lots/nouveau";
        }
    }

    /**
     * Bloquer un lot
     */
    @PostMapping("/{id}/bloquer")
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String bloquer(@PathVariable Long id,
                          @RequestParam String motifBlocage,
                          RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            lotService.bloquerLot(id, motifBlocage, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Lot bloqué avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/lots/" + id;
    }

    /**
     * Débloquer un lot
     */
    @PostMapping("/{id}/debloquer")
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String debloquer(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            lotService.debloquerLot(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Lot débloqué avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/lots/" + id;
    }

    /**
     * Lots bientôt expirés (alertes)
     */
    @GetMapping("/alertes-expiration")
    public String alertesExpiration(Model model,
                                    @RequestParam(defaultValue = "30") int joursAvant) {
        List<Lot> lotsBientotExpires = lotService.getLotsBientotExpires(joursAvant);
        model.addAttribute("lots", lotsBientotExpires);
        model.addAttribute("joursAvant", joursAvant);
        return "stocks/lots/alertes-expiration";
    }
}
