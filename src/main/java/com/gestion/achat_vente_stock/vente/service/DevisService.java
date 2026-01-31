package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.vente.model.Devis;
import com.gestion.achat_vente_stock.vente.model.LigneDevis;
import com.gestion.achat_vente_stock.vente.repository.DevisRepository;
import com.gestion.achat_vente_stock.vente.repository.LigneDevisRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 20-21: Ventes > Devis
 * - Ligne 20: Créer devis/pro-forma client (articles, prix, remises)
 * - Ligne 21: Valider remises > plafond par responsable ventes
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DevisService {

    private final DevisRepository devisRepository;
    private final LigneDevisRepository ligneDevisRepository;
    private final AuditService auditService;

    /** Préfixe pour les numéros de devis */
    private static final String PREFIXE_NUMERO = "DV";

    /**
     * TODO.YML Ligne 21: Seuil de remise nécessitant validation
     */
    private static final BigDecimal SEUIL_REMISE_VALIDATION = new BigDecimal("15"); // 15%

    // ==================== CRUD ====================

    /**
     * TODO.YML Ligne 20: Créer un nouveau devis
     */
    public Devis creerDevis(Devis devis, Utilisateur commercial) {
        // Génération du numéro
        devis.setNumero(genererNumero());
        devis.setCommercial(commercial);
        devis.setDateDevis(LocalDate.now());
        devis.setStatut("BROUILLON");

        // Calcul des totaux
        calculerTotaux(devis);

        Devis saved = devisRepository.save(devis);
        for (LigneDevis ligne : devis.getLignes()) {
            ligne.setDevis(saved);
            ligneDevisRepository.save(ligne);
        }
        // Audit
        auditService.logAction(commercial, "devis", saved.getId(),
                "CREATE", null, "Devis créé: " + saved.getNumero(), null);

        return saved;
    }

    /**
     * Enregistrer un devis (création ou mise à jour)
     */
    public Devis enregistrer(Devis devis) {
        if (devis.getId() == null) {
            devis.setNumero(genererNumero());
            devis.setDateDevis(LocalDate.now());
            devis.setStatut("BROUILLON");
        }
        calculerTotaux(devis);
        return devisRepository.save(devis);
    }

    /**
     * Récupérer tous les devis
     */
    public List<Devis> listerTous() {
        return devisRepository.findAll();
    }

    /**
     * Récupérer un devis par ID
     */
    public Devis obtenirParId(Long id) {
        return devisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Devis non trouvé: " + id));
    }

    /**
     * Récupérer les devis par statut
     */
    public List<Devis> listerParStatut(String statut) {
        return devisRepository.findByStatut(statut);
    }

    /**
     * Récupérer les devis par client
     */
    public List<Devis> listerParClient(Long clientId) {
        return devisRepository.findByClientId(clientId);
    }

    // ==================== WORKFLOW ====================

    /**
     * TODO.YML Ligne 21: Soumettre le devis pour validation si remise > seuil
     */
    public void soumettreDevis(Long devisId, Utilisateur commercial) {
        Devis devis = obtenirParId(devisId);

        if (!"BROUILLON".equals(devis.getStatut())) {
            throw new IllegalStateException("Seuls les devis en brouillon peuvent être soumis");
        }

        // Vérifier si la remise nécessite validation
        boolean remiseElevee = devis.getRemiseGlobalePourcent() != null
                && devis.getRemiseGlobalePourcent().compareTo(SEUIL_REMISE_VALIDATION) > 0;

        // Vérifier aussi les remises sur les lignes
        for (LigneDevis ligne : devis.getLignes()) {
            if (ligne.getRemisePourcent() != null
                    && ligne.getRemisePourcent().compareTo(SEUIL_REMISE_VALIDATION) > 0) {
                remiseElevee = true;
                break;
            }
        }

        if (remiseElevee) {
            devis.setStatut("EN_ATTENTE_VALIDATION");
            auditService.logAction(commercial, "devis", devisId,
                    "SUBMIT_VALIDATION", "BROUILLON", "EN_ATTENTE_VALIDATION",
                    "Remise > " + SEUIL_REMISE_VALIDATION + "%, validation requise");
        } else {
            devis.setStatut("VALIDE");
            auditService.logAction(commercial, "devis", devisId,
                    "SUBMIT", "BROUILLON", "VALIDE", null);
        }

        devisRepository.save(devis);
    }

    /**
     * TODO.YML Ligne 21: Valider le devis (responsable ventes)
     */
    public void validerDevis(Long devisId, Utilisateur responsable, boolean approuve, String commentaire) {
        Devis devis = obtenirParId(devisId);

        if (!"EN_ATTENTE_VALIDATION".equals(devis.getStatut())) {
            throw new IllegalStateException("Ce devis n'est pas en attente de validation");
        }

        String ancienStatut = devis.getStatut();
        if (approuve) {
            devis.setStatut("VALIDE");
        } else {
            devis.setStatut("BROUILLON"); // Retour au commercial pour révision
        }

        devisRepository.save(devis);

        auditService.logAction(responsable, "devis", devisId,
                approuve ? "APPROVE" : "REJECT", ancienStatut, devis.getStatut(), commentaire);
    }

    /**
     * Marquer le devis comme accepté par le client
     */
    public void accepterDevis(Long devisId, Utilisateur utilisateur) {
        Devis devis = obtenirParId(devisId);

        if (!"VALIDE".equals(devis.getStatut())) {
            throw new IllegalStateException("Seuls les devis validés peuvent être acceptés");
        }

        devis.setStatut("ACCEPTE");
        devisRepository.save(devis);

        auditService.logAction(utilisateur, "devis", devisId,
                "ACCEPT", "VALIDE", "ACCEPTE", "Client a accepté le devis");
    }

    /**
     * Marquer le devis comme refusé par le client
     */
    public void refuserDevis(Long devisId, Utilisateur utilisateur, String motif) {
        Devis devis = obtenirParId(devisId);

        if (!"VALIDE".equals(devis.getStatut())) {
            throw new IllegalStateException("Seuls les devis validés peuvent être refusés");
        }

        devis.setStatut("REFUSE");
        devisRepository.save(devis);

        auditService.logAction(utilisateur, "devis", devisId,
                "REFUSE", "VALIDE", "REFUSE", motif);
    }

    // ==================== CALCULS ====================

    /**
     * Calculer les totaux du devis
     */
    public void calculerTotaux(Devis devis) {
        BigDecimal totalHt = BigDecimal.ZERO;

        // Calcul des lignes
        for (LigneDevis ligne : devis.getLignes()) {
            ligne.calculerMontantHt();
            if (ligne.getMontantLigneHt() != null) {
                totalHt = totalHt.add(ligne.getMontantLigneHt());
            }
        }

        // Appliquer remise globale
        if (devis.getRemiseGlobalePourcent() != null
                && devis.getRemiseGlobalePourcent().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remise = totalHt.multiply(devis.getRemiseGlobalePourcent())
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            totalHt = totalHt.subtract(remise);
        }

        devis.setMontantTotalHt(totalHt);

        // TVA à 20%
        BigDecimal tva = totalHt.multiply(new BigDecimal("0.20")).setScale(4, RoundingMode.HALF_UP);
        devis.setMontantTva(tva);

        // TTC
        devis.setMontantTotalTtc(totalHt.add(tva));
    }

    // ==================== UTILITAIRES ====================

    /**
     * Générer un numéro unique de devis
     */
    private String genererNumero() {
        long count = devisRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }

    /**
     * Vérifier les devis expirés
     */
    public void verifierDevisExpires() {
        List<Devis> devisExpires = devisRepository.findByDateValiditeBeforeAndStatutNot(
                LocalDate.now(), "EXPIRE");

        for (Devis devis : devisExpires) {
            if ("VALIDE".equals(devis.getStatut()) || "BROUILLON".equals(devis.getStatut())) {
                devis.setStatut("EXPIRE");
                devisRepository.save(devis);
            }
        }
    }
}
