BEGIN;

-- =========================
-- Donnees Module 6 : Administration / Securite
-- =========================

-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO role (code, libelle, description) VALUES
('ROLE-DEMANDEUR', 'Demandeur', 'Peut creer des demandes achat'),
('ROLE-ACHETEUR', 'Acheteur', 'Peut creer et gerer les bons de commande'),
('ROLE-RESP-ACHATS', 'Responsable Achats', 'Valide les BC au-dessus du seuil'),
('ROLE-DAF', 'Directeur Administratif et Financier', 'Valide les aspects financiers et signatures BC'),
('ROLE-MAGASINIER-REC', 'Magasinier Reception', 'Peut recevoir les marchandises'),
('ROLE-MAGASINIER-SORT', 'Magasinier Sortie', 'Peut preparer et expedier les commandes'),
('ROLE-CHEF-MAGASIN', 'Chef de Magasin', 'Supervise les operations magasin et inventaires'),
('ROLE-COMMERCIAL', 'Commercial', 'Peut creer devis et commandes clients'),
('ROLE-MANAGER-VENTES', 'Manager Ventes', 'Valide les remises exceptionnelles'),
('ROLE-COMPTABLE-FOUR', 'Comptable Fournisseurs', 'Valide les factures fournisseurs (3-way match)'),
('ROLE-COMPTABLE-CLI', 'Comptable Clients', 'Gere la facturation clients et encaissements'),
('ROLE-AUDITEUR', 'Auditeur / Controleur Interne', 'Valide les ajustements stock importants'),
('ROLE-ADMIN', 'Administrateur Systeme', 'Administration complete du systeme');

-- ============================================================
-- PERMISSIONS
-- ============================================================
INSERT INTO permission (code, module, action, description) VALUES
-- Demandes Achat
('PERM-DA-CREATE', 'DEMANDE_ACHAT', 'CREATE', 'Creer une demande achat'),
('PERM-DA-READ', 'DEMANDE_ACHAT', 'READ', 'Consulter les demandes achat'),
('PERM-DA-APPROVE-N1', 'DEMANDE_ACHAT', 'APPROVE_N1', 'Approuver DA niveau 1 (manager)'),
('PERM-DA-APPROVE-N2', 'DEMANDE_ACHAT', 'APPROVE_N2', 'Approuver DA niveau 2 (directeur achats)'),
('PERM-DA-APPROVE-FIN', 'DEMANDE_ACHAT', 'APPROVE_FIN', 'Validation financiere DA'),

-- Bons de Commande
('PERM-BC-CREATE', 'BON_COMMANDE', 'CREATE', 'Creer un bon de commande'),
('PERM-BC-READ', 'BON_COMMANDE', 'READ', 'Consulter les bons de commande'),
('PERM-BC-APPROVE', 'BON_COMMANDE', 'APPROVE', 'Approuver BC au-dessus du seuil'),
('PERM-BC-SIGN', 'BON_COMMANDE', 'SIGN', 'Signer un bon de commande'),

-- Reception
('PERM-REC-CREATE', 'RECEPTION', 'CREATE', 'Creer une reception'),
('PERM-REC-READ', 'RECEPTION', 'READ', 'Consulter les receptions'),

-- Factures Fournisseurs
('PERM-FACT-FOUR-READ', 'FACTURE_FOURNISSEUR', 'READ', 'Consulter les factures fournisseurs'),
('PERM-FACT-FOUR-VALIDATE', 'FACTURE_FOURNISSEUR', 'VALIDATE', 'Valider une facture fournisseur'),

-- Commandes Clients
('PERM-DEVIS-CREATE', 'DEVIS', 'CREATE', 'Creer un devis'),
('PERM-DEVIS-READ', 'DEVIS', 'READ', 'Consulter les devis'),
('PERM-DEVIS-APPROVE', 'DEVIS', 'APPROVE', 'Approuver remise exceptionnelle'),
('PERM-CMD-CLI-CREATE', 'COMMANDE_CLIENT', 'CREATE', 'Creer une commande client'),
('PERM-CMD-CLI-READ', 'COMMANDE_CLIENT', 'READ', 'Consulter les commandes clients'),

