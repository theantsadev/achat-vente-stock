package com.gestion.achat_vente_stock.achat.service;

import com.gestion.achat_vente_stock.achat.model.DemandeAchat;
import com.gestion.achat_vente_stock.achat.model.Proforma;
import com.gestion.achat_vente_stock.achat.repository.ProformaRepository;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.referentiel.repository.FournisseurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 10: Achats > Pro-forma
 * - Service de gestion des pro-formas
 * - Génération depuis DA approuvée
 * - Traçabilité et historique
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProformaService {

    private final ProformaRepository proformaRepository;
    private final FournisseurRepository fournisseurRepository;
    private static final String PREFIXE_NUMERO = "PF";

    /**
     * Générer une pro-forma depuis une DA approuvée
     */
    public Proforma genererDepuisDA(DemandeAchat demandeAchat, Utilisateur createur) {

        // Vérifier que la DA est approuvée
        if (!demandeAchat.getStatut().equals("APPROUVEE")) {
            throw new IllegalArgumentException("La demande d'achat doit être approuvée");
        }

        // Vérifier qu'une pro-forma n'existe pas déjà
        List<Proforma> existantes = proformaRepository.findByDemandeAchatId(demandeAchat.getId());
        if (!existantes.isEmpty()) {
            throw new IllegalArgumentException("Une pro-forma existe déjà pour cette DA");
        }

        Proforma proforma = new Proforma();
        proforma.setNumero(genererNumero());
        proforma.setDemandeAchat(demandeAchat);
        proforma.setCreateur(createur);
        proforma.setDateCreation(LocalDateTime.now());
        proforma.setStatut("BROUILLON");
        System.out.println(proforma.getCreateur().getNom());
        System.out.println(proforma.getDateCreation());

        // Calculer montant estimé depuis la DA
        if (demandeAchat.getMontantEstimeHt() != null) {
            proforma.setMontantTotalHt(demandeAchat.getMontantEstimeHt());
            // Appliquer TVA par défaut (à récupérer de config)
            BigDecimal tva = demandeAchat.getMontantEstimeHt().multiply(new BigDecimal("0.20"));
            proforma.setMontantTva(tva);
            proforma.setMontantTotalTtc(demandeAchat.getMontantEstimeHt().add(tva));
        }

        return proformaRepository.save(proforma);
    }

    /**
     * Enregistrer une pro-forma avec détails fournisseur
     */
    public Proforma enregistrer(Proforma proforma) {
        if (proforma.getId() == null) {
            System.out.println("Création nouvelle pro-forma");
            proforma.setNumero(genererNumero());
            proforma.setDateCreation(LocalDateTime.now());
            proforma.setStatut("BROUILLON");
        }
        return proformaRepository.save(proforma);
    }

    /**
     * Accepter une pro-forma
     */
    public Proforma accepter(Long proformaId, Utilisateur valideur) {
        Proforma proforma = proformaRepository.findById(proformaId)
                .orElseThrow(() -> new IllegalArgumentException("Pro-forma non trouvée"));

        proforma.setStatut("ACCEPTE");
        proforma.setValideAt(LocalDateTime.now());
        proforma.setValideBy(valideur);

        return proformaRepository.save(proforma);
    }

    /**
     * Rejeter une pro-forma
     */
    public Proforma rejeter(Long proformaId, Utilisateur valideur, String motif) {
        Proforma proforma = proformaRepository.findById(proformaId)
                .orElseThrow(() -> new IllegalArgumentException("Pro-forma non trouvée"));

        proforma.setStatut("REJETEE");
        proforma.setValideAt(LocalDateTime.now());
        proforma.setValideBy(valideur);
        proforma.setRemarques(motif);

        return proformaRepository.save(proforma);
    }

    /**
     * Récupérer toutes les pro-formas
     */
    public List<Proforma> listerTous() {
        return proformaRepository.findAll();
    }

    /**
     * Récupérer pro-formas par statut
     */
    public List<Proforma> listerParStatut(String statut) {
        return proformaRepository.findByStatut(statut);
    }

    /**
     * Récupérer pro-forma par ID
     */
    public Proforma obtenirParId(Long id) {
        return proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pro-forma non trouvée"));
    }

    /**
     * Récupérer pro-forma par DA
     */
    public Optional<Proforma> obtenirParDA(DemandeAchat demandeAchat) {
        return proformaRepository.findByDemandeAchatId(demandeAchat.getId()).stream().findFirst();
    }

    /**
     * Générer un numéro unique
     */
    private String genererNumero() {
        long count = proformaRepository.count() + 1;
        return PREFIXE_NUMERO + String.format("%05d", count);
    }
}
