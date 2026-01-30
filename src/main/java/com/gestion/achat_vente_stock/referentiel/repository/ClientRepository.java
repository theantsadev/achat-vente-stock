package com.gestion.achat_vente_stock.referentiel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestion.achat_vente_stock.referentiel.model.Client;

import java.util.List;
import java.util.Optional;

/**
 * TODO.YML Ligne 4: Référentiels > Clients
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByCode(String code);
    
    List<Client> findByStatut(String statut);
}
