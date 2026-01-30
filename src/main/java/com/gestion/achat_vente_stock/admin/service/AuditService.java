package com.gestion.achat_vente_stock.admin.service;


import com.gestion.achat_vente_stock.admin.model.AuditLog;
import com.gestion.achat_vente_stock.admin.model.Utilisateur;
import com.gestion.achat_vente_stock.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 60: Sécurité > Audit
 * Journalisation complète non modifiable (qui/quoi/quand)
 */
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Enregistrer une action dans le log d'audit
     */
    @Transactional
    public void logAction(Utilisateur utilisateur, String tableName, Long recordId, 
                         String action, String avant, String apres, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUtilisateur(utilisateur);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setAction(action);
        log.setAvant(avant);
        log.setApres(apres);
        log.setIpAddress(ipAddress);
        log.setCreatedAt(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getHistorique(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getActionsByUser(Long utilisateurId) {
        return auditLogRepository.findByUtilisateurId(utilisateurId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getActionsByPeriod(LocalDateTime debut, LocalDateTime fin) {
        return auditLogRepository.findByCreatedAtBetween(debut, fin);
    }
}
