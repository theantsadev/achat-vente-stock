package com.gestion.achat_vente_stock.stock.controller;

import com.gestion.achat_vente_stock.referentiel.repository.ArticleRepository;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;
import com.gestion.achat_vente_stock.stock.model.LigneTransfert;
import com.gestion.achat_vente_stock.stock.model.TransfertStock;
import com.gestion.achat_vente_stock.stock.service.TransfertStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * TODO.YML Lignes 34-35: Stock > Transferts
 * Contrôleur pour la gestion des transferts inter-dépôts
 */
@Controller
@RequestMapping("/stocks/transferts")
@RequiredArgsConstructor
public class TransfertStockController {

    private final TransfertStockService transfertStockService;
    private final DepotRepository depotRepository;
    private final ArticleRepository articleRepository;

    /**
     * Liste des transferts
     */
    @GetMapping
    public String liste(Model model,
                        @RequestParam(required = false) String statut) {
        
        List<TransfertStock> transferts;
        
        if (statut != null && !statut.isEmpty()) {
            transferts = transfertStockService.listerParStatut(statut);
        } else {
            transferts = transfertStockService.listerTous();
        }
        
        model.addAttribute("transferts", transferts);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", List.of("BROUILLON", "DEMANDE", "VALIDEE", "EN_TRANSIT", "RECUE", "ANNULEE"));
        
        return "stocks/transferts/liste";
    }

    /**
     * Détail d'un transfert
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        TransfertStock transfert = transfertStockService.trouverParId(id);
        List<LigneTransfert> lignes = transfertStockService.getLignesTransfert(id);
        
        model.addAttribute("transfert", transfert);
        model.addAttribute("lignes", lignes);
        model.addAttribute("articles", articleRepository.findAll());
        
        return "stocks/transferts/detail";
    }

    /**
     * Formulaire de création d'un transfert
     */
    @GetMapping("/nouveau")
    public String formulaire(Model model) {
        model.addAttribute("depots", depotRepository.findAll());
        return "stocks/transferts/formulaire";
    }

    /**
     * Créer un transfert
     */
    @PostMapping
    public String creer(@RequestParam Long depotSourceId,
                        @RequestParam Long depotDestinationId,
                        RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            TransfertStock transfert = transfertStockService.creerTransfert(
                    depotSourceId, depotDestinationId, null);

            redirectAttributes.addFlashAttribute("success", 
                    "Transfert créé: " + transfert.getNumero());
            return "redirect:/stocks/transferts/" + transfert.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/stocks/transferts/nouveau";
        }
    }

    /**
     * Ajouter une ligne au transfert
     */
    @PostMapping("/{id}/lignes")
    public String ajouterLigne(@PathVariable Long id,
                               @RequestParam Long articleId,
                               @RequestParam BigDecimal quantiteDemandee,
                               @RequestParam(required = false) String lotNumero,
                               RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            transfertStockService.ajouterLigne(id, articleId, quantiteDemandee, lotNumero, null);
            redirectAttributes.addFlashAttribute("success", "Ligne ajoutée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * Soumettre le transfert
     */
    @PostMapping("/{id}/soumettre")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            transfertStockService.soumettreTransfert(id, null);
            redirectAttributes.addFlashAttribute("success", "Transfert soumis pour validation");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * TODO.YML Ligne 35: Valider le transfert (chef magasin)
     */
    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté et vérifier le rôle CHEF_MAGASIN
            transfertStockService.validerTransfert(id, null);
            redirectAttributes.addFlashAttribute("success", "Transfert validé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * Expédier le transfert
     */
    @PostMapping("/{id}/expedier")
    public String expedier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            transfertStockService.expedierTransfert(id, null);
            redirectAttributes.addFlashAttribute("success", "Transfert expédié");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * Réceptionner le transfert
     */
    @PostMapping("/{id}/receptionner")
    public String receptionner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            transfertStockService.receptionnerTransfert(id, null);
            redirectAttributes.addFlashAttribute("success", "Transfert réceptionné");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * Annuler le transfert
     */
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Récupérer l'utilisateur connecté
            transfertStockService.annulerTransfert(id, null);
            redirectAttributes.addFlashAttribute("success", "Transfert annulé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stocks/transferts/" + id;
    }

    /**
     * Transferts à valider (pour chef magasin)
     */
    @GetMapping("/a-valider")
    public String aValider(@RequestParam Long depotId, Model model) {
        List<TransfertStock> transferts = transfertStockService.listerTransfertsAValider(depotId);
        model.addAttribute("transferts", transferts);
        model.addAttribute("titre", "Transferts à valider");
        model.addAttribute("depots", depotRepository.findAll());
        return "stocks/transferts/liste";
    }

    /**
     * Transferts en transit vers un dépôt
     */
    @GetMapping("/en-transit")
    public String enTransit(@RequestParam Long depotId, Model model) {
        List<TransfertStock> transferts = transfertStockService.listerTransfertsEnTransitVers(depotId);
        model.addAttribute("transferts", transferts);
        model.addAttribute("titre", "Transferts en transit");
        model.addAttribute("depots", depotRepository.findAll());
        return "stocks/transferts/liste";
    }
}
