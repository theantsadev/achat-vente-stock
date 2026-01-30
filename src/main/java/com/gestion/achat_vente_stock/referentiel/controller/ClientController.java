package com.gestion.achat_vente_stock.referentiel.controller;

import com.gestion.achat_vente_stock.referentiel.model.Client;
import com.gestion.achat_vente_stock.referentiel.service.ClientService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TODO.YML Ligne 4: Référentiels > Clients
 * Page "Gestion clients"
 * Créer/modifier clients (infos légales, adresses, tarifs)
 */
@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    public String listerClients(Model model) {
        model.addAttribute("clients", clientService.listerTous());
        model.addAttribute("titre", "Gestion des Clients");
        return "referentiel/clients/liste";
    }
    
    @GetMapping("/nouveau")
    public String nouveauClient(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("titre", "Nouveau Client");
        model.addAttribute("action", "Créer");
        return "referentiel/clients/formulaire";
    }
    
    @PostMapping
    public String creerClient(@ModelAttribute Client client, RedirectAttributes redirectAttributes) {
        try {
            clientService.creerClient(client);
            redirectAttributes.addFlashAttribute("message", "Client créé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/clients";
    }
    
    @GetMapping("/{id}/modifier")
    public String modifierClient(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.trouverParId(id));
        model.addAttribute("titre", "Modifier Client");
        model.addAttribute("action", "Modifier");
        return "referentiel/clients/formulaire";
    }
    
    @PostMapping("/{id}")
    public String mettreAJourClient(@PathVariable Long id, @ModelAttribute Client client, 
                                     RedirectAttributes redirectAttributes) {
        try {
            clientService.modifierClient(id, client);
            redirectAttributes.addFlashAttribute("message", "Client modifié avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/clients";
    }
    
    @GetMapping("/{id}/supprimer")
    public String supprimerClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.supprimerClient(id);
            redirectAttributes.addFlashAttribute("message", "Client supprimé avec succès");
            redirectAttributes.addFlashAttribute("typeMessage", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur: " + e.getMessage());
            redirectAttributes.addFlashAttribute("typeMessage", "error");
        }
        return "redirect:/clients";
    }
}
