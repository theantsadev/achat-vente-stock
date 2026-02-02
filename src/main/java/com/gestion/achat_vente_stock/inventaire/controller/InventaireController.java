package com.gestion.achat_vente_stock.inventaire.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.StatutInventaire;
import com.gestion.achat_vente_stock.inventaire.model.Inventaire.TypeInventaire;
import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import com.gestion.achat_vente_stock.inventaire.service.AjustementStockService;
import com.gestion.achat_vente_stock.inventaire.service.InventaireService;
import com.gestion.achat_vente_stock.inventaire.service.LigneInventaireService;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * TODO.YML Lignes 42-54: Module Inventaire
 * Contrôleur pour la gestion des inventaires
 */
@Controller
@RequestMapping("/inventaires")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-AUDITEUR', 'ROLE-ADMIN')")
public class InventaireController {

    private final InventaireService inventaireService;
    private final LigneInventaireService ligneInventaireService;
    private final AjustementStockService ajustementStockService;
    private final DepotRepository depotRepository;
    private final SessionService sessionService;

    /**
     * Liste des inventaires
     */
    @GetMapping
    public String liste(Model model, @RequestParam(required = false) String statut) {
        List<Inventaire> inventaires;
        
        if (statut != null && !statut.isEmpty()) {
            inventaires = inventaireService.findByStatut(StatutInventaire.valueOf(statut));
        } else {
            inventaires = inventaireService.findAll();
        }

        model.addAttribute("inventaires", inventaires);
        model.addAttribute("statuts", StatutInventaire.values());
        model.addAttribute("statut", statut);
        return "inventaires/liste";
    }

    /**
     * Formulaire de création
     */
    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("depots", depotRepository.findAll());
        model.addAttribute("types", TypeInventaire.values());
        return "inventaires/formulaire";
    }

    /**
     * Création d'un inventaire
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creer(@RequestParam Long depotId,
                       @RequestParam String type,
                       RedirectAttributes redirectAttributes) {
        try {
            Utilisateur responsable = sessionService.getUtilisateurConnecte();
            
            Inventaire inventaire = inventaireService.creer(depotId, TypeInventaire.valueOf(type), responsable);
            redirectAttributes.addFlashAttribute("success", "Inventaire " + inventaire.getNumero() + " créé avec succès");
            return "redirect:/inventaires/" + inventaire.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventaires/nouveau";
        }
    }

    /**
     * Détail d'un inventaire
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Inventaire inventaire = inventaireService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventaire non trouvé: " + id));

        List<LigneInventaire> lignes = ligneInventaireService.findByInventaire(id);
        Map<String, Long> statsComptage = ligneInventaireService.getStatistiquesComptage(id);
        BigDecimal sommeEcarts = ligneInventaireService.getSommeEcartsValorises(id);

        model.addAttribute("inventaire", inventaire);
        model.addAttribute("lignes", lignes);
        model.addAttribute("statsComptage", statsComptage);
        model.addAttribute("sommeEcarts", sommeEcarts);
        model.addAttribute("ajustements", ajustementStockService.findByInventaire(id));

        return "inventaires/detail";
    }

    /**
     * Ouvrir un inventaire (générer le snapshot)
     */
    @PostMapping("/{id}/ouvrir")
    public String ouvrir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventaireService.ouvrir(id);
            redirectAttributes.addFlashAttribute("success", "Inventaire ouvert - Stock théorique capturé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }

    /**
     * Démarrer le comptage
     */
    @PostMapping("/{id}/demarrer-comptage")
    public String demarrerComptage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventaireService.demarrerComptage(id);
            redirectAttributes.addFlashAttribute("success", "Comptage démarré");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }

    /**
     * Terminer le comptage
     */
    @PostMapping("/{id}/terminer-comptage")
    public String terminerComptage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventaireService.terminerComptage(id);
            redirectAttributes.addFlashAttribute("success", "Comptage terminé - En attente de validation");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }

    /**
     * Clôturer l'inventaire
     */
    @PostMapping("/{id}/cloturer")
    public String cloturer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventaireService.cloturer(id);
            redirectAttributes.addFlashAttribute("success", "Inventaire clôturé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }

    /**
     * Annuler l'inventaire
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventaireService.annuler(id);
            redirectAttributes.addFlashAttribute("success", "Inventaire annulé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }

    /**
     * Générer les ajustements à partir des écarts
     */
    @PostMapping("/{id}/generer-ajustements")
    @PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String genererAjustements(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            var ajustements = ajustementStockService.genererAjustementsInventaire(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", ajustements.size() + " ajustement(s) créé(s)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + id;
    }
}
