package com.gestion.achat_vente_stock.achat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.achat_vente_stock.achat.model.DemandeAchat;
import com.gestion.achat_vente_stock.achat.model.LigneDA;
import com.gestion.achat_vente_stock.achat.model.LigneProforma;
import com.gestion.achat_vente_stock.achat.model.Proforma;
import com.gestion.achat_vente_stock.achat.service.DemandeAchatService;
import com.gestion.achat_vente_stock.achat.service.ProformaService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.YML Ligne 10: Achats > Pro-forma
 * - Contrôleur web pour gestion des pro-formas
 * - Génération depuis DA approuvée
 * - Validations et transformations
 */
@Controller
@RequestMapping("/achats/pro-formas")
@RequiredArgsConstructor
public class ProformaController {

    private final ProformaService proformaService;
    private final DemandeAchatService demandeAchatService;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper;

    /**
     * Liste des pro-formas
     */
    @GetMapping
    public String lister(Model model) {
        List<Proforma> proformas = proformaService.listerTous();
        model.addAttribute("proformas", proformas);
        return "achats/pro-formas/liste";
    }

    /**
     * Formulaire génération pro-forma depuis DA approuvée
     */
    @GetMapping("/generer/{daId}")
    public String genererFormulaire(@PathVariable Long daId, Model model, RedirectAttributes redirectAttributes) {
        try {
            DemandeAchat demandeAchat = demandeAchatService.obtenirParId(daId);

            // Vérifier que la DA est approuvée
            if (!demandeAchat.getStatut().equals("APPROUVEE")) {
                redirectAttributes.addFlashAttribute("error",
                        "La demande d'achat doit être approuvée pour générer une pro-forma");
                return "redirect:/achats/demandes/" + daId;
            }

            // Vérifier qu'une pro-forma n'existe pas déjà
            var existante = proformaService.obtenirParDA(demandeAchat);
            if (existante.isPresent()) {
                redirectAttributes.addFlashAttribute("error",
                        "Une pro-forma existe déjà pour cette demande d'achat");
                return "redirect:/achats/demandes/" + daId;
            }

            // Générer pro-forma brouillon
            Utilisateur createur = utilisateurRepository.findById(1L).orElse(null);
            Proforma proforma = proformaService.genererDepuisDA(demandeAchat, createur);

            // Convertir les lignes DA en JSON pour le formulaire
            String lignesPFJson = convertLignesToJson(proforma.getLignes());
            System.out.println("✅ Lignes PF pour formulaire généré: " + lignesPFJson);
            model.addAttribute("proforma", proforma);
            model.addAttribute("fournisseurs", fournisseurRepository.findAll());
            model.addAttribute("demandeAchat", demandeAchat);
            model.addAttribute("lignesPFJson", lignesPFJson);

            return "achats/pro-formas/formulaire";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/achats/demandes/" + daId;
        }
    }

    /**
     * Enregistrer pro-forma avec détails
     */
    @PostMapping
    public String enregistrer(@ModelAttribute Proforma proforma,
            RedirectAttributes redirectAttributes) {
        try {
            // Charger l'entité DemandeAchat complète si seulement l'ID est fourni
            if (proforma.getDemandeAchat() != null && proforma.getDemandeAchat().getId() != null) {
                DemandeAchat da = demandeAchatService.obtenirParId(proforma.getDemandeAchat().getId());
                proforma.setDemandeAchat(da);
            }
            Proforma sauvegardee = proformaService.enregistrer(proforma);
            redirectAttributes.addFlashAttribute("success", "Pro-forma créée avec succès");
            return "redirect:/achats/pro-formas/" + sauvegardee.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/achats/pro-formas";
        }
    }

    /**
     * Détail d'une pro-forma
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Proforma proforma = proformaService.obtenirParId(id);
        model.addAttribute("proforma", proforma);
        model.addAttribute("demandeAchat", proforma.getDemandeAchat());
        model.addAttribute("fournisseurs", fournisseurRepository.findAll());
        return "achats/pro-formas/detail";
    }

    /**
     * Formulaire édition pro-forma
     */
    @GetMapping("/{id}/editer")
    public String editerFormulaire(@PathVariable Long id, Model model) throws Exception {
        Proforma proforma = proformaService.obtenirParId(id);

        // Convertir les lignes proforma ou DA en JSON pour le formulaire
        List<?> lignesToConvert = (proforma.getLignes() != null && !proforma.getLignes().isEmpty())
                ? proforma.getLignes()
                : proforma.getDemandeAchat().getLignes();

        System.out.println("✅ Lignes à convertir: " + lignesToConvert.size());
        String lignesPFJson = convertLignesToJson(lignesToConvert);
        System.out.println("✅ JSON lignes: " + lignesPFJson);

        model.addAttribute("proforma", proforma);
        model.addAttribute("fournisseurs", fournisseurRepository.findAll());
        model.addAttribute("lignesPFJson", lignesPFJson);
        return "achats/pro-formas/formulaire";
    }