-- Preparation et Livraison
('PERM-PREP-CREATE', 'PREPARATION', 'CREATE', 'Creer une preparation'),
('PERM-PREP-READ', 'PREPARATION', 'READ', 'Consulter les preparations'),
('PERM-LIV-CREATE', 'LIVRAISON', 'CREATE', 'Creer une livraison'),
('PERM-LIV-READ', 'LIVRAISON', 'READ', 'Consulter les livraisons'),

-- Factures Clients
('PERM-FACT-CLI-CREATE', 'FACTURE_CLIENT', 'CREATE', 'Creer une facture client'),
('PERM-FACT-CLI-READ', 'FACTURE_CLIENT', 'READ', 'Consulter les factures clients'),

-- Stock et Inventaire
('PERM-STOCK-READ', 'STOCK', 'READ', 'Consulter les stocks'),
('PERM-INV-CREATE', 'INVENTAIRE', 'CREATE', 'Creer un inventaire'),
('PERM-INV-READ', 'INVENTAIRE', 'READ', 'Consulter les inventaires'),
('PERM-INV-VALIDATE', 'INVENTAIRE', 'VALIDATE', 'Valider un ajustement inventaire'),

-- Referentiels
('PERM-REF-ARTICLE-WRITE', 'REFERENTIEL', 'ARTICLE_WRITE', 'Creer/modifier des articles'),
('PERM-REF-READ', 'REFERENTIEL', 'READ', 'Consulter les referentiels'),

-- Rapports
('PERM-REPORT-KPI', 'REPORTING', 'KPI', 'Consulter les KPI'),
('PERM-REPORT-STOCK', 'REPORTING', 'STOCK', 'Rapports stock'),
('PERM-REPORT-ACHATS', 'REPORTING', 'ACHATS', 'Rapports achats'),
('PERM-REPORT-VENTES', 'REPORTING', 'VENTES', 'Rapports ventes'),

-- Administration
('PERM-ADMIN-FULL', 'ADMINISTRATION', 'FULL', 'Administration complete');

-- ============================================================
-- SERVICES
-- ============================================================
INSERT INTO service (code, libelle, parent_id) VALUES
('SRV-DIR', 'Direction Generale', NULL),
('SRV-ACHATS', 'Service Achats', NULL),
('SRV-VENTES', 'Service Ventes', NULL),
('SRV-MAGASIN', 'Service Magasin', NULL),
('SRV-COMPTA', 'Service Comptabilite', NULL),
('SRV-MARKETING', 'Service Marketing', NULL),
('SRV-AUDIT', 'Service Audit et Controle', 1),
('SRV-IT', 'Service Informatique', 1);

-- ============================================================
-- UTILISATEURS
-- ============================================================
INSERT INTO utilisateur (login, nom, prenom, email, service_id, site_id, manager_id, actif, last_login) VALUES
-- Direction
('dg', 'Martin', 'Philippe', 'philippe.martin@supplyflow.fr', 1, 1, NULL, TRUE, NOW()),
('daf', 'Dupont', 'Christine', 'christine.dupont@supplyflow.fr', 1, 1, 1, TRUE, NOW()),

-- Service Achats
('dir_achats', 'Bernard', 'Laurent', 'laurent.bernard@supplyflow.fr', 2, 1, 1, TRUE, NOW()),
('paul', 'Mercier', 'Paul', 'paul.mercier@supplyflow.fr', 2, 1, 3, TRUE, NOW()),
('acheteur2', 'Dubois', 'Sophie', 'sophie.dubois@supplyflow.fr', 2, 1, 3, TRUE, NOW()),

-- Service Marketing
('sarah', 'Lefevre', 'Sarah', 'sarah.lefevre@supplyflow.fr', 6, 1, 1, TRUE, NOW()),
('marketing2', 'Rousseau', 'Julie', 'julie.rousseau@supplyflow.fr', 6, 1, 6, TRUE, NOW()),

-- Service Ventes
('mgr_ventes', 'Fournier', 'Marc', 'marc.fournier@supplyflow.fr', 3, 1, 1, TRUE, NOW()),
('clara', 'Lambert', 'Clara', 'clara.lambert@supplyflow.fr', 3, 1, 8, TRUE, NOW()),
('commercial2', 'Blanc', 'Thomas', 'thomas.blanc@supplyflow.fr', 3, 2, 8, TRUE, NOW()),
('commercial3', 'Girard', 'Emilie', 'emilie.girard@supplyflow.fr', 3, 3, 8, TRUE, NOW()),

