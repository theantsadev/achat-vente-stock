package com.gestion.achat_vente_stock.admin.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 68: Admin > Utilisateurs
 * Créer/modifier/désactiver utilisateurs
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getActif() == null) {
            utilisateur.setActif(true);
        }
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur modifierUtilisateur(Long id, Utilisateur utilisateur) {
        Utilisateur existant = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));

        existant.setLogin(utilisateur.getLogin());
        existant.setNom(utilisateur.getNom());
        existant.setPrenom(utilisateur.getPrenom());
        existant.setEmail(utilisateur.getEmail());
        existant.setService(utilisateur.getService());
        existant.setSite(utilisateur.getSite());
        existant.setManager(utilisateur.getManager());
        existant.setActif(utilisateur.getActif());

        return utilisateurRepository.save(existant);
    }

    public void desactiverUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    public void enregistrerConnexion(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
        utilisateur.setLastLogin(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParLogin(String login) {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + login));
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listerActifs() {
        return utilisateurRepository.findByActif(true);
    }
}
