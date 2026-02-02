package com.gestion.achat_vente_stock.vente.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.dto.ArticleDTO;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.ClientRepository;
import com.gestion.achat_vente_stock.vente.model.Devis;
import com.gestion.achat_vente_stock.vente.model.LigneDevis;
import com.gestion.achat_vente_stock.vente.service.DevisService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO.YML Lignes 20-21: Ventes > Devis
 * - Ligne 20: Créer devis/pro-forma client (articles, prix, remises)
 * - Ligne 21: Valider remises > plafond par responsable ventes
 * 
 * Contrôleur web pour la gestion des devis clients
 * 
 * Rôles autorisés: ROLE-COMMERCIAL, ROLE-MANAGER-VENTES, ROLE-ADMIN
 */
@Controller
@RequestMapping("/ventes/devis")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-COMMERCIAL', 'ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
public class DevisController {

    private final DevisService devisService;
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    // ==================== LISTE ====================

    /**
     * TODO.YML Ligne 20: Liste des devis
     */
    @GetMapping
    public String lister(Model model) {
        List<Devis> devis = devisService.listerTous();
        model.addAttribute("devisList", devis);
        return "ventes/devis/liste";
    }

    // ==================== CRÉATION ====================

    /**
     * TODO.YML Ligne 20: Formulaire de création de devis
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(Model model) {
        try {
            Devis devis = new Devis();
            model.addAttribute("devis", devis);
            model.addAttribute("clients", clientRepository.findAll());
            List<Article> articles = articleRepository.findAll();
            List<ArticleDTO> articlesDTO = articles.stream()
                    .map(a -> new ArticleDTO(
                            a.getId(),
                            a.getCode(),
                            a.getDesignation(),
                            a.getPrixAchatMoyen(),
                            a.getPrixVentePublic()))
                    .collect(Collectors.toList());

            model.addAttribute("articles", articlesDTO);
            String articlesJson = objectMapper.writeValueAsString(articlesDTO);
            model.addAttribute("articlesJson", articlesJson);
            return "ventes/devis/formulaire";
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur sérialisation JSON: " + e.getMessage());
            throw new RuntimeException("Erreur lors du chargement du formulaire", e);
        }
    }

    /**
     * TODO.YML Ligne 20: Enregistrer un nouveau devis
     */
    @PostMapping
    public String enregistrer(@ModelAttribute Devis devis,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer le commercial (utilisateur connecté)
            Utilisateur commercial = sessionService.getUtilisateurConnecte();
            if (commercial == null) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non connecté");
                return "redirect:/ventes/devis";
            }
            devis.setCommercial(commercial);

            Devis saved = devisService.creerDevis(devis, commercial);
         
            redirectAttributes.addFlashAttribute("success", "Devis créé avec succès: " + saved.getNumero());
            return "redirect:/ventes/devis/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/devis/nouveau";
        }
    }

    // ==================== DÉTAIL / ÉDITION ====================

    /**
     * Détail d'un devis
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Devis devis = devisService.obtenirParId(id);
        model.addAttribute("devis", devis);
        return "ventes/devis/detail";
    }

    /**
     * Formulaire d'édition
     */
    @GetMapping("/{id}/editer")
    public String editerFormulaire(@PathVariable Long id, Model model) throws Exception {
        Devis devis = devisService.obtenirParId(id);

        // Convertir les lignes en JSON pour le formulaire
        String lignesJson = convertLignesToJson(devis.getLignes());

        model.addAttribute("devis", devis);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("articles", articleRepository.findAll());
        model.addAttribute("lignesJson", lignesJson);
        return "ventes/devis/formulaire";
    }

    /**
     * Modifier un devis
     */
    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id,
            @ModelAttribute Devis devis,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            devis.setId(id);
            Devis saved = devisService.enregistrer(devis);

            redirectAttributes.addFlashAttribute("success", "Devis modifié avec succès");
            return "redirect:/ventes/devis/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/ventes/devis/" + id + "/editer";
        }
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 21: Soumettre le devis pour validation
     */
    @PostMapping("/{id}/soumettre")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur commercial = sessionService.getUtilisateurConnecte();
            devisService.soumettreDevis(id, commercial);
            redirectAttributes.addFlashAttribute("success", "Devis soumis avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/devis/" + id;
    }

    /**
     * TODO.YML Ligne 21: Valider le devis (responsable ventes)
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-MANAGER-VENTES', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id,
            @RequestParam boolean approuve,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur responsable = sessionService.getUtilisateurConnecte();
            devisService.validerDevis(id, responsable, approuve, commentaire);
            redirectAttributes.addFlashAttribute("success", approuve ? "Devis validé" : "Devis renvoyé pour révision");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/devis/" + id;
    }

    /**
     * Accepter le devis (par le client)
     */
    @PostMapping("/{id}/accepter")
    public String accepter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            devisService.accepterDevis(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Devis accepté par le client");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/devis/" + id;
    }

    /**
     * Refuser le devis (par le client)
     */
    @PostMapping("/{id}/refuser")
    public String refuser(@PathVariable Long id,
            @RequestParam(required = false) String motif,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = sessionService.getUtilisateurConnecte();
            devisService.refuserDevis(id, utilisateur, motif);
            redirectAttributes.addFlashAttribute("success", "Devis refusé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/ventes/devis/" + id;
    }

    // ==================== API JSON ====================

    /**
     * API REST pour récupérer les lignes d'un devis
     */
    @GetMapping("/api/{id}/lignes")
    @ResponseBody
    public List<Map<String, Object>> getLignesDevis(@PathVariable Long id) throws Exception {
        Devis devis = devisService.obtenirParId(id);
        return convertLignesToList(devis.getLignes());
    }

    // ==================== UTILITAIRES ====================

    /**
     * Convertir les lignes en JSON (évite les problèmes de sérialisation Hibernate)
     */
    private String convertLignesToJson(List<LigneDevis> lignes) throws Exception {
        return objectMapper.writeValueAsString(convertLignesToList(lignes));
    }

    /**
     * Convertir les lignes en liste de Map (évite les problèmes de sérialisation)
     */
    private List<Map<String, Object>> convertLignesToList(List<LigneDevis> lignes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LigneDevis ligne : lignes) {
            Map<String, Object> ligneMap = new HashMap<>();
            ligneMap.put("id", ligne.getId());
            ligneMap.put("quantite", ligne.getQuantite());
            ligneMap.put("prixUnitaireHt", ligne.getPrixUnitaireHt());
            ligneMap.put("remisePourcent", ligne.getRemisePourcent());
            ligneMap.put("montantLigneHt", ligne.getMontantLigneHt());

            if (ligne.getArticle() != null) {
                Map<String, Object> articleMap = new HashMap<>();
                articleMap.put("id", ligne.getArticle().getId());
                articleMap.put("code", ligne.getArticle().getCode());
                articleMap.put("designation", ligne.getArticle().getDesignation());
                articleMap.put("prixVenteMoyen", ligne.getArticle().getPrixVentePublic());
                ligneMap.put("article", articleMap);
            }
            result.add(ligneMap);
        }
        return result;
    }

   
}
