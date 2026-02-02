package com.gestion.achat_vente_stock.inventaire.controller;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.inventaire.model.LigneInventaire;
import com.gestion.achat_vente_stock.inventaire.service.LigneInventaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * TODO.YML Lignes 48-51: Inventaire > Saisie et Écarts
 * Contrôleur pour la saisie des comptages
 */
@Controller
@RequestMapping("/inventaires/{inventaireId}/lignes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-CHEF-MAGASIN', 'ROLE-MAGASINIER-REC', 'ROLE-MAGASINIER-SORT', 'ROLE-ADMIN')")
public class LigneInventaireController {

    private final LigneInventaireService ligneInventaireService;
    private final SessionService sessionService;

    /**
     * Page de comptage d'une ligne
     */
    @GetMapping("/{ligneId}")
    public String detail(@PathVariable Long inventaireId, 
                        @PathVariable Long ligneId, 
                        Model model) {
        LigneInventaire ligne = ligneInventaireService.findById(ligneId)
                .orElseThrow(() -> new IllegalArgumentException("Ligne non trouvée: " + ligneId));

        model.addAttribute("ligne", ligne);
        model.addAttribute("inventaireId", inventaireId);
        return "inventaires/ligne-comptage";
    }

    /**
     * Saisie du premier comptage
     */
    @PostMapping("/{ligneId}/comptage1")
    public String saisirComptage1(@PathVariable Long inventaireId,
                                  @PathVariable Long ligneId,
                                  @RequestParam BigDecimal quantite,
                                  RedirectAttributes redirectAttributes) {
        try {
            Utilisateur compteur = sessionService.getUtilisateurConnecte();
            
            ligneInventaireService.saisirComptage1(ligneId, quantite, compteur);
            redirectAttributes.addFlashAttribute("success", "Comptage 1 enregistré");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + inventaireId;
    }

    /**
     * Saisie du second comptage (double comptage)
     */
    @PostMapping("/{ligneId}/comptage2")
    public String saisirComptage2(@PathVariable Long inventaireId,
                                  @PathVariable Long ligneId,
                                  @RequestParam BigDecimal quantite,
                                  RedirectAttributes redirectAttributes) {
        try {
            Utilisateur compteur = sessionService.getUtilisateurConnecte();
            
            ligneInventaireService.saisirComptage2(ligneId, quantite, compteur);
            redirectAttributes.addFlashAttribute("success", "Comptage 2 enregistré");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + inventaireId;
    }

    /**
     * Validation manuelle de la quantité retenue
     */
    @PostMapping("/{ligneId}/valider-quantite")
    public String validerQuantite(@PathVariable Long inventaireId,
                                  @PathVariable Long ligneId,
                                  @RequestParam BigDecimal quantiteRetenue,
                                  RedirectAttributes redirectAttributes) {
        try {
            ligneInventaireService.validerQuantiteRetenue(ligneId, quantiteRetenue);
            redirectAttributes.addFlashAttribute("success", "Quantité retenue validée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventaires/" + inventaireId;
    }
}
