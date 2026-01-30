package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.LigneDA;

import java.util.List;

/**
 * TODO.YML Ligne 7: Achats > Demande Achat > Lignes
 */
@Repository
public interface LigneDARepository extends JpaRepository<LigneDA, Long> {
    
    List<LigneDA> findByDemandeAchatId(Long demandeAchatId);
    
    List<LigneDA> findByArticleId(Long articleId);
}
