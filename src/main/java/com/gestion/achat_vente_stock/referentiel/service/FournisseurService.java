package com.gestion.achat_vente_stock.referentiel.service;

import com.gestion.achat_vente_stock.referentiel.model.Fournisseur;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO.YML Ligne 3: Référentiels > Fournisseurs
 * Créer/modifier fournisseurs (raison sociale, contacts, conditions)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FournisseurService {
    
    private final FournisseurRepository fournisseurRepository;
    
    public Fournisseur creerFournisseur(Fournisseur fournisseur) {
        if (fournisseur.getStatut() == null) {
            fournisseur.setStatut("ACTIF");
        }
        return fournisseurRepository.save(fournisseur);
    }
    
    public Fournisseur modifierFournisseur(Long id, Fournisseur fournisseur) {
        Fournisseur existant = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + id));
        
        existant.setCode(fournisseur.getCode());
        existant.setRaisonSociale(fournisseur.getRaisonSociale());
        existant.setAdresse(fournisseur.getAdresse());
        existant.setEmail(fournisseur.getEmail());
        existant.setTelephone(fournisseur.getTelephone());
        existant.setConditionsPaiement(fournisseur.getConditionsPaiement());
        existant.setDelaiLivraisonJours(fournisseur.getDelaiLivraisonJours());
        existant.setStatut(fournisseur.getStatut());
        
        return fournisseurRepository.save(existant);
    }
    
    public void supprimerFournisseur(Long id) {
        fournisseurRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Fournisseur trouverParId(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Fournisseur> listerTous() {
        return fournisseurRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Fournisseur> listerActifs() {
        return fournisseurRepository.findByStatut("ACTIF");
    }
}
