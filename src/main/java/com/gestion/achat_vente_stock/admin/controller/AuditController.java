package com.gestion.achat_vente_stock.admin.controller;

import com.gestion.achat_vente_stock.admin.model.AuditLog;
import com.gestion.achat_vente_stock.admin.repository.AuditLogRepository;
import com.gestion.achat_vente_stock.admin.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO.YML Ligne 60: Sécurité > Audit
 * Contrôleur pour la consultation des logs d'audit
 */
@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final UtilisateurService utilisateurService;

    /**
     * Liste des logs d'audit avec filtres
     */
    @GetMapping
    public String liste(Model model,
                       @RequestParam(required = false) Long utilisateurId,
                       @RequestParam(required = false) String tableName,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        
        List<AuditLog> logs;
        
        if (utilisateurId != null) {
            logs = auditLogRepository.findByUtilisateurId(utilisateurId);
        } else if (tableName != null && !tableName.isBlank()) {
            logs = auditLogRepository.findByTableName(tableName);
        } else if (dateDebut != null && dateFin != null) {
            LocalDateTime debut = dateDebut.atStartOfDay();
            LocalDateTime fin = dateFin.atTime(23, 59, 59);
            logs = auditLogRepository.findByCreatedAtBetween(debut, fin);
        } else {
            // Par défaut, les 100 derniers logs
            logs = auditLogRepository.findTop100ByOrderByCreatedAtDesc();
        }
        
        // Filtrer par action si spécifiée
        if (action != null && !action.isBlank()) {
            logs = logs.stream()
                    .filter(l -> action.equals(l.getAction()))
                    .toList();
        }
        
        model.addAttribute("logs", logs);
        model.addAttribute("utilisateurs", utilisateurService.listerTous());
        model.addAttribute("tables", auditLogRepository.findDistinctTableNames());
        model.addAttribute("actions", List.of("CREATE", "UPDATE", "DELETE", "VALIDATE", "CANCEL", "APPROVE", "REJECT"));
        model.addAttribute("utilisateurId", utilisateurId);
        model.addAttribute("tableName", tableName);
        model.addAttribute("action", action);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        
        return "admin/audit/liste";
    }

    /**
     * Détail d'un log d'audit
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log non trouvé: " + id));
        
        model.addAttribute("log", log);
        
        return "admin/audit/detail";
    }

    /**
     * Historique d'un enregistrement spécifique
     */
    @GetMapping("/historique/{tableName}/{recordId}")
    public String historique(@PathVariable String tableName, 
                            @PathVariable Long recordId, 
                            Model model) {
        List<AuditLog> logs = auditLogRepository.findByTableNameAndRecordId(tableName, recordId);
        
        model.addAttribute("logs", logs);
        model.addAttribute("tableName", tableName);
        model.addAttribute("recordId", recordId);
        
        return "admin/audit/historique";
    }
}