    /**
     * Modifier pro-forma
     */
    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id,
            @ModelAttribute Proforma proforma,
            @RequestParam(required = false) String lignesModifiees,
            RedirectAttributes redirectAttributes) {
        try {
            proforma.setId(id);
            // Charger l'entité DemandeAchat complète si seulement l'ID est fourni
            if (proforma.getDemandeAchat() != null && proforma.getDemandeAchat().getId() != null) {
                DemandeAchat da = demandeAchatService.obtenirParId(proforma.getDemandeAchat().getId());
                proforma.setDemandeAchat(da);
            }

            // Traiter les lignes modifiées si présentes
            if (lignesModifiees != null && !lignesModifiees.isEmpty()) {
                System.out.println("📦 Lignes modifiées reçues: " + lignesModifiees);
                proformaService.mettreAJourLignes(id, lignesModifiees);
            }

            Proforma modifiee = proformaService.enregistrer(proforma);
            redirectAttributes.addFlashAttribute("success", "Pro-forma modifiée avec succès");
            return "redirect:/achats/pro-formas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/achats/pro-formas/" + id;
        }
    }

    /**
     * Accepter une pro-forma
     */
    @PostMapping("/{id}/accepter")
    public String accepter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur valideur = utilisateurRepository.findById(1L).orElse(null);
            proformaService.accepter(id, valideur);
            redirectAttributes.addFlashAttribute("success", "Pro-forma acceptée");
            return "redirect:/achats/pro-formas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/achats/pro-formas/" + id;
        }
    }

    /**
     * Rejeter une pro-forma
     */
    @PostMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Long id,
            @RequestParam String motif,
            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur valideur = utilisateurRepository.findById(1L).orElse(null);
            proformaService.rejeter(id, valideur, motif);
            redirectAttributes.addFlashAttribute("success", "Pro-forma rejetée");
            return "redirect:/achats/pro-formas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/achats/pro-formas/" + id;
        }
    }

    /**
     * API REST pour récupérer les lignes d'une proforma en JSON
     */
    @GetMapping("/api/{id}/lignes")
    @ResponseBody
    public List<Map<String, Object>> getLignesProforma(@PathVariable Long id) throws Exception {
        Proforma proforma = proformaService.obtenirParId(id);

        List<Map<String, Object>> result = new ArrayList<>();
        List<?> lignesToConvert = (proforma.getLignes() != null && !proforma.getLignes().isEmpty())
                ? proforma.getLignes()
                : proforma.getDemandeAchat().getLignes();

        for (Object ligne : lignesToConvert) {
            Map<String, Object> ligneMap = new HashMap<>();

            if (ligne instanceof LigneDA) {
                LigneDA ligneDA = (LigneDA) ligne;
                ligneMap.put("id", ligneDA.getId());
                ligneMap.put("quantite", ligneDA.getQuantite());
                ligneMap.put("prixEstimeHt", ligneDA.getPrixEstimeHt());

                Map<String, Object> articleMap = new HashMap<>();
                if (ligneDA.getArticle() != null) {
                    articleMap.put("id", ligneDA.getArticle().getId());
                    articleMap.put("code", ligneDA.getArticle().getCode());
                    articleMap.put("designation", ligneDA.getArticle().getDesignation());
                    articleMap.put("prixAchatMoyen", ligneDA.getArticle().getPrixAchatMoyen());
                }
                ligneMap.put("article", articleMap);
            } else if (ligne instanceof LigneProforma) {
                LigneProforma lignePF = (LigneProforma) ligne;
                ligneMap.put("id", lignePF.getId());
                ligneMap.put("quantite", lignePF.getQuantite());
                ligneMap.put("prixUnitaireHt", lignePF.getPrixUnitaireHt());
                ligneMap.put("remisePourcent", lignePF.getRemisePourcent());

                Map<String, Object> articleMap = new HashMap<>();
                if (lignePF.getArticle() != null) {
                    articleMap.put("id", lignePF.getArticle().getId());
                    articleMap.put("code", lignePF.getArticle().getCode());
                    articleMap.put("designation", lignePF.getArticle().getDesignation());
                    articleMap.put("prixAchatMoyen", lignePF.getArticle().getPrixAchatMoyen());
                }
                ligneMap.put("article", articleMap);
            }

            result.add(ligneMap);
        }

        return result;
    }

    /**
     * Convertir les lignes DA ou PF en structure JSON sérialisable
     */
    private String convertLignesToJson(List<?> lignes) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object ligne : lignes) {
            Map<String, Object> ligneMap = new HashMap<>();

            if (ligne instanceof LigneDA) {
                LigneDA ligneDA = (LigneDA) ligne;
                ligneMap.put("id", ligneDA.getId());
                ligneMap.put("quantite", ligneDA.getQuantite());
                ligneMap.put("prixEstimeHt", ligneDA.getPrixEstimeHt());

                Map<String, Object> articleMap = new HashMap<>();
                if (ligneDA.getArticle() != null) {
                    articleMap.put("id", ligneDA.getArticle().getId());
                    articleMap.put("code", ligneDA.getArticle().getCode());
                    articleMap.put("designation", ligneDA.getArticle().getDesignation());
                    articleMap.put("prixAchatMoyen", ligneDA.getArticle().getPrixAchatMoyen());
                }
                ligneMap.put("article", articleMap);
            } else if (ligne instanceof LigneProforma) {
                LigneProforma lignePF = (LigneProforma) ligne;
                ligneMap.put("id", lignePF.getId());
                ligneMap.put("quantite", lignePF.getQuantite());
                ligneMap.put("prixUnitaireHt", lignePF.getPrixUnitaireHt());
                ligneMap.put("remisePourcent", lignePF.getRemisePourcent());

                System.out.println("🔍 LigneProforma: id=" + lignePF.getId() +
                        ", article=" + (lignePF.getArticle() != null ? lignePF.getArticle().getId() : "NULL"));

                Map<String, Object> articleMap = new HashMap<>();
                if (lignePF.getArticle() != null) {
                    articleMap.put("id", lignePF.getArticle().getId());
                    articleMap.put("code", lignePF.getArticle().getCode());
                    articleMap.put("designation", lignePF.getArticle().getDesignation());
                    articleMap.put("prixAchatMoyen", lignePF.getArticle().getPrixAchatMoyen());
                }
                ligneMap.put("article", articleMap);
            }

            result.add(ligneMap);
        }

        return objectMapper.writeValueAsString(result);
    }
}
