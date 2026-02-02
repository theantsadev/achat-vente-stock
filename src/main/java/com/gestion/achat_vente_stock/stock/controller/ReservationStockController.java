package com.gestion.achat_vente_stock.stock.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.stock.model.ReservationStock;
import com.gestion.achat_vente_stock.stock.service.ReservationStockService;
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
 * TODO.YML Ligne 36: Stock > Réservations
 * Contrôleur pour la gestion des réservations de stock
 */
@Controller
@RequestMapping("/stocks/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMMERCIAL', 'ROLE-MANAGER-VENTES', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
public class ReservationStockController {

    private final ReservationStockService reservationStockService;
    private final ArticleRepository articleRepository;
    private final DepotRepository depotRepository;
    private final SessionService sessionService;

    /**
     * Liste des réservations
     */
    @GetMapping
    public String liste(Model model,
                        @RequestParam(required = false) String statut) {
        
        List<ReservationStock> reservations;
        
        if ("ACTIVE".equals(statut)) {
            reservations = reservationStockService.listerActives();
        } else {
            reservations = reservationStockService.listerTous();
        }
        
        model.addAttribute("reservations", reservations);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", List.of("ACTIVE", "CONSOMMEE", "EXPIREE", "ANNULEE"));
        
        return "stocks/reservations/liste";
    }

    /**
     * Détail d'une réservation
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ReservationStock reservation = reservationStockService.trouverParId(id);
        model.addAttribute("reservation", reservation);
        return "stocks/reservations/detail";
    }

    /**
     * Formulaire de création d'une réservation
     */
    @GetMapping("/nouveau")
    public String formulaire(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        return "stocks/reservations/formulaire";
    }

    /**
     * Créer une réservation
     */
    @PostMapping
    public String creer(@RequestParam Long articleId,
                        @RequestParam Long depotId,
                        @RequestParam BigDecimal quantite,
                        @RequestParam(required = false) Long commandeClientId,
                        RedirectAttributes redirectAttributes) {
        try {
            var article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));
            var depot = depotRepository.findById(depotId)
                    .orElseThrow(() -> new RuntimeException("Dépôt non trouvé"));

            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            ReservationStock reservation = reservationStockService.creerReservation(
                    article, depot, quantite, commandeClientId, utilisateur);

            redirectAttributes.addFlashAttribute("success", "Réservation créée");
            return "redirect:/stocks/reservations/" + reservation.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/stocks/reservations/nouveau";
        }
    }

    /**
     * Consommer une réservation (lors de la livraison)
     */
    @PostMapping("/{id}/consommer")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String consommer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            reservationStockService.consommerReservation(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Réservation consommée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/reservations/" + id;
    }

    /**
     * Annuler une réservation
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            reservationStockService.annulerReservation(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Réservation annulée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/reservations/" + id;
    }

    /**
     * Réservations actives seulement
     */
    @GetMapping("/actives")
    public String actives(Model model) {
        List<ReservationStock> reservations = reservationStockService.listerActives();
        model.addAttribute("reservations", reservations);
        model.addAttribute("titre", "Réservations actives");
        return "stocks/reservations/liste";
    }
}
