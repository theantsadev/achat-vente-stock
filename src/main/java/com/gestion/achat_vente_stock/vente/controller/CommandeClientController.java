package com.gestion.achat_vente_stock.vente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.ClientRepository;
import com.gestion.achat_vente_stock.vente.model.CommandeClient;
import com.gestion.achat_vente_stock.vente.model.LigneCommandeClient;
import com.gestion.achat_vente_stock.vente.service.CommandeClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.YML Lignes 22-23: Ventes > Commande Client
 * - Ligne 22: Transformer devis en commande client
 * - Ligne 23: Réserver stock à la commande (configurable)
 * 
 * Contrôleur web pour la gestion des commandes clients
 * 
 * Rôles autorisés: ROLE-COMMERCIAL, ROLE-MANAGER-VENTES, ROLE-ADMIN
 */
@Controller
@RequestMapping("/ventes/commandes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMMERCIAL', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class CommandeClientController {

    private final CommandeClientService commandeClientService;
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    // ==================== LISTE ====================

    /**
     * Liste des commandes clients
     */
    @GetMapping
    public String lister(Model model) {
        List<CommandeClient> commandes = commandeClientService.listerTous();
        model.addAttribute("commandes", commandes);
        return "ventes/commandes/liste";
    }

    // ==================== CRÉATION ====================

    /**
     * Formulaire de création de commande
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(Model model) {
        CommandeClient commande = new CommandeClient();
        model.addAttribute("commande", commande);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("lignesJson", "[]");
        return "ventes/commandes/formulaire";
    }

    /**
     * TODO.YML Ligne 22: Transformer un devis en commande
     */
    @PostMapping("/depuis-devis/{devisId}")
    public String transformerDevis(@PathVariable Long devisId, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur commercial = sessionService.getUtilisateurConnecte();
            CommandeClient commande = commandeClientService.transformerDevisEnCommande(devisId, commercial);
            redirectAttributes.addFlashAttribute("success", "Commande créée depuis le devis: " + commande.getNumero());
            return "redirect:/ventes/commandes/" + commande.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/devis/" + devisId;
        }
    }

    /**
     * Enregistrer une nouvelle commande
     */
    @PostMapping
    public String enregistrer(@ModelAttribute CommandeClient commande,
                              @RequestParam(required = false) String lignesModifiees,
                              RedirectAttributes redirectAttributes) {
        try {
            Utilisateur commercial = sessionService.getUtilisateurConnecte();
            CommandeClient saved = commandeClientService.enregistrer(commande);
            
            redirectAttributes.addFlashAttribute("success", "Commande créée: " + saved.getNumero());
            return "redirect:/ventes/commandes/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/commandes/nouveau";
        }
    }

    // ==================== DÉTAIL / ÉDITION ====================

    /**
     * Détail d'une commande
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CommandeClient commande = commandeClientService.obtenirParId(id);
        model.addAttribute("commande", commande);
        return "ventes/commandes/detail";
    }

    /**
     * Formulaire d'édition
     */
    @GetMapping("/{id}/editer")
    public String editerFormulaire(@PathVariable Long id, Model model) throws Exception {
        CommandeClient commande = commandeClientService.obtenirParId(id);
        
        String lignesJson = convertLignesToJson(commande.getLignes());
        
        model.addAttribute("commande", commande);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("lignesJson", lignesJson);
        return "ventes/commandes/formulaire";
    }

    /**
     * Modifier une commande
     */
    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id,
                           @ModelAttribute CommandeClient commande,
                           @RequestParam(required = false) String lignesModifiees,
                           RedirectAttributes redirectAttributes) {
        try {
            commande.setId(id);
            CommandeClient saved = commandeClientService.enregistrer(commande);
            
            redirectAttributes.addFlashAttribute("success", "Commande modifiée avec succès");
            return "redirect:/ventes/commandes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/commandes/" + id + "/editer";
        }
    }

    // ==================== WORKFLOW ====================

    /**
     * Confirmer une commande
     */
    @PostMapping("/{id}/confirmer")
    public String confirmer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            commandeClientService.confirmerCommande(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Commande confirmée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/commandes/" + id;
    }

    /**
     * TODO.YML Ligne 23: Réserver le stock
     */
    @PostMapping("/{id}/reserver-stock")
    public String reserverStock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            commandeClientService.reserverStock(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Stock réservé pour la commande");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/commandes/" + id;
    }

    /**
     * Libérer le stock réservé
     */
    @PostMapping("/{id}/liberer-stock")
    public String libererStock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            commandeClientService.libererStock(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Stock libéré");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/commandes/" + id;
    }

    /**
     * Démarrer la préparation
     */
    @PostMapping("/{id}/demarrer-preparation")
    @PreAuthorize("hasAnyAuthority('ROLE-MAGASINIER-SORT', 'ROLE-CHEF-MAGASIN', 'ROLE-ADMIN')")
    public String demarrerPreparation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            commandeClientService.demarrerPreparation(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Préparation démarrée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/commandes/" + id;
    }

    /**
     * Annuler une commande
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id,
                          @RequestParam(required = false) String motif,
                          RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            commandeClientService.annulerCommande(id, utilisateur, motif);
            redirectAttributes.addFlashAttribute("success", "Commande annulée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/commandes/" + id;
    }

    // ==================== API JSON ====================

    /**
     * API REST pour récupérer les lignes d'une commande
     */
    @GetMapping("/api/{id}/lignes")
    @ResponseBody
    public List<Map<String, Object>> getLignesCommande(@PathVariable Long id) {
        CommandeClient commande = commandeClientService.obtenirParId(id);
        return convertLignesToList(commande.getLignes());
    }

    // ==================== UTILITAIRES ====================

    private String convertLignesToJson(List<LigneCommandeClient> lignes) throws Exception {
        return objectMapper.writeValueAsString(convertLignesToList(lignes));
    }

    private List<Map<String, Object>> convertLignesToList(List<LigneCommandeClient> lignes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LigneCommandeClient ligne : lignes) {
            Map<String, Object> ligneMap = new HashMap<>();
            ligneMap.put("id", ligne.getId());
            ligneMap.put("quantite", ligne.getQuantite());
            ligneMap.put("prixUnitaireHt", ligne.getPrixUnitaireHt());
            ligneMap.put("remisePourcent", ligne.getRemisePourcent());
            ligneMap.put("montantLigneHt", ligne.getMontantLigneHt());
            ligneMap.put("quantitePreparee", ligne.getQuantitePreparee());
            ligneMap.put("quantiteLivree", ligne.getQuantiteLivree());
            
            if (ligne.getArticle() != null) {
                Map<String, Object> articleMap = new HashMap<>();
                articleMap.put("id", ligne.getArticle().getId());
                articleMap.put("code", ligne.getArticle().getCode());
                articleMap.put("designation", ligne.getArticle().getDesignation());
                ligneMap.put("article", articleMap);
            }
            result.add(ligneMap);
        }
        return result;
    }
}