-- Service Magasin
('jean', 'Moreau', 'Jean', 'jean.moreau@supplyflow.fr', 4, 2, 1, TRUE, NOW()),
('marie', 'Simon', 'Marie', 'marie.simon@supplyflow.fr', 4, 1, 12, TRUE, NOW()),
('luc', 'Laurent', 'Luc', 'luc.laurent@supplyflow.fr', 4, 3, 12, TRUE, NOW()),
('magasinier4', 'Petit', 'Pierre', 'pierre.petit@supplyflow.fr', 4, 1, 12, TRUE, NOW()),
('magasinier5', 'Roux', 'Isabelle', 'isabelle.roux@supplyflow.fr', 4, 2, 12, TRUE, NOW()),

-- Service Comptabilite
('sophie', 'Garnier', 'Sophie', 'sophie.garnier@supplyflow.fr', 5, 1, 2, TRUE, NOW()),
('thomas_compta', 'Bonnet', 'Thomas', 'thomas.bonnet@supplyflow.fr', 5, 1, 2, TRUE, NOW()),

-- Service Audit
('auditeur', 'Fontaine', 'Michel', 'michel.fontaine@supplyflow.fr', 7, 1, 1, TRUE, NOW()),

-- Service IT
('admin', 'Administrateur', 'Systeme', 'admin@supplyflow.fr', 8, 1, 1, TRUE, NOW());

-- Mise a jour du responsable_id des services maintenant que les utilisateurs existent
UPDATE service SET responsable_id = 1 WHERE code = 'SRV-DIR';
UPDATE service SET responsable_id = 3 WHERE code = 'SRV-ACHATS';
UPDATE service SET responsable_id = 8 WHERE code = 'SRV-VENTES';
UPDATE service SET responsable_id = 12 WHERE code = 'SRV-MAGASIN';
UPDATE service SET responsable_id = 2 WHERE code = 'SRV-COMPTA';
UPDATE service SET responsable_id = 6 WHERE code = 'SRV-MARKETING';
UPDATE service SET responsable_id = 19 WHERE code = 'SRV-AUDIT';
UPDATE service SET responsable_id = 20 WHERE code = 'SRV-IT';

-- Mise a jour des responsables depot maintenant que les utilisateurs existent
UPDATE depot SET responsable_id = 13 WHERE code = 'DEP-A1';
UPDATE depot SET responsable_id = 13 WHERE code = 'DEP-A2';
UPDATE depot SET responsable_id = 12 WHERE code = 'DEP-B1';
UPDATE depot SET responsable_id = 14 WHERE code = 'DEP-C1';
UPDATE depot SET responsable_id = 15 WHERE code = 'DEP-D1';
UPDATE depot SET responsable_id = 15 WHERE code = 'DEP-D2';
UPDATE depot SET responsable_id = 16 WHERE code = 'DEP-D3';
UPDATE depot SET responsable_id = 13 WHERE code = 'DEP-QUAR';

-- ============================================================
-- AFFECTATION ROLES AUX UTILISATEURS
-- ============================================================
INSERT INTO utilisateur_role (utilisateur_id, role_id, depot_id, site_id, montant_max, date_debut, date_fin) VALUES
-- Direction Generale - DG
(1, 13, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),

-- Direction Generale - DAF
(2, 4, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),

-- Directeur Achats
(3, 3, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),
(3, 1, NULL, 1, 200000.00, '2026-01-01 00:00:00', NULL),

-- Acheteur Paul
(4, 2, NULL, 1, 100000.00, '2026-01-01 00:00:00', NULL),
(4, 1, NULL, 1, 50000.00, '2026-01-01 00:00:00', NULL),

-- Acheteur 2
(5, 2, NULL, 1, 100000.00, '2026-01-01 00:00:00', NULL),
(5, 1, NULL, 1, 50000.00, '2026-01-01 00:00:00', NULL),

-- Marketing Sarah (Demandeur)
(6, 1, NULL, 1, 30000.00, '2026-01-01 00:00:00', NULL),

