package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.*;
import com.gestion.achat_vente_stock.achat.repository.FactureFournisseurRepository;
import com.gestion.achat_vente_stock.achat.repository.LigneFactureFournisseurRepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;
import com.gestion.achat_vente_stock.achat.repository.LigneBRRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Lignes 17-18: Achats > Facture Fournisseur
 * - Ligne 17: Rapprocher facture vs réception vs BC (3-way match)
 * - Ligne 18: Bloquer paiement si écart non résolu
 * - Ligne 58: Séparation tâches (réceptionner ≠ valider facture)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FactureFournisseurService {
    
    private final FactureFournisseurRepository factureFournisseurRepository;
    public FactureFournisseurRepository getFactureFournisseurRepository() {
        return factureFournisseurRepository;
    }

    private final LigneFactureFournisseurRepository ligneFactureFournisseurRepository;
    private final BonCommandeService bonCommandeService;
    private final BonReceptionService bonReceptionService;
    private final LigneBRRepository ligneBRRepository;
    private final AuditService auditService;
    
    /**
     * Créer une facture fournisseur
     */
    public FactureFournisseur creerFactureFournisseur(FactureFournisseur facture, 
                                                       List<LigneFactureFournisseur> lignes) {
        facture.setNumero(genererNumeroFF());
        facture.setStatut("EN_ATTENTE");
        facture.setThreeWayMatchOk(false);
        
        FactureFournisseur saved = factureFournisseurRepository.save(facture);
        
        // Sauvegarder les lignes
        for (LigneFactureFournisseur ligne : lignes) {
            ligne.setFactureFournisseur(saved);
            ligneFactureFournisseurRepository.save(ligne);
        }
        
        // TODO.YML Ligne 17: Effectuer le 3-way match automatiquement
        effectuerThreeWayMatch(saved.getId());
        
        return saved;
    }
    
    /**
     * TODO.YML Ligne 17: 3-way match (Facture vs Réception vs BC)
     * Rapprochement automatique des 3 documents
     */
    public void effectuerThreeWayMatch(Long factureId) {
        FactureFournisseur facture = trouverParId(factureId);
        BonCommande bc = facture.getBonCommande();
        
        if (bc == null) {
            facture.setThreeWayMatchOk(false);
            facture.setEcartsThreeWay("Aucun BC associé");
            factureFournisseurRepository.save(facture);
            return;
        }
        
        // Récupérer les réceptions
        List<BonReception> receptions = bonReceptionService.listerParBC(bc.getId());
        
        if (receptions.isEmpty()) {
            facture.setThreeWayMatchOk(false);
            facture.setEcartsThreeWay("Aucune réception enregistrée");
            factureFournisseurRepository.save(facture);
            return;
        }
        
        StringBuilder ecarts = new StringBuilder();
        boolean matchOk = true;
        
        // Vérifier les montants
        BigDecimal montantBC = bc.getMontantTotalTtc();
        BigDecimal montantFacture = facture.getMontantTtc();
        BigDecimal tolerance = new BigDecimal("0.01"); // 1 centime de tolérance
        
        if (montantFacture.subtract(montantBC).abs().compareTo(tolerance) > 0) {
            matchOk = false;
            ecarts.append("Écart montant: BC=").append(montantBC)
                  .append(" vs Facture=").append(montantFacture).append("; ");
        }
        
        // Vérifier les quantités réceptionnées
        List<LigneFactureFournisseur> lignesFF = ligneFactureFournisseurRepository
                .findByFactureFournisseurId(factureId);
        
        for (LigneFactureFournisseur ligneFF : lignesFF) {
            BigDecimal qteFacturee = ligneFF.getQuantite();
            
            // Trouver la quantité réceptionnée pour cet article
            BigDecimal qteReceptionnee = BigDecimal.ZERO;
            for (BonReception br : receptions) {
                List<LigneBR> lignesBR = ligneBRRepository.findByBonReceptionId(br.getId());
                for (LigneBR ligneBR : lignesBR) {
                    if (ligneBR.getArticle().getId().equals(ligneFF.getArticle().getId())) {
                        qteReceptionnee = qteReceptionnee.add(ligneBR.getQuantiteConforme());
                    }
                }
            }
            
            if (qteFacturee.compareTo(qteReceptionnee) > 0) {
                matchOk = false;
                ecarts.append("Article ").append(ligneFF.getArticle().getCode())
                      .append(": Facturé=").append(qteFacturee)
                      .append(" > Réceptionné=").append(qteReceptionnee).append("; ");
            }
        }
        
        facture.setThreeWayMatchOk(matchOk);
        facture.setEcartsThreeWay(ecarts.length() > 0 ? ecarts.toString() : null);
        
        // TODO.YML Ligne 18: Bloquer si écart non résolu
        if (!matchOk) {
            facture.setStatut("BLOQUEE");
        }
        
        factureFournisseurRepository.save(facture);
    }
    
    /**
     * TODO.YML Ligne 58: Valider facture (valideur ≠ réceptionnaire)
     */
    public void validerFacture(Long factureId, Utilisateur valideur) {
        FactureFournisseur facture = trouverParId(factureId);
        
        // TODO.YML Ligne 58: Vérifier que le valideur n'est pas le réceptionnaire
        BonCommande bc = facture.getBonCommande();
        List<BonReception> receptions = bonReceptionService.listerParBC(bc.getId());
        
        for (BonReception br : receptions) {
            if (br.getMagasinier() != null && 
                br.getMagasinier().getId().equals(valideur.getId())) {
                throw new RuntimeException("Le réceptionnaire ne peut pas valider la facture (séparation des tâches)");
            }
        }
        
        // Vérifier le 3-way match
        if (!facture.getThreeWayMatchOk()) {
            throw new RuntimeException("La facture présente des écarts non résolus: " + 
                                      facture.getEcartsThreeWay());
        }
        
        facture.setStatut("VALIDEE");
        facture.setValideeAt(LocalDateTime.now());
        facture.setValideeBy(valideur);
        
        factureFournisseurRepository.save(facture);
        
        // Audit
        auditService.logAction(valideur, "facture_fournisseur", factureId, 
                               "VALIDATE", "EN_ATTENTE", "VALIDEE", null);
    }
    
    /**
     * TODO.YML Ligne 18: Débloquer facture après résolution des écarts
     */
    public void debloquerFacture(Long factureId, String justification, Utilisateur valideur) {
        FactureFournisseur facture = trouverParId(factureId);
        
        if (!"BLOQUEE".equals(facture.getStatut())) {
            throw new RuntimeException("Cette facture n'est pas bloquée");
        }
        
        facture.setStatut("EN_ATTENTE");
        factureFournisseurRepository.save(facture);
        
        // Audit
        auditService.logAction(valideur, "facture_fournisseur", factureId, 
                               "UNBLOCK", "BLOQUEE", "EN_ATTENTE", justification);
    }
    
    @Transactional(readOnly = true)
    public FactureFournisseur trouverParId(Long id) {
        return factureFournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture fournisseur non trouvée: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<FactureFournisseur> listerTous() {
        return factureFournisseurRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<FactureFournisseur> listerFacturesBloquees() {
        return factureFournisseurRepository.findFacturesBloquees();
    }
    
    @Transactional(readOnly = true)
    public List<FactureFournisseur> listerAvecEcartsThreeWay() {
        return factureFournisseurRepository.findAvecEcartsThreeWay();
    }
    
    private String genererNumeroFF() {
        return "FF" + System.currentTimeMillis();
    }
}
