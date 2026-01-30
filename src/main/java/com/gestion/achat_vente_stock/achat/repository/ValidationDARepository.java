package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.ValidationDA;

import java.util.List;

/**
 * TODO.YML Lignes 8-9: Achats > Demande Achat > Validation
 */
@Repository
public interface ValidationDARepository extends JpaRepository<ValidationDA, Long> {
    
    List<ValidationDA> findByDemandeAchatId(Long demandeAchatId);
    
    List<ValidationDA> findByValideurId(Long valideurId);
    
    List<ValidationDA> findByDemandeAchatIdAndNiveau(Long demandeAchatId, Integer niveau);
    
    // TODO.YML Ligne 9: Vérifier que le créateur n'est pas le valideur
    @Query("SELECT COUNT(v) > 0 FROM ValidationDA v " +
           "WHERE v.demandeAchat.id = :daId " +
           "AND v.valideur.id = :userId")
    boolean validateurEstCreateur(@Param("daId") Long daId, @Param("userId") Long userId);
}