-- Marketing 2 (Demandeur)
(7, 1, NULL, 1, 20000.00, '2026-01-01 00:00:00', NULL),

-- Manager Ventes
(8, 9, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),
(8, 8, NULL, 1, NULL, '2026-01-01 00:00:00', NULL),

-- Clara (Commercial)
(9, 8, NULL, 1, NULL, '2026-01-01 00:00:00', NULL),

-- Commercial 2 (Site B)
(10, 8, NULL, 2, NULL, '2026-01-01 00:00:00', NULL),

-- Commercial 3 (Site C)
(11, 8, NULL, 3, NULL, '2026-01-01 00:00:00', NULL),

-- Jean (Chef Magasin Site B)
(12, 7, 3, 2, NULL, '2026-01-01 00:00:00', NULL),
(12, 5, 3, 2, NULL, '2026-01-01 00:00:00', NULL),
(12, 6, 3, 2, NULL, '2026-01-01 00:00:00', NULL),

-- Marie (Magasinier Reception Site A)
(13, 5, 1, 1, NULL, '2026-01-01 00:00:00', NULL),
(13, 5, 2, 1, NULL, '2026-01-01 00:00:00', NULL),

-- Luc (Magasinier Sortie Site C)
(14, 6, 4, 3, NULL, '2026-01-01 00:00:00', NULL),

-- Magasinier 4 (Reception Site A)
(15, 5, 1, 1, NULL, '2026-01-01 00:00:00', NULL),

-- Magasinier 5 (Reception Site B)
(16, 5, 3, 2, NULL, '2026-01-01 00:00:00', NULL),

-- Sophie (Comptable Fournisseurs)
(17, 10, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),

-- Thomas (Comptable Clients)
(18, 11, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),

-- Auditeur
(19, 12, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL),

-- Admin
(20, 13, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL);

-- ============================================================
-- AFFECTATION PERMISSIONS AUX ROLES
-- ============================================================
INSERT INTO role_permission (role_id, permission_id) VALUES
-- ROLE-DEMANDEUR
(1, 1), -- CREATE DA
(1, 2), -- READ DA
(1, 7), -- READ BC
(1, 29), -- READ REF

-- ROLE-ACHETEUR
(2, 1), -- CREATE DA
(2, 2), -- READ DA
(2, 3), -- APPROVE DA N1
(2, 6), -- CREATE BC
(2, 7), -- READ BC
(2, 11), -- READ RECEPTION
(2, 12), -- READ FACTURE FOUR
(2, 29), -- READ REF
(2, 31), -- REPORT ACHATS

-- ROLE-RESP-ACHATS
(3, 2), -- READ DA
(3, 4), -- APPROVE DA N2
(3, 7), -- READ BC
(3, 8), -- APPROVE BC
(3, 11), -- READ RECEPTION
(3, 12), -- READ FACTURE FOUR
(3, 29), -- READ REF
(3, 30), -- REPORT KPI
(3, 31), -- REPORT ACHATS

-- ROLE-DAF
(4, 2), -- READ DA
(4, 5), -- APPROVE FIN DA
(4, 7), -- READ BC
(4, 9), -- SIGN BC
(4, 12), -- READ FACTURE FOUR
(4, 27), -- VALIDATE INVENTAIRE
(4, 29), -- READ REF
(4, 30), -- REPORT KPI
(4, 31), -- REPORT ACHATS
(4, 32), -- REPORT VENTES

-- ROLE-MAGASINIER-REC
(5, 10), -- CREATE RECEPTION
(5, 11), -- READ RECEPTION
(5, 25), -- READ STOCK
(5, 29), -- READ REF

-- ROLE-MAGASINIER-SORT
(6, 18), -- READ CMD CLI
(6, 19), -- CREATE PREPARATION
(6, 20), -- READ PREPARATION
(6, 21), -- CREATE LIVRAISON
(6, 22), -- READ LIVRAISON
(6, 25), -- READ STOCK
(6, 29), -- READ REF

-- ROLE-CHEF-MAGASIN
(7, 11), -- READ RECEPTION
(7, 22), -- READ LIVRAISON
(7, 25), -- READ STOCK
(7, 26), -- CREATE INVENTAIRE
(7, 27), -- READ INVENTAIRE
(7, 29), -- READ REF
(7, 31), -- REPORT STOCK

