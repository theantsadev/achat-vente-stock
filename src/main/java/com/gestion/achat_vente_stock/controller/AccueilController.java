package com.gestion.achat_vente_stock.controller;

import com.gestion.achat_vente_stock.vente.service.StatistiquesVenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur principal pour la page d'accueil
 */
@Controller
@RequiredArgsConstructor
public class AccueilController {
    
    private final StatistiquesVenteService statistiquesVenteService;
    
    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("titre", "Système de Gestion Achat-Vente-Stock");
        
        // Statistiques CA
        try {
            model.addAttribute("caTotal", statistiquesVenteService.getChiffreAffairesTotal());
            model.addAttribute("caHtTotal", statistiquesVenteService.getChiffreAffairesHtTotal());
            model.addAttribute("caMoisCourant", statistiquesVenteService.getChiffreAffairesMoisCourant());
            model.addAttribute("caAnneeCourante", statistiquesVenteService.getChiffreAffairesAnneeCourante());
            model.addAttribute("nbFactures", statistiquesVenteService.getNombreFacturesValidees());
            model.addAttribute("montantImpayes", statistiquesVenteService.getMontantImpayes());
        } catch (Exception e) {
            // Si erreur (ex: base vide), mettre des valeurs par défaut
            model.addAttribute("caTotal", java.math.BigDecimal.ZERO);
            model.addAttribute("caHtTotal", java.math.BigDecimal.ZERO);
            model.addAttribute("caMoisCourant", java.math.BigDecimal.ZERO);
            model.addAttribute("caAnneeCourante", java.math.BigDecimal.ZERO);
            model.addAttribute("nbFactures", 0L);
            model.addAttribute("montantImpayes", java.math.BigDecimal.ZERO);
        }
        
        return "accueil";
    }
}
