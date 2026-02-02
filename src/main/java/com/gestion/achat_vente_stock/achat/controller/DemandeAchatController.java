package com.gestion.achat_vente_stock.achat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.achat_vente_stock.achat.model.DemandeAchat;
import com.gestion.achat_vente_stock.achat.model.LigneDA;
import com.gestion.achat_vente_stock.achat.model.ValidationDA;
import com.gestion.achat_vente_stock.achat.service.DemandeAchatService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.ServiceRepository;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.dto.ArticleDTO;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO.YML Lignes 7-9: Achats > Demande Achat - Contrôleur web
 */
@Controller
@RequestMapping("/achats/demandes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE-DEMANDEUR', 'ROLE-ACHETEUR', 'ROLE-RESP-ACHATS', 'ROLE-ADMIN')")
public class DemandeAchatController {

    private final DemandeAchatService demandeAchatService;
    private final ArticleRepository articleRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ServiceRepository serviceRepository;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    /**
     * TODO.YML Ligne 7: Liste des demandes d'achat
     */
    @GetMapping
    public String lister(Model model) {
        model.addAttribute("demandes", demandeAchatService.listerTous());
        return "achats/demandes/liste";
    }

    /**
     * TODO.YML Ligne 7: Formulaire création DA
     * CORRECTION: Utilisation de DTO pour éviter les problèmes de sérialisation
     * Thymeleaf
     */
    @GetMapping("/nouveau")
    public String nouveauFormulaire(Model model) {
        try {
            // Demande vide
            model.addAttribute("demande", new DemandeAchat());

            // Charger les articles et les convertir en DTO
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

            // Sérialiser en JSON pour JavaScript
            String articlesJson = objectMapper.writeValueAsString(articlesDTO);
            model.addAttribute("articlesJson", articlesJson);

            // Autres données
            model.addAttribute("fournisseurs", fournisseurRepository.findAll());
            model.addAttribute("services", serviceRepository.findAll());

            System.out.println("✅ Formulaire DA chargé - " + articlesDTO.size() + " articles");

            return "achats/demandes/formulaire";

        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur sérialisation JSON: " + e.getMessage());
            throw new RuntimeException("Erreur lors du chargement du formulaire", e);
        }
    }

    /**
     * TODO.YML Ligne 7: Créer demande d'achat
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE-DEMANDEUR', 'ROLE-ACHETEUR', 'ROLE-ADMIN')")
    public String creer(@ModelAttribute DemandeAchat demande,
            @RequestParam(required = false) List<Long> articleIds,
            @RequestParam(required = false) List<java.math.BigDecimal> quantites,
            @RequestParam(required = false) List<java.math.BigDecimal> prix,
            RedirectAttributes redirectAttributes) {
        Utilisateur demandeur = sessionService.getUtilisateurConnecte();

        // Créer les lignes à partir des paramètres
        List<LigneDA> lignes = new ArrayList<>();
        if (articleIds != null && !articleIds.isEmpty()) {
            for (int i = 0; i < articleIds.size(); i++) {
                if (articleIds.get(i) != null) {
                    LigneDA ligne = new LigneDA();
                    ligne.setArticle(articleRepository.findById(articleIds.get(i))
                            .orElseThrow(() -> new RuntimeException("Article non trouvé")));
                    ligne.setQuantite(
                            quantites != null && i < quantites.size() ? quantites.get(i) : java.math.BigDecimal.ONE);
                    ligne.setPrixEstimeHt(prix != null && i < prix.size() ? prix.get(i) : java.math.BigDecimal.ZERO);
                    lignes.add(ligne);
                }
            }
        }

        DemandeAchat created = demandeAchatService.creerDemandeAchat(demande, lignes, demandeur);
        redirectAttributes.addFlashAttribute("success", "Demande d'achat créée avec succès");
        return "redirect:/achats/demandes/" + created.getId();
    }

    /**
     * TODO.YML Ligne 7-9: Détail d'une DA avec validations
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        DemandeAchat demande = demandeAchatService.trouverParId(id);
        List<LigneDA> lignes = demandeAchatService.getLignesDA(id);
        List<ValidationDA> validations = demandeAchatService.getValidations(id);

        model.addAttribute("demande", demande);
        model.addAttribute("lignes", lignes);
        model.addAttribute("validations", validations);
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());

        return "achats/demandes/detail";
    }

    /**
     * TODO.YML Ligne 8: Soumettre DA pour approbation
     */
    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE-DEMANDEUR', 'ROLE-ACHETEUR', 'ROLE-ADMIN')")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur demandeur = sessionService.getUtilisateurConnecte();

        demandeAchatService.soumettrePourApprobation(id, demandeur);
        redirectAttributes.addFlashAttribute("success", "Demande soumise pour approbation");
        return "redirect:/achats/demandes/" + id;
    }

    /**
     * TODO.YML Ligne 8-9: Valider/Approuver une DA
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE-RESP-ACHATS', 'ROLE-DAF', 'ROLE-ADMIN')")
    public String valider(@PathVariable Long id,
            @RequestParam Long valideurId,
            @RequestParam Integer niveau,
            @RequestParam String decision,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        Utilisateur valideur = utilisateurRepository.findById(valideurId)
                .orElseThrow(() -> new RuntimeException("Valideur non trouvé"));

        try {
            demandeAchatService.validerDemandeAchat(id, valideur, niveau, decision, commentaire);
            redirectAttributes.addFlashAttribute("success", "Validation enregistrée");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/achats/demandes/" + id;
    }

    /**
     * Formulaire édition
     */
    @GetMapping("/{id}/editer")
    public String editerFormulaire(@PathVariable Long id, Model model) {
        model.addAttribute("demande", demandeAchatService.trouverParId(id));
        model.addAttribute("services", serviceRepository.findAll());
        return "achats/demandes/formulaire";
    }

    /**
     * Modifier
     */
    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id,
            @ModelAttribute DemandeAchat demande,
            RedirectAttributes redirectAttributes) {
        demandeAchatService.modifierDemandeAchat(id, demande);
        redirectAttributes.addFlashAttribute("success", "Demande d'achat modifiée");
        return "redirect:/achats/demandes/" + id;
    }

}