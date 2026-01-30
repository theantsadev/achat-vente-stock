BEGIN;

-- =========================
-- Donnees Module 1 : Referentiels
-- =========================

-- ============================================================
-- ENTITES LEGALES
-- ============================================================
INSERT INTO entite_legale (raison_sociale, siret, pays, forme_juridique) VALUES
('SUPPLYFLOW SA', '12345678901234', 'France', 'SA'),
('SUPPLYFLOW LOGISTICS SAS', '98765432109876', 'France', 'SAS'),
('TECHSUPPLY FRANCE', '11122233344455', 'France', 'SARL'),
('FOODIMPORT INTERNATIONAL', '55566677788899', 'France', 'SA'),
('TEXTILE DISTRIBUTION SAS', '99988877766655', 'France', 'SAS');

-- ============================================================
-- SITES
-- ============================================================
INSERT INTO site (code, libelle, ville, pays, entite_legale_id) VALUES
('SITE-A', 'Plateforme logistique principale', 'Paris', 'France', 1),
('SITE-B', 'Entrepot regional Nord', 'Lille', 'France', 1),
('SITE-C', 'Entrepot regional Sud', 'Marseille', 'France', 1),
('SITE-D1', 'Centre distribution Paris Centre', 'Paris', 'France', 1),
('SITE-D2', 'Centre distribution Paris Est', 'Paris', 'France', 1),
('SITE-D3', 'Centre distribution Paris Ouest', 'Paris', 'France', 1);

-- ============================================================
-- FAMILLES ARTICLES
-- ============================================================
INSERT INTO famille_article (code, libelle, parent_id, tracabilite_lot_defaut, methode_valorisation_defaut) VALUES
-- Familles principales
('FAM-ALI', 'Alimentaire', NULL, TRUE, 'FIFO'),
('FAM-ELEC', 'Electronique', NULL, TRUE, 'CUMP'),
('FAM-TEXT', 'Textile', NULL, FALSE, 'CUMP'),

-- Sous-familles Alimentaire
('FAM-ALI-SEC', 'Produits secs', 1, TRUE, 'FIFO'),
('FAM-ALI-CONS', 'Conserves', 1, TRUE, 'FIFO'),
('FAM-ALI-HUILE', 'Huiles', 1, TRUE, 'FIFO'),

-- Sous-familles Electronique
('FAM-ELEC-TEL', 'Telephonie', 2, TRUE, 'CUMP'),
('FAM-ELEC-INFO', 'Informatique', 2, TRUE, 'CUMP'),
('FAM-ELEC-ACC', 'Accessoires', 2, FALSE, 'CUMP'),

-- Sous-familles Textile
('FAM-TEXT-VET', 'Vetements', 3, FALSE, 'CUMP'),
('FAM-TEXT-CHAU', 'Chaussures', 3, FALSE, 'CUMP');

-- ============================================================
-- ARTICLES
-- ============================================================
INSERT INTO article (code, designation, famille_id, unite_mesure, achetable, vendable, stockable, 
                     tracabilite_lot, dluo_obligatoire, methode_valorisation, prix_achat_moyen, 
                     prix_vente_public, stock_minimum, stock_maximum, statut, created_at) VALUES
-- Alimentaire
('ART-RIZ-001', 'Riz Basmati 1kg', 4, 'KG', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 2.50, 4.99, 1000, 10000, 'ACTIF', NOW()),
('ART-RIZ-002', 'Riz Jasmin 1kg', 4, 'KG', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 3.00, 5.99, 800, 8000, 'ACTIF', NOW()),
('ART-HUILE-001', 'Huile Olive 1L', 6, 'L', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 8.50, 15.99, 500, 5000, 'ACTIF', NOW()),
('ART-HUILE-002', 'Huile Tournesol 1L', 6, 'L', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 3.50, 6.99, 600, 6000, 'ACTIF', NOW()),
('ART-SUCRE-001', 'Sucre blanc 1kg', 4, 'KG', TRUE, TRUE, TRUE, TRUE, FALSE, 'FIFO', 1.20, 2.49, 2000, 20000, 'ACTIF', NOW()),
('ART-CONS-001', 'Tomates pelees 400g', 5, 'UNITE', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 1.10, 2.29, 1500, 15000, 'ACTIF', NOW()),
('ART-CONS-002', 'Haricots verts 400g', 5, 'UNITE', TRUE, TRUE, TRUE, TRUE, TRUE, 'FIFO', 1.30, 2.59, 1200, 12000, 'ACTIF', NOW()),

