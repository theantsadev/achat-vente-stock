package com.gestion.achat_vente_stock.referentiel.service;

import com.gestion.achat_vente_stock.referentiel.model.Depot;
import com.gestion.achat_vente_stock.referentiel.repository.DepotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO.YML Ligne 5: Référentiels > Dépôts
 * Créer dépôts/emplacements (site, zone, adresse)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepotService {
    
    private final DepotRepository depotRepository;
    
    public Depot creerDepot(Depot depot) {
        if (depot.getActif() == null) {
            depot.setActif(true);
        }
        return depotRepository.save(depot);
    }
    
    public Depot modifierDepot(Long id, Depot depot) {
        Depot existant = depotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé: " + id));
        
        existant.setCode(depot.getCode());
        existant.setLibelle(depot.getLibelle());
        existant.setSite(depot.getSite());
        existant.setAdresse(depot.getAdresse());
        existant.setType(depot.getType());
        existant.setActif(depot.getActif());
        
        return depotRepository.save(existant);
    }
    
    public void supprimerDepot(Long id) {
        depotRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Depot trouverParId(Long id) {
        return depotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Depot> listerTous() {
        return depotRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Depot> listerActifs() {
        return depotRepository.findByActif(true);
    }
}
