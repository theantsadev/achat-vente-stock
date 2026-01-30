package com.gestion.achat_vente_stock.referentiel.service;

import com.gestion.achat_vente_stock.referentiel.model.Client;
import com.gestion.achat_vente_stock.referentiel.repository.ClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO.YML Ligne 4: Référentiels > Clients
 * Créer/modifier clients (infos légales, adresses, tarifs)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    public Client creerClient(Client client) {
        if (client.getStatut() == null) {
            client.setStatut("ACTIF");
        }
        return clientRepository.save(client);
    }
    
    public Client modifierClient(Long id, Client client) {
        Client existant = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));
        
        existant.setCode(client.getCode());
        existant.setRaisonSociale(client.getRaisonSociale());
        existant.setAdresse(client.getAdresse());
        existant.setEmail(client.getEmail());
        existant.setLimiteCredit(client.getLimiteCredit());
        existant.setTarif(client.getTarif());
        existant.setConditionsPaiement(client.getConditionsPaiement());
        existant.setStatut(client.getStatut());
        
        return clientRepository.save(existant);
    }
    
    public void supprimerClient(Long id) {
        clientRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Client trouverParId(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Client> listerTous() {
        return clientRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Client> listerActifs() {
        return clientRepository.findByStatut("ACTIF");
    }
}
