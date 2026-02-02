package com.gestion.achat_vente_stock.config.security;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.model.UtilisateurRole;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de session utilisateur
 * Fournit des méthodes utilitaires pour accéder à l'utilisateur connecté
 * et vérifier ses droits dans toute l'application
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;

    /**
     * Récupère l'utilisateur connecté
     * 
     * @return l'utilisateur connecté ou null si non authentifié
     */
    @Transactional(readOnly = true)
    public Utilisateur getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        if (auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            // Recharger depuis la base pour avoir l'entité attachée
            return utilisateurRepository.findById(userDetails.getId()).orElse(null);
        }

        return null;
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public Long getUtilisateurConnecteId() {
        Utilisateur user = getUtilisateurConnecte();
        return user != null ? user.getId() : null;
    }

    /**
     * Récupère le login de l'utilisateur connecté
     */
    public String getLoginConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public boolean estAuthentifie() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Vérifie si l'utilisateur connecté possède un rôle spécifique
     */
    public boolean hasRole(String roleCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleCode));
    }

    /**
     * Vérifie si l'utilisateur connecté possède au moins un des rôles spécifiés
     */
    public boolean hasAnyRole(String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (hasRole(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO.YML Ligne 56: ABAC - Vérifier si l'utilisateur a accès à un dépôt
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToDepot(Long depotId) {
        Utilisateur user = getUtilisateurConnecte();
        if (user == null)
            return false;

        // Admin a accès à tout
        if (hasRole("ROLE-ADMIN"))
            return true;

        // Vérifier si un des rôles de l'utilisateur donne accès à ce dépôt
        List<UtilisateurRole> roles = utilisateurRoleRepository.findByUtilisateurId(user.getId());
        return roles.stream()
                .filter(ur -> isRoleActif(ur))
                .anyMatch(ur -> ur.getDepot() == null || ur.getDepot().getId().equals(depotId));
    }

    /**
     * TODO.YML Ligne 56: ABAC - Vérifier si l'utilisateur a accès à un site
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToSite(Long siteId) {
        Utilisateur user = getUtilisateurConnecte();
        if (user == null)
            return false;

        // Admin a accès à tout
        if (hasRole("ROLE-ADMIN"))
            return true;

        // Vérifier si un des rôles de l'utilisateur donne accès à ce site
        List<UtilisateurRole> roles = utilisateurRoleRepository.findByUtilisateurId(user.getId());
        return roles.stream()
                .filter(ur -> isRoleActif(ur))
                .anyMatch(ur -> ur.getSite() == null || ur.getSite().getId().equals(siteId));
    }

    /**
     * TODO.YML Ligne 56, 8: ABAC - Vérifier si l'utilisateur peut approuver un
     * montant
     */
    @Transactional(readOnly = true)
    public boolean canApproveMontant(BigDecimal montant, String roleCode) {
        Utilisateur user = getUtilisateurConnecte();
        if (user == null)
            return false;

        // Admin peut tout approuver
        if (hasRole("ROLE-ADMIN"))
            return true;

        // Chercher le rôle avec le seuil d'approbation
        List<UtilisateurRole> roles = utilisateurRoleRepository.findByUtilisateurIdAndRoleCode(user.getId(), roleCode);

        return roles.stream()
                .filter(ur -> isRoleActif(ur))
                .anyMatch(ur -> {
                    // Si pas de montant max défini, peut tout approuver avec ce rôle
                    if (ur.getMontantMax() == null)
                        return true;
                    // Sinon vérifier si le montant est inférieur au seuil
                    return montant.compareTo(ur.getMontantMax()) <= 0;
                });
    }

    /**
     * TODO.YML Ligne 57: Règle métier - Vérifier la séparation des tâches
     * Vérifie que l'utilisateur actuel n'est pas le créateur d'un document
     */
    public boolean canApprove(Long createurId) {
        Long currentUserId = getUtilisateurConnecteId();
        if (currentUserId == null)
            return false;

        // L'utilisateur ne peut pas approuver ce qu'il a créé
        return !currentUserId.equals(createurId);
    }

    /**
     * Obtenir le montant maximum d'approbation pour un rôle donné
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getMontantMaxApprobation(String roleCode) {
        Utilisateur user = getUtilisateurConnecte();
        if (user == null)
            return Optional.empty();

        List<UtilisateurRole> roles = utilisateurRoleRepository.findByUtilisateurIdAndRoleCode(user.getId(), roleCode);

        return roles.stream()
                .filter(ur -> isRoleActif(ur))
                .map(UtilisateurRole::getMontantMax)
                .filter(m -> m != null)
                .max(BigDecimal::compareTo);
    }

    /**
     * Vérifie si un rôle utilisateur est actuellement actif
     */
    private boolean isRoleActif(UtilisateurRole ur) {
        LocalDateTime now = LocalDateTime.now();

        if (ur.getDateDebut() != null && now.isBefore(ur.getDateDebut())) {
            return false;
        }

        if (ur.getDateFin() != null && now.isAfter(ur.getDateFin())) {
            return false;
        }

        return true;
    }

    /**
     * Enregistrer la dernière connexion de l'utilisateur
     */
    @Transactional
    public void enregistrerConnexion() {
        Utilisateur user = getUtilisateurConnecte();
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            utilisateurRepository.save(user);
            log.info("Connexion enregistrée pour l'utilisateur: {}", user.getLogin());
        }
    }
}
