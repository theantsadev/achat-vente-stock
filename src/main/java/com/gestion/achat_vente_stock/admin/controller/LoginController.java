package com.gestion.achat_vente_stock.admin.controller;

import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour l'authentification
 * Gère les pages de login/logout
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final SessionService sessionService;

    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        
        // Si déjà connecté, rediriger vers l'accueil
        if (sessionService.estAuthentifie()) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Login ou mot de passe incorrect");
            log.warn("Tentative de connexion échouée");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Vous avez été déconnecté avec succès");
            log.info("Utilisateur déconnecté");
        }

        if (expired != null) {
            model.addAttribute("expiredMessage", "Votre session a expiré. Veuillez vous reconnecter.");
            log.info("Session expirée");
        }

        return "login";
    }

    /**
     * Page d'accès refusé (403)
     */
    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "Accès refusé. Vous n'avez pas les droits nécessaires pour accéder à cette page.");
        return "error/403";
    }
}
