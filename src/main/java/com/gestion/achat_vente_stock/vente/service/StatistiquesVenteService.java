package com.gestion.achat_vente_stock.vente.service;

import com.gestion.achat_vente_stock.vente.model.FactureClient;
import com.gestion.achat_vente_stock.vente.repository.FactureClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour les statistiques de vente (CA, etc.)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiquesVenteService {

    private final FactureClientRepository factureClientRepository;

    /**
     * Calculer le Chiffre d'Affaires Total (toutes factures validées/payées)
     */
    public BigDecimal getChiffreAffairesTotal() {
        List<FactureClient> factures = factureClientRepository.findAll();
        return factures.stream()
                .filter(f -> "VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut()))
                .map(f -> f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculer le CA HT Total
     */
    public BigDecimal getChiffreAffairesHtTotal() {
        List<FactureClient> factures = factureClientRepository.findAll();
        return factures.stream()
                .filter(f -> "VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut()))
                .map(f -> f.getMontantHt() != null ? f.getMontantHt() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculer le CA du mois en cours
     */
    public BigDecimal getChiffreAffairesMoisCourant() {
        LocalDate debutMois = YearMonth.now().atDay(1);
        LocalDate finMois = YearMonth.now().atEndOfMonth();
        
        List<FactureClient> factures = factureClientRepository.findAll();
        return factures.stream()
                .filter(f -> ("VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut())))
                .filter(f -> f.getDateFacture() != null)
                .filter(f -> !f.getDateFacture().isBefore(debutMois) && !f.getDateFacture().isAfter(finMois))
                .map(f -> f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculer le CA de l'année en cours
     */
    public BigDecimal getChiffreAffairesAnneeCourante() {
        int annee = LocalDate.now().getYear();
        LocalDate debutAnnee = LocalDate.of(annee, 1, 1);
        LocalDate finAnnee = LocalDate.of(annee, 12, 31);
        
        List<FactureClient> factures = factureClientRepository.findAll();
        return factures.stream()
                .filter(f -> ("VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut())))
                .filter(f -> f.getDateFacture() != null)
                .filter(f -> !f.getDateFacture().isBefore(debutAnnee) && !f.getDateFacture().isAfter(finAnnee))
                .map(f -> f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculer le CA par mois pour l'année courante
     */
    public Map<String, BigDecimal> getChiffreAffairesParMois() {
        int annee = LocalDate.now().getYear();
        Map<String, BigDecimal> caParMois = new HashMap<>();
        
        // Initialiser tous les mois à 0
        String[] mois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                         "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        for (String m : mois) {
            caParMois.put(m, BigDecimal.ZERO);
        }
        
        List<FactureClient> factures = factureClientRepository.findAll();
        factures.stream()
                .filter(f -> ("VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut())))
                .filter(f -> f.getDateFacture() != null && f.getDateFacture().getYear() == annee)
                .forEach(f -> {
                    int moisIndex = f.getDateFacture().getMonthValue() - 1;
                    String nomMois = mois[moisIndex];
                    BigDecimal montant = f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO;
                    caParMois.merge(nomMois, montant, BigDecimal::add);
                });
        
        return caParMois;
    }

    /**
     * Nombre de factures validées/payées
     */
    public long getNombreFacturesValidees() {
        return factureClientRepository.findAll().stream()
                .filter(f -> "VALIDEE".equals(f.getStatut()) || "PAYEE".equals(f.getStatut()) 
                        || "PARTIELLEMENT_PAYEE".equals(f.getStatut()))
                .count();
    }

    /**
     * Montant total des impayés (factures validées mais pas encore payées)
     */
    public BigDecimal getMontantImpayes() {
        List<FactureClient> factures = factureClientRepository.findAll();
        return factures.stream()
                .filter(f -> "VALIDEE".equals(f.getStatut()) || "PAYEE_PARTIELLEMENT".equals(f.getStatut()))
                .map(f -> {
                    BigDecimal montant = f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO;
                    // Calculer le montant payé à partir des encaissements
                    BigDecimal paye = BigDecimal.ZERO;
                    if (f.getEncaissements() != null) {
                        paye = f.getEncaissements().stream()
                                .map(e -> e.getMontant() != null ? e.getMontant() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                    }
                    return montant.subtract(paye);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
