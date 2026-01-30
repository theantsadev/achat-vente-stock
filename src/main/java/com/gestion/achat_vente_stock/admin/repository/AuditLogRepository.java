package com.gestion.achat_vente_stock.admin.repository;

import com.gestion.achat_vente_stock.admin.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 60: Sécurité > Audit
 * Journalisation complète non modifiable
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUtilisateurId(Long utilisateurId);
    
    List<AuditLog> findByTableName(String tableName);
    
    List<AuditLog> findByTableNameAndRecordId(String tableName, Long recordId);
    
    List<AuditLog> findByCreatedAtBetween(LocalDateTime debut, LocalDateTime fin);
}