-- Electronique
('ART-TEL-001', 'Samsung Galaxy S26', 7, 'UNITE', TRUE, TRUE, TRUE, TRUE, FALSE, 'CUMP', 450.00, 699.99, 50, 500, 'ACTIF', NOW()),
('ART-TEL-002', 'iPhone 16 Pro', 7, 'UNITE', TRUE, TRUE, TRUE, TRUE, FALSE, 'CUMP', 850.00, 1199.99, 30, 300, 'ACTIF', NOW()),
('ART-TEL-003', 'Samsung Galaxy A55', 7, 'UNITE', TRUE, TRUE, TRUE, TRUE, FALSE, 'CUMP', 280.00, 399.99, 100, 1000, 'ACTIF', NOW()),
('ART-INFO-001', 'Dell Latitude 7450', 8, 'UNITE', TRUE, TRUE, TRUE, TRUE, FALSE, 'CUMP', 750.00, 1099.99, 20, 200, 'ACTIF', NOW()),
('ART-INFO-002', 'MacBook Air M4', 8, 'UNITE', TRUE, TRUE, TRUE, TRUE, FALSE, 'CUMP', 950.00, 1399.99, 15, 150, 'ACTIF', NOW()),
('ART-ACC-001', 'Coque Samsung S26', 9, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 5.00, 19.99, 200, 2000, 'ACTIF', NOW()),
('ART-ACC-002', 'Chargeur USB-C 65W', 9, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 12.00, 29.99, 300, 3000, 'ACTIF', NOW()),
('ART-ACC-003', 'Ecouteurs Bluetooth', 9, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 25.00, 59.99, 150, 1500, 'ACTIF', NOW()),

