package com.gestion.achat_vente_stock.achat.controller;

import com.gestion.achat_vente_stock.achat.model.BonReception;
import com.gestion.achat_vente_stock.achat.service.BonCommandeService;
import com.gestion.achat_vente_stock.achat.service.BonReceptionService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * TODO.YML Lignes 14-16: Achats > Réception - Contrôleur web
 */
@Controller
@RequestMapping("/achats/receptions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-REC', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
public class BonReceptionController {
    
    private final BonReceptionService bonReceptionService;
    private final BonCommandeService bonCommandeService;
    private final SessionService sessionService;
    
    /**
     * TODO.YML Ligne 14: Liste des réceptions
     */
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("receptions", bonReceptionService.listerTous());
        return "achats/receptions/liste";
    }
    
    /**
     * TODO.YML Ligne 14-15: Formulaire nouvelle réception
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(@RequestParam Long bcId, Model model) {
        model.addAttribute("bonCommande", bonCommandeService.trouverParId(bcId));
        model.addAttribute("reception", new BonReception());
        return "achats/receptions/formulaire";
    }
    
    /**
     * TODO.YML Ligne 14-15: Créer bon de réception
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-REC', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String creer(@RequestParam Long bcId,
                       @RequestParam String numeroBlFournisseur,
                       @RequestParam String dateBlFournisseur,
                       RedirectAttributes redirectAttributes) {
        Utilisateur magasinier = sessionService.getUtilisateurConnecte();
        
        LocalDate dateBl = LocalDate.parse(dateBlFournisseur);
        
        BonReception br = bonReceptionService.creerBonReception(bcId, magasinier, numeroBlFournisseur, dateBl);
        redirectAttributes.addFlashAttribute("success", "Bon de réception créé");
        return "redirect:/achats/receptions/" + br.getId();
    }
    
    /**
     * TODO.YML Ligne 14-16: Détail réception avec contrôle quantités
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BonReception br = bonReceptionService.trouverParId(id);
        model.addAttribute("reception", br);
        model.addAttribute("lignes", bonReceptionService.getLignesBR(id));
        return "achats/receptions/detail";
    }
    
    /**
     * TODO.YML Ligne 16: Finaliser réception
     */
    @PostMapping("/{id}/finaliser")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-REC', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String finaliser(@PathVariable Long id,
                           @RequestParam(required = false) String observations,
                           RedirectAttributes redirectAttributes) {
        Utilisateur magasinier = sessionService.getUtilisateurConnecte();
        
        bonReceptionService.finaliserReception(id, observations, magasinier);
        redirectAttributes.addFlashAttribute("success", "Réception finalisée");
        return "redirect:/achats/receptions/" + id;
    }
}