-- ROLE-COMMERCIAL
(8, 14), -- CREATE DEVIS
(8, 15), -- READ DEVIS
(8, 17), -- CREATE CMD CLI
(8, 18), -- READ CMD CLI
(8, 22), -- READ LIVRAISON
(8, 23), -- READ FACTURE CLI
(8, 25), -- READ STOCK
(8, 29), -- READ REF

-- ROLE-MANAGER-VENTES
(9, 14), -- CREATE DEVIS
(9, 15), -- READ DEVIS
(9, 16), -- APPROVE DEVIS
(9, 17), -- CREATE CMD CLI
(9, 18), -- READ CMD CLI
(9, 22), -- READ LIVRAISON
(9, 23), -- READ FACTURE CLI
(9, 25), -- READ STOCK
(9, 29), -- READ REF
(9, 30), -- REPORT KPI
(9, 32), -- REPORT VENTES

-- ROLE-COMPTABLE-FOUR
(10, 7), -- READ BC
(10, 11), -- READ RECEPTION
(10, 12), -- READ FACTURE FOUR
(10, 13), -- VALIDATE FACTURE FOUR
(10, 29), -- READ REF

-- ROLE-COMPTABLE-CLI
(11, 18), -- READ CMD CLI
(11, 22), -- READ LIVRAISON
(11, 23), -- READ FACTURE CLI
(11, 24), -- CREATE FACTURE CLI
(11, 29), -- READ REF

-- ROLE-AUDITEUR
(12, 2), -- READ DA
(12, 7), -- READ BC
(12, 11), -- READ RECEPTION
(12, 12), -- READ FACTURE FOUR
(12, 18), -- READ CMD CLI
(12, 23), -- READ FACTURE CLI
(12, 25), -- READ STOCK
(12, 26), -- READ INVENTAIRE
(12, 27), -- VALIDATE INVENTAIRE
(12, 29), -- READ REF
(12, 30), -- REPORT KPI
(12, 31), -- REPORT ACHATS
(12, 32), -- REPORT VENTES

-- ROLE-ADMIN
(13, 33); -- ADMIN FULL

-- ============================================================
-- EXEMPLES DE DELEGATIONS
-- ============================================================
INSERT INTO delegation (delegant_id, delegataire_id, role_id, date_debut, date_fin, justification, statut) VALUES
-- Paul delegue a Acheteur2 pendant ses conges
(4, 5, 2, '2026-08-01 00:00:00', '2026-08-31 23:59:59', 'Conges ete Paul', 'ACTIVE'),

-- Manager Ventes delegue a Clara pendant deplacement
(8, 9, 9, '2026-02-15 00:00:00', '2026-02-20 23:59:59', 'Deplacement professionnel', 'ACTIVE');

-- ============================================================
-- EXEMPLES D'AUDIT LOG (traces historiques)
-- ============================================================
INSERT INTO audit_log (utilisateur_id, table_name, record_id, action, avant, apres, ip_address, created_at) VALUES
(13, 'reception', 1, 'CREATE', NULL, '{"bc_id":1,"quantite":480,"date":"2026-10-16"}', '192.168.1.50', '2026-10-16 14:30:00'),
(17, 'facture_fournisseur', 1, 'VALIDATE', '{"statut":"EN_ATTENTE"}', '{"statut":"VALIDEE","montant":216000.00}', '192.168.1.75', '2026-10-17 09:15:00'),
(9, 'commande_client', 1, 'CREATE', NULL, '{"client_id":1,"montant_total":60000.00}', '192.168.1.60', '2026-10-18 11:00:00'),
(14, 'livraison', 1, 'CREATE', NULL, '{"commande_id":1,"quantite":100}', '192.168.1.55', '2026-10-19 10:30:00'),
(12, 'inventaire', 1, 'CREATE', NULL, '{"depot_id":3,"date":"2026-11-15"}', '192.168.1.52', '2026-11-15 08:00:00'),
(19, 'ajustement_stock', 1, 'VALIDATE', '{"statut":"EN_ATTENTE"}', '{"statut":"VALIDE","ecart":-5,"valeur":2250.00}', '192.168.1.80', '2026-11-15 16:45:00');

COMMIT;
