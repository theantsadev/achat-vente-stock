package com.gestion.achat_vente_stock.vente.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.vente.model.Encaissement;
import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.service.EncaissementService;
import com.gestion.achat_vente_stock.vente.service.FactureClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Ligne 29: Ventes > Encaissement
 * - Enregistrement des paiements clients
 * - Suivi du solde restant
 * 
 * Contrôleur web pour la gestion des encaissements
 */
@Controller
@RequestMapping("/ventes/encaissements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class EncaissementController {

    private final EncaissementService encaissementService;
    private final FactureClientService factureClientService;
    private final SessionService sessionService;

    // ==================== LISTE ====================

    /**
     * Liste de tous les encaissements
     */
    @GetMapping
    public String lister(Model model) {
        List<Encaissement> encaissements = encaissementService.listerTous();
        model.addAttribute("encaissements", encaissements);
        return "ventes/encaissements/liste";
    }

    /**
     * Encaissements d'une facture
     */
    @GetMapping("/facture/{factureId}")
    public String listerParFacture(@PathVariable Long factureId, Model model) {
        List<Encaissement> encaissements = encaissementService.listerParFacture(factureId);
        FactureClient facture = factureClientService.obtenirParId(factureId);
        BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(factureId);
        
        model.addAttribute("encaissements", encaissements);
        model.addAttribute("facture", facture);
        model.addAttribute("soldeRestant", soldeRestant);
        return "ventes/encaissements/liste-facture";
    }

    /**
     * Encaissements par date
     */
    @GetMapping("/date/{date}")
    public String listerParDate(@PathVariable String date, Model model) {
        LocalDate localDate = LocalDate.parse(date);
        List<Encaissement> encaissements = encaissementService.listerParDate(localDate);
        model.addAttribute("encaissements", encaissements);
        model.addAttribute("dateFiltre", localDate);
        return "ventes/encaissements/liste";
    }

    // ==================== CRÉATION ====================

    /**
     * TODO.YML Ligne 29: Formulaire d'encaissement pour une facture
     */
    @GetMapping("/nouveau/{factureId}")
    public String nouveauFormulaire(@PathVariable Long factureId, Model model) {
        FactureClient facture = factureClientService.obtenirParId(factureId);
        BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(factureId);
        
        model.addAttribute("facture", facture);
        model.addAttribute("soldeRestant", soldeRestant);
        model.addAttribute("encaissement", new Encaissement());
        model.addAttribute("modesPaiement", List.of("ESPECES", "CHEQUE", "VIREMENT", "CARTE_BANCAIRE", "EFFET"));
        return "ventes/encaissements/formulaire";
    }

    /**
     * TODO.YML Ligne 29: Enregistrer un encaissement
     */
    @PostMapping("/facture/{factureId}")
    @PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-ADMIN')")
    public String enregistrer(@PathVariable Long factureId,
                             @RequestParam BigDecimal montant,
                             @RequestParam String modePaiement,
                             @RequestParam(required = false) String reference,
                             @RequestParam(required = false) String banque,
                             @RequestParam(required = false) String dateEcheance,
                             @RequestParam(required = false) String commentaire,
                             RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            LocalDate echeance = dateEcheance != null && !dateEcheance.isEmpty() 
                ? LocalDate.parse(dateEcheance) : null;
            
            Encaissement encaissement = encaissementService.enregistrerEncaissement(
                factureId, montant, modePaiement, reference, banque, echeance, utilisateur);
            
            redirectAttributes.addFlashAttribute("success", 
                "Encaissement de " + montant + " € enregistré avec succès");
            
            // Vérifier si la facture est soldée
            BigDecimal soldeRestant = factureClientService.calculerSoldeRestant(factureId);
            if (soldeRestant.compareTo(BigDecimal.ZERO) == 0) {
                redirectAttributes.addFlashAttribute("info", "La facture est maintenant soldée");
            }
            
            return "redirect:/ventes/encaissements/facture/" + factureId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/encaissements/nouveau/" + factureId;
        }
    }

    // ==================== DÉTAIL ====================

    /**
     * Détail d'un encaissement
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Encaissement encaissement = encaissementService.obtenirParId(id);
        model.addAttribute("encaissement", encaissement);
        return "ventes/encaissements/detail";
    }

    // ==================== WORKFLOW ====================

    /**
     * Valider un encaissement
     */
    @GetMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-COMPTABLE-CLI', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            encaissementService.validerEncaissement(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Encaissement validé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        
        Encaissement encaissement = encaissementService.obtenirParId(id);
        return "redirect:/ventes/encaissements/facture/" + encaissement.getFactureClient().getId();
    }

    /**
     * Rejeter un encaissement (chèque impayé, etc.)
     */
    @GetMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Long id,
                         @RequestParam(required = false) String motif,
                         RedirectAttributes redirectAttributes) {
        try {
            encaissementService.rejeterEncaissement(id, motif);
            redirectAttributes.addFlashAttribute("warning", 
                "Encaissement rejeté" + (motif != null ? ": " + motif : ""));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        
        Encaissement encaissement = encaissementService.obtenirParId(id);
        return "redirect:/ventes/encaissements/facture/" + encaissement.getFactureClient().getId();
    }

    // ==================== RAPPORTS ====================

    /**
     * Rapport des encaissements par période
     */
    @GetMapping("/rapport")
    public String rapport(@RequestParam(required = false) String debut,
                         @RequestParam(required = false) String fin,
                         Model model) {
        LocalDate dateDebut = debut != null && !debut.isEmpty() 
            ? LocalDate.parse(debut) : LocalDate.now().minusMonths(1);
        LocalDate dateFin = fin != null && !fin.isEmpty() 
            ? LocalDate.parse(fin) : LocalDate.now();
        
        List<Encaissement> encaissements = encaissementService.listerParPeriode(dateDebut, dateFin);
        BigDecimal totalPeriode = encaissements.stream()
            .filter(e -> "VALIDE".equals(e.getStatut()))
            .map(Encaissement::getMontantEncaisse)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("encaissements", encaissements);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("totalPeriode", totalPeriode);
        return "ventes/encaissements/rapport";
    }
}
