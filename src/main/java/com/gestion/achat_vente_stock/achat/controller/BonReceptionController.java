package com.gestion.achat_vente_stock.achat.controller;

import com.gestion.achat_vente_stock.achat.model.BonCommande;
import com.gestion.achat_vente_stock.achat.model.BonReception;
import com.gestion.achat_vente_stock.achat.model.LigneBR;
import com.gestion.achat_vente_stock.achat.service.BonCommandeService;
import com.gestion.achat_vente_stock.achat.service.BonReceptionService;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.config.security.SessionService;
import com.gestion.achat_vente_stock.referentiel.model.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO.YML Lignes 14-16: Achats > Réception - Contrôleur web
 * - Ligne 14: Enregistrer réception (contrôle quantités vs BC)
 * - Ligne 15: Générer bon de réception, scanner code-barres
 * - Ligne 16: Gérer réceptions partielles et reliquats
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
    public String nouveauFormulaire(@RequestParam Long bcId, Model model, RedirectAttributes redirectAttributes) {
        BonCommande bc = bonCommandeService.trouverParId(bcId);
        
        // Vérifier si une réception est déjà en cours
        if (bonReceptionService.aReceptionEnCours(bcId)) {
            redirectAttributes.addFlashAttribute("warning", "Une réception est déjà en cours pour ce BC");
            List<BonReception> receptions = bonReceptionService.listerParBC(bcId);
            BonReception enCours = receptions.stream()
                    .filter(r -> "EN_COURS".equals(r.getStatut()))
                    .findFirst().orElse(null);
            if (enCours != null) {
                return "redirect:/achats/receptions/" + enCours.getId();
            }
        }
        
        // TODO.YML Ligne 16: Afficher le reliquat si réceptions précédentes
        BigDecimal reliquat = bonReceptionService.calculerReliquat(bcId);
        
        model.addAttribute("bonCommande", bc);
        model.addAttribute("lignesBC", bonCommandeService.getLignesBC(bcId));
        model.addAttribute("reliquat", reliquat);
        model.addAttribute("reception", new BonReception());
        return "achats/receptions/formulaire";
    }
    
    /**
     * TODO.YML Ligne 14-15: Créer bon de réception
     */
    @PostMapping
    public String creer(@RequestParam Long bcId,
                       @RequestParam String numeroBlFournisseur,
                       @RequestParam String dateBlFournisseur,
                       RedirectAttributes redirectAttributes) {
        Utilisateur magasinier = sessionService.getUtilisateurConnecte();
        
        try {
            LocalDate dateBl = LocalDate.parse(dateBlFournisseur);
            BonReception br = bonReceptionService.creerBonReception(bcId, magasinier, numeroBlFournisseur, dateBl);
            redirectAttributes.addFlashAttribute("success", "Bon de réception créé. Procédez au contrôle des quantités.");
            return "redirect:/achats/receptions/" + br.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/achats/bons-commande/" + bcId;
        }
    }
    
    /**
     * TODO.YML Ligne 14-16: Détail réception avec contrôle quantités
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BonReception br = bonReceptionService.trouverParId(id);
        List<LigneBR> lignes = bonReceptionService.getLignesBR(id);
        
        // TODO.YML Ligne 16: Calculer les totaux pour affichage
        BigDecimal totalCommande = lignes.stream()
                .map(LigneBR::getQuantiteCommandee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRecu = lignes.stream()
                .map(LigneBR::getQuantiteRecue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalConforme = lignes.stream()
                .map(LigneBR::getQuantiteConforme)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNonConforme = lignes.stream()
                .map(LigneBR::getQuantiteNonConforme)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("reception", br);
        model.addAttribute("lignes", lignes);
        model.addAttribute("totalCommande", totalCommande);
        model.addAttribute("totalRecu", totalRecu);
        model.addAttribute("totalConforme", totalConforme);
        model.addAttribute("totalNonConforme", totalNonConforme);
        
        return "achats/receptions/detail";
    }
    
    /**
     * TODO.YML Ligne 14: Mettre à jour une ligne de réception (contrôle quantités)
     */
    @PostMapping("/{id}/lignes/{ligneBrId}")
    public String mettreAJourLigne(@PathVariable Long id,
                                   @PathVariable Long ligneBrId,
                                   @RequestParam BigDecimal quantiteRecue,
                                   @RequestParam BigDecimal quantiteConforme,
                                   @RequestParam BigDecimal quantiteNonConforme,
                                   @RequestParam(required = false) String motifNonConformite,
                                   RedirectAttributes redirectAttributes) {
        Utilisateur magasinier = sessionService.getUtilisateurConnecte();
        
        try {
            bonReceptionService.mettreAJourLigneReception(ligneBrId, quantiteRecue, 
                    quantiteConforme, quantiteNonConforme, motifNonConformite, magasinier);
            redirectAttributes.addFlashAttribute("success", "Ligne mise à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/achats/receptions/" + id;
    }
    
    /**
     * TODO.YML Ligne 15: API pour recherche article multicritère (scan, code, désignation)
     */
    @GetMapping("/api/scan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanArticle(@RequestParam Long brId, 
                                                            @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        
        // Recherche multicritère dans les lignes BR
        List<LigneBR> lignesTrouvees = bonReceptionService.rechercherLignesBR(brId, code);
        
        if (!lignesTrouvees.isEmpty()) {
            result.put("found", true);
            result.put("count", lignesTrouvees.size());
            
            // Si une seule ligne trouvée, retourner ses détails
            if (lignesTrouvees.size() == 1) {
                LigneBR ligne = lignesTrouvees.get(0);
                result.put("ligneBrId", ligne.getId());
                result.put("articleCode", ligne.getArticle().getCode());
                result.put("articleDesignation", ligne.getArticle().getDesignation());
                result.put("quantiteCommandee", ligne.getQuantiteCommandee());
                result.put("quantiteRecue", ligne.getQuantiteRecue());
            } else {
                // Plusieurs résultats - retourner la liste
                List<Map<String, Object>> lignesData = lignesTrouvees.stream()
                        .map(l -> {
                            Map<String, Object> ligneMap = new HashMap<>();
                            ligneMap.put("ligneBrId", l.getId());
                            ligneMap.put("articleCode", l.getArticle().getCode());
                            ligneMap.put("articleDesignation", l.getArticle().getDesignation());
                            ligneMap.put("quantiteCommandee", l.getQuantiteCommandee());
                            ligneMap.put("quantiteRecue", l.getQuantiteRecue());
                            return ligneMap;
                        })
                        .toList();
                result.put("lignes", lignesData);
                result.put("message", lignesTrouvees.size() + " articles correspondent à votre recherche");
            }
            return ResponseEntity.ok(result);
        } else {
            // Chercher dans tous les articles (peut-être pas dans ce BC)
            Optional<Article> article = bonReceptionService.rechercherArticleMulticritere(code);
            if (article.isPresent()) {
                result.put("found", false);
                result.put("message", "Article trouvé mais pas dans ce bon de commande");
                result.put("articleCode", article.get().getCode());
                result.put("articleDesignation", article.get().getDesignation());
            } else {
                result.put("found", false);
                result.put("message", "Aucun article trouvé pour: " + code);
            }
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * TODO.YML Ligne 16: Finaliser réception
     */
    @PostMapping("/{id}/finaliser")
    public String finaliser(@PathVariable Long id,
                           @RequestParam(required = false) String observations,
                           RedirectAttributes redirectAttributes) {
        Utilisateur magasinier = sessionService.getUtilisateurConnecte();
        
        try {
            BonReception br = bonReceptionService.finaliserReception(id, observations, magasinier);
            
            String message = "Réception finalisée - Statut: " + br.getStatut();
            if ("PARTIELLE".equals(br.getStatut())) {
                message += ". Des articles restent à recevoir (reliquat).";
            } else if ("AVEC_ECART".equals(br.getStatut())) {
                message += ". Des écarts de conformité ont été détectés.";
            }
            
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/achats/receptions/" + id;
    }
    
    /**
     * TODO.YML Ligne 16: Liste des réceptions pour un BC (suivi reliquats)
     */
    @GetMapping("/par-bc/{bcId}")
    public String listerParBC(@PathVariable Long bcId, Model model) {
        BonCommande bc = bonCommandeService.trouverParId(bcId);
        List<BonReception> receptions = bonReceptionService.listerParBC(bcId);
        BigDecimal reliquat = bonReceptionService.calculerReliquat(bcId);
        
        model.addAttribute("bonCommande", bc);
        model.addAttribute("receptions", receptions);
        model.addAttribute("reliquat", reliquat);
        
        return "achats/receptions/par-bc";
    }
}
