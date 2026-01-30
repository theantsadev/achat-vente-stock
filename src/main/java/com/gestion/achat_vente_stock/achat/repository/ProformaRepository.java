package com.gestion.achat_vente_stock.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.achat.model.Proforma;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 10: Achats > Pro-forma
 */
@Repository
public interface ProformaRepository extends JpaRepository<Proforma, Long> {
    
    Optional<Proforma> findByNumero(String numero);
    
    List<Proforma> findByDemandeAchatId(Long demandeAchatId);
    
    List<Proforma> findByFournisseurId(Long fournisseurId);
    
    List<Proforma> findByStatut(String statut);
}
