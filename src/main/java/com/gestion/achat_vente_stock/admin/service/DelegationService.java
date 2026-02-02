package com.gestion.achat_vente_stock.admin.service;

import com.gestion.achat_vente_stock.admin.model.Delegation;
import com.gestion.achat_vente_stock.admin.model.Role;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.DelegationRepository;
import com.gestion.achat_vente_stock.admin.repository.RoleRepository;
import com.gestion.achat_vente_stock.admin.repository.UtilisateurRepository;
import com.gestion.achat_vente_stock.config.security.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 61: Sécurité > Délégation
 * Service de gestion des délégations temporaires
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DelegationService {

    private final DelegationRepository delegationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final SessionService sessionService;
    private final AuditService auditService;

    /**
     * Créer une nouvelle délégation
     */
    public Delegation creerDelegation(Long delegataireId, Long roleId, 
                                      LocalDateTime dateDebut, LocalDateTime dateFin, 
                                      String justification) {
        Utilisateur delegant = sessionService.getUtilisateurConnecte();
        if (delegant == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        Utilisateur delegataire = utilisateurRepository.findById(delegataireId)
                .orElseThrow(() -> new RuntimeException("Délégataire non trouvé: " + delegataireId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleId));

        // Vérifier que le délégant a le rôle qu'il veut déléguer
        boolean hasRole = sessionService.hasRole(role.getCode());
        if (!hasRole) {
            throw new RuntimeException("Vous ne pouvez pas déléguer un rôle que vous ne possédez pas");
        }

        // Vérifier que la date de fin est après la date de début
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        Delegation delegation = new Delegation();
        delegation.setDelegant(delegant);
        delegation.setDelegataire(delegataire);
        delegation.setRole(role);
        delegation.setDateDebut(dateDebut);
        delegation.setDateFin(dateFin);
        delegation.setJustification(justification);
        delegation.setStatut("ACTIVE");

        Delegation saved = delegationRepository.save(delegation);
        
        log.info("Délégation créée: {} délègue {} à {} du {} au {}", 
                delegant.getLogin(), role.getCode(), delegataire.getLogin(), dateDebut, dateFin);
        
        auditService.logAction(delegant, "delegation", saved.getId(), "CREATE", 
                null, "Délégation de " + role.getCode() + " à " + delegataire.getLogin(), null);

        return saved;
    }

    /**
     * Annuler une délégation
     */
    public void annulerDelegation(Long delegationId) {
        Delegation delegation = delegationRepository.findById(delegationId)
                .orElseThrow(() -> new RuntimeException("Délégation non trouvée: " + delegationId));

        Utilisateur user = sessionService.getUtilisateurConnecte();
        
        // Seul le délégant ou un admin peut annuler
        if (!delegation.getDelegant().getId().equals(user.getId()) && !sessionService.hasRole("ROLE-ADMIN")) {
            throw new RuntimeException("Vous ne pouvez pas annuler cette délégation");
        }

        delegation.setStatut("ANNULEE");
        delegationRepository.save(delegation);

        log.info("Délégation {} annulée par {}", delegationId, user.getLogin());
        
        auditService.logAction(user, "delegation", delegationId, "CANCEL", 
                "ACTIVE", "ANNULEE", null);
    }

    /**
     * Lister les délégations données par un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Delegation> listerDelegationsDonnees(Long delegantId) {
        return delegationRepository.findByDelegantId(delegantId);
    }

    /**
     * Lister les délégations reçues par un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Delegation> listerDelegationsRecues(Long delegataireId) {
        return delegationRepository.findByDelegataireId(delegataireId);
    }

    /**
     * Lister les délégations actives pour un utilisateur (délégataire)
     */
    @Transactional(readOnly = true)
    public List<Delegation> listerDelegationsActives(Long delegataireId) {
        return delegationRepository.findActiveDelegations(delegataireId, LocalDateTime.now());
    }

    /**
     * Lister toutes les délégations (admin)
     */
    @Transactional(readOnly = true)
    public List<Delegation> listerToutes() {
        return delegationRepository.findAll();
    }

    /**
     * Trouver une délégation par ID
     */
    @Transactional(readOnly = true)
    public Delegation trouverParId(Long id) {
        return delegationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Délégation non trouvée: " + id));
    }

    /**
     * Vérifier si un utilisateur a une délégation active pour un rôle
     */
    @Transactional(readOnly = true)
    public boolean hasDelegation(Long utilisateurId, String roleCode) {
        List<Delegation> delegations = delegationRepository.findActiveDelegations(utilisateurId, LocalDateTime.now());
        return delegations.stream()
                .anyMatch(d -> d.getRole().getCode().equals(roleCode));
    }
}
