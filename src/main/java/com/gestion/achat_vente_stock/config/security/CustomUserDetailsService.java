package com.gestion.achat_vente_stock.config.security;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de chargement des utilisateurs pour Spring Security
 * Charge l'utilisateur depuis la base de données avec ses rôles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Charge un utilisateur par son login
     * Méthode appelée automatiquement par Spring Security lors de l'authentification
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur: {}", username);
        
        Utilisateur utilisateur = utilisateurRepository.findByLoginWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });

        // Vérifier si l'utilisateur est actif
        if (utilisateur.getActif() == null || !utilisateur.getActif()) {
            log.warn("Tentative de connexion d'un utilisateur désactivé: {}", username);
            throw new UsernameNotFoundException("Compte désactivé: " + username);
        }

        log.info("Utilisateur chargé avec succès: {} ({} rôles)", 
                username, utilisateur.getRoles().size());
        
        return new CustomUserDetails(utilisateur);
    }
}
