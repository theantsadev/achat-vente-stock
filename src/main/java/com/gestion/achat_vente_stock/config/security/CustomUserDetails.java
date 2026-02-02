package com.gestion.achat_vente_stock.config.security;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation de UserDetails pour Spring Security
 * Encapsule l'entité Utilisateur avec les méthodes requises par Spring Security
 */
public class CustomUserDetails implements UserDetails {

    private final Utilisateur utilisateur;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        
        // Convertir les rôles de l'utilisateur en GrantedAuthority
        this.authorities = utilisateur.getRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .filter(ur -> isRoleActif(ur))
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getCode()))
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si le rôle est actuellement actif (dans la période de validité)
     */
    private boolean isRoleActif(UtilisateurRole ur) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Vérifier date début
        if (ur.getDateDebut() != null && now.isBefore(ur.getDateDebut())) {
            return false;
        }
        
        // Vérifier date fin
        if (ur.getDateFin() != null && now.isAfter(ur.getDateFin())) {
            return false;
        }
        
        return true;
    }

    /**
     * Retourne l'entité Utilisateur complète
     * Utile pour accéder aux informations métier (site, service, etc.)
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    /**
     * Retourne l'ID de l'utilisateur
     */
    public Long getId() {
        return utilisateur.getId();
    }

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getNomComplet() {
        return utilisateur.getNomComplet();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return utilisateur.getPassword();
    }

    @Override
    public String getUsername() {
        return utilisateur.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return utilisateur.getActif() != null && utilisateur.getActif();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.getActif() != null && utilisateur.getActif();
    }

    /**
     * Vérifie si l'utilisateur possède un rôle spécifique
     */
    public boolean hasRole(String roleCode) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleCode));
    }

    /**
     * Vérifie si l'utilisateur possède au moins un des rôles spécifiés
     */
    public boolean hasAnyRole(String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (hasRole(roleCode)) {
                return true;
            }
        }
        return false;
    }
}
