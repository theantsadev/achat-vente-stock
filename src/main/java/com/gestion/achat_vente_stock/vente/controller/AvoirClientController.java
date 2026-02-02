package com.gestion.achat_vente_stock.vente.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.vente.model.AvoirClient;
import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.service.AvoirClientService;
import com.gestion.achat_vente_stock.vente.service.FactureClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO.YML Ligne 30: Ventes > Avoirs
 * - Créer avoir (retour, erreur prix, casse, geste commercial)
 * - Double validation obligatoire au-dessus d'un seuil
 * 
 * Contrôleur web pour la gestion des avoirs clients
 */
@Controller
@RequestMapping("/ventes/avoirs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class AvoirClientController {

    private final AvoirClientService avoirClientService;
    private final FactureClientService factureClientService;
    private final SessionService sessionService;

    // ==================== LISTE ====================

    /**
     * Liste de tous les avoirs
     */
    @GetMapping
    public String lister(Model model) {
        List<AvoirClient> avoirs = avoirClientService.listerTous();
        model.addAttribute("avoirs", avoirs);
        return "ventes/avoirs/liste";
    }

    /**
     * Avoirs en attente de validation
     */
    @GetMapping("/en-attente")
    public String listerEnAttente(Model model) {
        List<AvoirClient> avoirs = avoirClientService.listerEnAttenteValidation();
        model.addAttribute("avoirs", avoirs);
        model.addAttribute("filtreActif", "en-attente");
        return "ventes/avoirs/liste";
    }

    /**
     * Avoirs d'un client
     */
    @GetMapping("/client/{clientId}")
    public String listerParClient(@PathVariable Long clientId, Model model) {
        List<AvoirClient> avoirs = avoirClientService.listerParClient(clientId);
        model.addAttribute("avoirs", avoirs);
        return "ventes/avoirs/liste";
    }

    /**
     * Avoirs d'une facture
     */
    @GetMapping("/facture/{factureId}")
    public String listerParFacture(@PathVariable Long factureId, Model model) {
        List<AvoirClient> avoirs = avoirClientService.listerParFacture(factureId);
        FactureClient facture = factureClientService.obtenirParId(factureId);

        model.addAttribute("avoirs", avoirs);
        model.addAttribute("facture", facture);
        return "ventes/avoirs/liste-facture";
    }

    // ==================== CRÉATION ====================

    /**
     * TODO.YML Ligne 30: Formulaire de création d'avoir
     */
    @GetMapping("/nouveau/{factureId}")
    public String nouveauFormulaire(@PathVariable Long factureId, Model model) {
        FactureClient facture = factureClientService.obtenirParId(factureId);

        model.addAttribute("facture", facture);
        model.addAttribute("avoir", new AvoirClient());
        model.addAttribute("motifs", List.of("RETOUR", "ERREUR_PRIX", "CASSE", "COMMERCIAL"));
        return "ventes/avoirs/formulaire";
    }

    /**
     * TODO.YML Ligne 30: Créer un avoir
     */
    @PostMapping("/facture/{factureId}")
    @PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-ADMIN')")
    public String creer(@PathVariable Long factureId,
            @RequestParam String motif,
            @RequestParam BigDecimal montantHt,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur createur = sessionService.getUtilisateurConnecte();

            AvoirClient avoir = avoirClientService.creerAvoir(
                    factureId, motif, montantHt, commentaire, createur);

            redirectAttributes.addFlashAttribute("success",
                    "Avoir " + avoir.getNumero() + " créé avec succès");

            // Message si validation requise
            if (avoir.getStatut().equals("EN_ATTENTE_VALIDATION")) {
                redirectAttributes.addFlashAttribute("info",
                        "Cet avoir nécessite une validation par un responsable");
            }

            return "redirect:/ventes/avoirs/" + avoir.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/avoirs/nouveau/" + factureId;
        }
    }

    // ==================== DÉTAIL ====================

    /**
     * Détail d'un avoir
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AvoirClient avoir = avoirClientService.obtenirParId(id);
        model.addAttribute("avoir", avoir);
        return "ventes/avoirs/detail";
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 30: Valider un avoir (double validation au-dessus du seuil)
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur valideur = sessionService.getUtilisateurConnecte();
            avoirClientService.validerAvoir(id, valideur);
            redirectAttributes.addFlashAttribute("success", "Avoir validé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur de validation: " + e.getMessage());
        }
        return "redirect:/ventes/avoirs/" + id;
    }

    /**
     * Refuser un avoir
     */
    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Long id,
            @RequestParam(required = false) String motifRefus,
            RedirectAttributes redirectAttributes) {
        try {
            avoirClientService.refuserAvoir(id, motifRefus);
            redirectAttributes.addFlashAttribute("warning", "Avoir refusé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/avoirs/" + id;
    }

    /**
     * Appliquer un avoir (crédit sur compte client)
     */
    @PostMapping("/{id}/appliquer")
    public String appliquer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            avoirClientService.appliquerAvoir(id);
            redirectAttributes.addFlashAttribute("success",
                    "Avoir appliqué - le solde client a été crédité");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/avoirs/" + id;
    }

    /**
     * Annuler un avoir
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            avoirClientService.annulerAvoir(id);
            redirectAttributes.addFlashAttribute("success", "Avoir annulé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/avoirs/" + id;
    }

    // ==================== IMPRESSION ====================

    /**
     * Imprimer/générer PDF de l'avoir
     */
    @GetMapping("/{id}/imprimer")
    public String imprimer(@PathVariable Long id, Model model) {
        AvoirClient avoir = avoirClientService.obtenirParId(id);
        model.addAttribute("avoir", avoir);
        return "ventes/avoirs/imprimer";
    }
}
