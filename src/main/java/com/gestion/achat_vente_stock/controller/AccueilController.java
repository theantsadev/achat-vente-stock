package com.gestion.achat_vente_stock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur principal pour la page d'accueil
 */
@Controller
public class AccueilController {
    
    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("titre", "Système de Gestion Achat-Vente-Stock");
        return "accueil";
    }
}
