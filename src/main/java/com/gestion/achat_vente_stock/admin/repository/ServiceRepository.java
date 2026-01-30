package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.Service;    
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TODO.YML Ligne 56: Sécurité > ABAC > Services
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Optional<Service> findByCode(String code);
}
