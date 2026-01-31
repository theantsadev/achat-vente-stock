package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.DemandeAchat;
import com.gestion.achat_vente_stock.achat.model.LigneDA;
import com.gestion.achat_vente_stock.achat.model.ValidationDA;
import com.gestion.achat_vente_stock.achat.repository.DemandeAchatRepository;
import com.gestion.achat_vente_stock.achat.repository.LigneDARepository;
import com.gestion.achat_vente_stock.achat.repository.ValidationDARepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Lignes 7-9: Achats > Demande Achat
 * - Ligne 7: Créer demande d'achat (articles, quantités, fournisseur)
 * - Ligne 8: Workflow approbation N1/N2/N3 selon seuils montant
 * - Ligne 9: Bloquer auto-approbation (créateur ≠ approbateur)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DemandeAchatService {

    private final DemandeAchatRepository demandeAchatRepository;
    private final LigneDARepository ligneDARepository;
    private final ValidationDARepository validationDARepository;
    private final AuditService auditService;

    // TODO.YML Ligne 8: Seuils pour workflow d'approbation
    private static final BigDecimal SEUIL_N1 = new BigDecimal("10000"); // 10K€
    private static final BigDecimal SEUIL_N2 = new BigDecimal("50000"); // 50K€
    private static final BigDecimal SEUIL_N3 = new BigDecimal("100000"); // 100K€

    /**
     * TODO.YML Ligne 7: Créer demande d'achat
     */
    public DemandeAchat creerDemandeAchat(DemandeAchat da, List<LigneDA> lignes, Utilisateur demandeur) {
        // Générer le numéro
        da.setNumero(genererNumeroDA());
        da.setDemandeur(demandeur);
        da.setStatut("BROUILLON");
        da.setCreatedAt(LocalDateTime.now());

        // Calculer le montant total
        BigDecimal montantTotal = lignes.stream()
                .map(ligne -> ligne.getPrixEstimeHt().multiply(ligne.getQuantite()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        da.setMontantEstimeHt(montantTotal);

        DemandeAchat daSaved = demandeAchatRepository.save(da);

        // Sauvegarder les lignes
        for (LigneDA ligne : lignes) {
            ligne.setDemandeAchat(daSaved);
            ligneDARepository.save(ligne);
        }

        // Audit
        auditService.logAction(demandeur, "demande_achat", daSaved.getId(),
                "CREATE", null, daSaved.toString(), null);

        return daSaved;
    }

    /**
     * TODO.YML Ligne 8: Soumettre DA pour approbation (déclenche le workflow)
     */
    public void soumettrePourApprobation(Long daId, Utilisateur demandeur) {
        DemandeAchat da = trouverParId(daId);

        if (!"BROUILLON".equals(da.getStatut())) {
            throw new RuntimeException("Seules les DA en brouillon peuvent être soumises");
        }

        da.setStatut("EN_ATTENTE");
        demandeAchatRepository.save(da);

        // Audit
        auditService.logAction(demandeur, "demande_achat", daId,
                "SUBMIT", "BROUILLON", "EN_ATTENTE", null);
    }

    /**
     * TODO.YML Ligne 8-9: Valider/Approuver une DA
     * - Workflow N1/N2/N3 selon seuils
     * - Bloquer auto-approbation
     */
    public void validerDemandeAchat(Long daId, Utilisateur valideur, Integer niveau,
            String decision, String commentaire) {
        DemandeAchat da = trouverParId(daId);

        // TODO.YML Ligne 9: Règle métier - Bloquer auto-approbation
        if (da.getDemandeur().getId().equals(valideur.getId())) {
            throw new RuntimeException("Le créateur ne peut pas approuver sa propre demande d'achat");
        }

        // TODO.YML Ligne 57: Séparation des tâches
        if (validationDARepository.validateurEstCreateur(daId, valideur.getId())) {
            throw new RuntimeException("Conflit d'intérêt: validation interdite");
        }

        // Créer la validation
        ValidationDA validation = new ValidationDA();
        validation.setDemandeAchat(da);
        validation.setValideur(valideur);
        validation.setNiveau(niveau);
        validation.setDecision(decision);
        validation.setCommentaire(commentaire);
        validation.setDateValidation(LocalDateTime.now());
        validationDARepository.save(validation);

        // Mettre à jour le statut de la DA selon la décision et le workflow
        if ("REJETEE".equals(decision)) {
            da.setStatut("REJETEE");
        } else if ("APPROUVEE".equals(decision)) {
            // TODO.YML Ligne 8: Vérifier si d'autres niveaux sont nécessaires
            Integer niveauRequis = determinerNiveauApprobationRequis(da.getMontantEstimeHt());

            if (niveau >= niveauRequis) {
                da.setStatut("APPROUVEE");
            } else {
                // Attente du niveau suivant
                da.setStatut("EN_ATTENTE");
            }
        }

        demandeAchatRepository.save(da);

        // Audit
        auditService.logAction(valideur, "demande_achat", daId,
                "APPROVE_N" + niveau, da.getStatut(), decision, null);
    }

    /**
     * TODO.YML Ligne 8: Déterminer le niveau d'approbation requis selon le montant
     */
    private Integer determinerNiveauApprobationRequis(BigDecimal montant) {
        if (montant.compareTo(SEUIL_N3) >= 0) {
            return 3; // DG/DAF
        } else if (montant.compareTo(SEUIL_N2) >= 0) {
            return 2; // Directeur
        } else if (montant.compareTo(SEUIL_N1) >= 0) {
            return 1; // Chef de service
        } else {
            return 0; // Pas d'approbation nécessaire
        }
    }

    public DemandeAchat modifierDemandeAchat(Long id, DemandeAchat da) {
        DemandeAchat existant = trouverParId(id);

        if (!"BROUILLON".equals(existant.getStatut())) {
            throw new RuntimeException("Seules les DA en brouillon peuvent être modifiées");
        }

        existant.setDateBesoin(da.getDateBesoin());
        existant.setJustification(da.getJustification());
        existant.setUrgence(da.getUrgence());

        return demandeAchatRepository.save(existant);
    }

    @Transactional(readOnly = true)
    public DemandeAchat trouverParId(Long id) {
        return demandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'achat non trouvée: " + id));
    }

    @Transactional(readOnly = true)
    public DemandeAchat obtenirParId(Long id) {
        return demandeAchatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'achat introuvable avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DemandeAchat> listerTous() {
        return demandeAchatRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DemandeAchat> listerParStatut(String statut) {
        return demandeAchatRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<LigneDA> getLignesDA(Long daId) {
        return ligneDARepository.findByDemandeAchatId(daId);
    }

    @Transactional(readOnly = true)
    public List<ValidationDA> getValidations(Long daId) {
        return validationDARepository.findByDemandeAchatId(daId);
    }

    private String genererNumeroDA() {
        return "DA" + System.currentTimeMillis();
    }
}