-- Textile
('ART-VET-001', 'T-shirt coton homme M', 10, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 8.00, 19.99, 500, 5000, 'ACTIF', NOW()),
('ART-VET-002', 'T-shirt coton homme L', 10, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 8.00, 19.99, 500, 5000, 'ACTIF', NOW()),
('ART-VET-003', 'Jean homme taille 42', 10, 'UNITE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 25.00, 59.99, 300, 3000, 'ACTIF', NOW()),
('ART-CHAU-001', 'Baskets running 42', 11, 'PAIRE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 35.00, 79.99, 200, 2000, 'ACTIF', NOW()),
('ART-CHAU-002', 'Baskets running 43', 11, 'PAIRE', TRUE, TRUE, TRUE, FALSE, FALSE, 'CUMP', 35.00, 79.99, 200, 2000, 'ACTIF', NOW());

-- ============================================================
-- FOURNISSEURS
-- ============================================================
INSERT INTO fournisseur (code, raison_sociale, adresse, email, telephone, conditions_paiement, 
                        delai_livraison_jours, statut, motif_blocage, date_blocage) VALUES
('FOUR-001', 'TECHSUPPLY', '15 Rue Innovation, 75001 Paris', 'contact@techsupply.fr', '+33140123456', '30 jours fin de mois', 5, 'ACTIF', NULL, NULL),
('FOUR-002', 'APPLE DISTRIBUTION', '20 Avenue Technologie, 92100 Boulogne', 'pro@apple-distrib.fr', '+33141234567', '45 jours fin de mois', 7, 'ACTIF', NULL, NULL),
('FOUR-003', 'FOODIMPORT INTERNATIONAL', '8 Boulevard Commerce, 13001 Marseille', 'achats@foodimport.fr', '+33491123456', '60 jours fin de mois', 10, 'ACTIF', NULL, NULL),
('FOUR-004', 'TEXTILE DISTRIBUTION', '42 Rue Tisserand, 69001 Lyon', 'ventes@textile-distrib.fr', '+33478123456', '30 jours fin de mois', 8, 'ACTIF', NULL, NULL),
('FOUR-005', 'HUILES DU MONDE', '3 Quai Maritime, 13016 Marseille', 'commandes@huilesdumonde.fr', '+33491234567', '30 jours net', 12, 'ACTIF', NULL, NULL),
('FOUR-006', 'CONSERVERIE DU SUD', '88 Route Nationale, 84000 Avignon', 'contact@conserveriedusud.fr', '+33490123456', '45 jours fin de mois', 15, 'ACTIF', NULL, NULL),
('FOUR-007', 'ACCESSOIRES TECH', '12 Avenue Accessoires, 75015 Paris', 'pro@acc-tech.fr', '+33145123456', '30 jours net', 3, 'ACTIF', NULL, NULL);

-- ============================================================
-- TARIFS
-- ============================================================
INSERT INTO tarif (code, libelle, date_debut, date_fin) VALUES
('TARIF-STD', 'Tarif standard', '2026-01-01 00:00:00', NULL),
('TARIF-PRO', 'Tarif professionnel', '2026-01-01 00:00:00', NULL),
('TARIF-GROS', 'Tarif grossiste', '2026-01-01 00:00:00', NULL),
('TARIF-VIP', 'Tarif VIP', '2026-01-01 00:00:00', NULL);

-- ============================================================
-- LIGNES DE TARIFS
-- ============================================================
INSERT INTO tarif_ligne (tarif_id, article_id, prix_unitaire_ht, remise_pourcent) VALUES
-- Tarif standard (pas de remise)
(1, 1, 4.99, 0.00),
(1, 2, 5.99, 0.00),
(1, 3, 15.99, 0.00),
(1, 8, 699.99, 0.00),
(1, 9, 1199.99, 0.00),

-- Tarif professionnel (5% remise)
(2, 1, 4.99, 5.00),
(2, 2, 5.99, 5.00),
(2, 3, 15.99, 5.00),
(2, 8, 699.99, 5.00),
(2, 9, 1199.99, 5.00),

-- Tarif grossiste (10% remise)
(3, 1, 4.99, 10.00),
(3, 2, 5.99, 10.00),
(3, 3, 15.99, 10.00),
(3, 8, 699.99, 10.00),
(3, 9, 1199.99, 10.00),

-- Tarif VIP (15% remise)
(4, 1, 4.99, 15.00),
(4, 2, 5.99, 15.00),
(4, 3, 15.99, 15.00),
(4, 8, 699.99, 15.00),
(4, 9, 1199.99, 15.00);

-- ============================================================
-- CLIENTS
-- ============================================================
INSERT INTO client (code, raison_sociale, adresse, email, limite_credit, tarif_id, 
                   conditions_paiement, statut, motif_blocage) VALUES
('CLI-001', 'TECHCITY BOUTIQUE', '25 Rue Commerce, 75002 Paris', 'commandes@techcity.fr', 500000.00, 2, '30 jours fin de mois', 'ACTIF', NULL),
('CLI-002', 'SUPERMARCHE PLUS', '100 Avenue Generale, 69002 Lyon', 'achats@supermarcheplus.fr', 1000000.00, 3, '45 jours fin de mois', 'ACTIF', NULL),
('CLI-003', 'BOUTIQUE MODE', '8 Rue Fashion, 06000 Nice', 'contact@boutiquemode.fr', 200000.00, 2, '30 jours net', 'ACTIF', NULL),
('CLI-004', 'ELECTRO DISCOUNT', '45 Boulevard Electro, 59000 Lille', 'ventes@electrodiscount.fr', 750000.00, 3, '60 jours fin de mois', 'ACTIF', NULL),
('CLI-005', 'ALIMENTATION GENERALE', '12 Place Marche, 13001 Marseille', 'commandes@alimgenerale.fr', 300000.00, 2, '30 jours fin de mois', 'ACTIF', NULL),
('CLI-006', 'VIP DISTRIBUTION', '1 Avenue Luxe, 75008 Paris', 'pro@vipdistrib.fr', 2000000.00, 4, '90 jours fin de mois', 'ACTIF', NULL);

-- ============================================================
-- DEPOTS
-- ============================================================
INSERT INTO depot (code, libelle, adresse, site_id, type, actif) VALUES
('DEP-A1', 'Depot principal Site A', 'Zone A, Plateforme Paris', 1, 'PRINCIPAL', TRUE),
('DEP-A2', 'Depot secondaire Site A', 'Zone B, Plateforme Paris', 1, 'SECONDAIRE', TRUE),
('DEP-B1', 'Depot Nord principal', 'Zone principale, Lille', 2, 'PRINCIPAL', TRUE),
('DEP-C1', 'Depot Sud principal', 'Zone principale, Marseille', 3, 'PRINCIPAL', TRUE),
('DEP-D1', 'Depot Paris Centre', 'Centre ville Paris', 4, 'DISTRIBUTION', TRUE),
('DEP-D2', 'Depot Paris Est', 'Est Paris', 5, 'DISTRIBUTION', TRUE),
('DEP-D3', 'Depot Paris Ouest', 'Ouest Paris', 6, 'DISTRIBUTION', TRUE),
('DEP-QUAR', 'Zone quarantaine', 'Zone isolee Site A', 1, 'QUARANTAINE', TRUE);

-- ============================================================
-- TAXES
-- ============================================================
INSERT INTO taxe (code, libelle, taux_pourcent, pays) VALUES
('TVA-FR-20', 'TVA France taux normal', 20.00, 'France'),
('TVA-FR-10', 'TVA France taux intermediaire', 10.00, 'France'),
('TVA-FR-5.5', 'TVA France taux reduit', 5.50, 'France'),
('TVA-FR-2.1', 'TVA France taux super reduit', 2.10, 'France'),
('TVA-FR-0', 'TVA France exoneration', 0.00, 'France');

COMMIT;
