package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.*;
import com.gestion.achat_vente_stock.achat.repository.BonReceptionRepository;
import com.gestion.achat_vente_stock.achat.repository.LigneBRRepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.service.AuditService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TODO.YML Lignes 14-16: Achats > Réception
 * - Ligne 14: Enregistrer réception (contrôle quantités vs BC)
 * - Ligne 15: Générer bon de réception, scanner code-barres
 * - Ligne 16: Gérer réceptions partielles et reliquats
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BonReceptionService {
    
    private final BonReceptionRepository bonReceptionRepository;
    private final LigneBRRepository ligneBRRepository;
    private final BonCommandeService bonCommandeService;
    private final AuditService auditService;
    
    /**
     * TODO.YML Ligne 14-15: Créer bon de réception
     */
    public BonReception creerBonReception(Long bcId, Utilisateur magasinier, 
                                          String numeroBlFournisseur, LocalDate dateBlFournisseur) {
        BonCommande bc = bonCommandeService.trouverParId(bcId);
        
        if (!"ENVOYEE".equals(bc.getStatut()) && !"APPROUVEE".equals(bc.getStatut())) {
            throw new RuntimeException("Ce BC ne peut pas encore être réceptionné");
        }
        
        // TODO.YML Ligne 15: Générer bon de réception
        BonReception br = new BonReception();
        br.setNumero(genererNumeroBR());
        br.setBonCommande(bc);
        br.setMagasinier(magasinier);
        br.setNumeroBlFournisseur(numeroBlFournisseur);
        br.setDateBlFournisseur(dateBlFournisseur);
        br.setDateReception(LocalDate.now());
        br.setStatut("PARTIELLE"); // Par défaut, sera mis à jour après contrôle
        
        BonReception brSaved = bonReceptionRepository.save(br);
        
        // Audit
        auditService.logAction(magasinier, "bon_reception", brSaved.getId(), 
                               "CREATE", null, brSaved.toString(), null);
        
        return brSaved;
    }
    
    /**
     * TODO.YML Ligne 14: Enregistrer ligne de réception avec contrôle quantités
     */
    public LigneBR enregistrerLigneReception(Long brId, LigneBC ligneBC, 
                                             BigDecimal quantiteRecue, BigDecimal quantiteConforme,
                                             BigDecimal quantiteNonConforme, String motifNonConformite,
                                             Utilisateur magasinier) {
        BonReception br = trouverParId(brId);
        
        LigneBR ligneBR = new LigneBR();
        ligneBR.setBonReception(br);
        ligneBR.setLigneBc(ligneBC);
        ligneBR.setArticle(ligneBC.getArticle());
        ligneBR.setQuantiteCommandee(ligneBC.getQuantite());
        ligneBR.setQuantiteRecue(quantiteRecue);
        ligneBR.setQuantiteConforme(quantiteConforme);
        ligneBR.setQuantiteNonConforme(quantiteNonConforme);
        ligneBR.setMotifNonConformite(motifNonConformite);
        
        LigneBR saved = ligneBRRepository.save(ligneBR);
        
        // TODO.YML Ligne 16: Mettre à jour le statut (partielle/complète)
        mettreAJourStatutReception(brId);
        
        // Audit
        auditService.logAction(magasinier, "ligne_br", saved.getId(), 
                               "RECEIVE", null, saved.toString(), null);
        
        return saved;
    }
    
    /**
     * TODO.YML Ligne 16: Déterminer si la réception est partielle ou complète
     */
    private void mettreAJourStatutReception(Long brId) {
        BonReception br = trouverParId(brId);
        List<LigneBR> lignes = ligneBRRepository.findByBonReceptionId(brId);
        
        if (lignes.isEmpty()) {
            return;
        }
        
        boolean toutRecu = true;
        boolean avecEcart = false;
        
        for (LigneBR ligne : lignes) {
            if (ligne.getQuantiteRecue().compareTo(ligne.getQuantiteCommandee()) < 0) {
                toutRecu = false;
            }
            if (ligne.getQuantiteNonConforme().compareTo(BigDecimal.ZERO) > 0) {
                avecEcart = true;
            }
        }
        
        if (avecEcart) {
            br.setStatut("AVEC_ECART");
        } else if (toutRecu) {
            br.setStatut("COMPLETE");
        } else {
            br.setStatut("PARTIELLE");
        }
        
        bonReceptionRepository.save(br);
    }
    
    /**
     * Finaliser le bon de réception
     */
    public void finaliserReception(Long brId, String observations, Utilisateur magasinier) {
        BonReception br = trouverParId(brId);
        br.setObservations(observations);
        bonReceptionRepository.save(br);
        
        // Audit
        auditService.logAction(magasinier, "bon_reception", brId, 
                               "FINALIZE", null, br.getStatut(), null);
    }
    
    @Transactional(readOnly = true)
    public BonReception trouverParId(Long id) {
        return bonReceptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de réception non trouvé: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<BonReception> listerTous() {
        return bonReceptionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<BonReception> listerParBC(Long bcId) {
        return bonReceptionRepository.findByBonCommandeId(bcId);
    }
    
    @Transactional(readOnly = true)
    public List<LigneBR> getLignesBR(Long brId) {
        return ligneBRRepository.findByBonReceptionId(brId);
    }
    
    private String genererNumeroBR() {
        return "BR" + System.currentTimeMillis();
    }
}
