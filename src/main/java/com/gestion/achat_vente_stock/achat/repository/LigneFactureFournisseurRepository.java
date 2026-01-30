package com.gestion.achat_vente_stock.achat.repository;

import com.gestion.achat_vente_stock.achat.model.LigneFactureFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO.YML Ligne 17: Achats > Facture Fournisseur > Lignes
 */
@Repository
public interface LigneFactureFournisseurRepository extends JpaRepository<LigneFactureFournisseur, Long> {
    
    List<LigneFactureFournisseur> findByFactureFournisseurId(Long factureFournisseurId);
    
    List<LigneFactureFournisseur> findByArticleId(Long articleId);
}
