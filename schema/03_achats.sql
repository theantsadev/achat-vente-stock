BEGIN;

-- Achats (demande d’achat →pro-forma →   approbation → commande → réception → 
-- facture fournisseur→ paiement) 
-- =========================
-- Module 2 : Achats
-- =========================

CREATE TABLE demande_achat (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  demandeur_id BIGINT, -- FK added in Module 6
  service_id BIGINT,   -- FK added in Module 6
  date_besoin DATE,
  justification TEXT,
  montant_estime_ht NUMERIC(18,4),
  urgence TEXT,
  statut TEXT,
  created_at TIMESTAMP
);

CREATE TABLE ligne_da (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  demande_achat_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_estime_ht NUMERIC(18,4),
  commentaire TEXT
);

CREATE TABLE validation_da (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  demande_achat_id BIGINT,
  valideur_id BIGINT, -- FK added in Module 6
  niveau INTEGER,
  decision TEXT,
  commentaire TEXT,
  date_validation TIMESTAMP
);

CREATE TABLE proforma (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  demande_achat_id BIGINT,
  fournisseur_id BIGINT,
  date_demande DATE,
  date_reponse DATE,
  montant_total_ht NUMERIC(18,4),
  delai_livraison_jours INTEGER,
  statut TEXT
);

CREATE TABLE ligne_proforma (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  proforma_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  remise_pourcent NUMERIC(9,4)
);

CREATE TABLE bon_commande (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  demande_achat_id BIGINT,
  fournisseur_id BIGINT,
  acheteur_id BIGINT,        -- FK added in Module 6
  depot_livraison_id BIGINT,
  date_commande DATE,
  date_livraison_prevue DATE,
  montant_total_ht NUMERIC(18,4),
  montant_tva NUMERIC(18,4),
  montant_total_ttc NUMERIC(18,4),
  conditions_paiement TEXT,
  statut TEXT,
  approuve_at TIMESTAMP,
  approuve_by BIGINT         -- FK added in Module 6
);

CREATE TABLE ligne_bc (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  bon_commande_id BIGINT,
  article_id BIGINT,
  reference_fournisseur TEXT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  remise_pourcent NUMERIC(9,4),
  montant_ligne_ht NUMERIC(18,4)
);

CREATE TABLE bon_reception (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  bon_commande_id BIGINT,
  magasinier_id BIGINT, -- FK added in Module 6
  numero_bl_fournisseur TEXT,
  date_bl_fournisseur DATE,
  date_reception DATE,
  observations TEXT,
  statut TEXT
);

CREATE TABLE ligne_br (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  bon_reception_id BIGINT,
  ligne_bc_id BIGINT,
  article_id BIGINT,
  quantite_commandee NUMERIC(18,4),
  quantite_recue NUMERIC(18,4),
  quantite_conforme NUMERIC(18,4),
  quantite_non_conforme NUMERIC(18,4),
  motif_non_conformite TEXT
);

CREATE TABLE facture_fournisseur (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  numero_facture_fournisseur TEXT,
  fournisseur_id BIGINT,
  bon_commande_id BIGINT,
  date_facture DATE,
  date_echeance DATE,
  montant_ht NUMERIC(18,4),
  montant_tva NUMERIC(18,4),
  montant_ttc NUMERIC(18,4),
  statut TEXT,
  three_way_match_ok BOOLEAN,
  ecarts_three_way TEXT,
  validee_at TIMESTAMP,
  validee_by BIGINT -- FK added in Module 6
);

CREATE TABLE ligne_facture_fournisseur (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  facture_fournisseur_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  montant_ligne_ht NUMERIC(18,4)
);

CREATE TABLE paiement_fournisseur (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  facture_fournisseur_id BIGINT,
  date_paiement DATE,
  montant_paye NUMERIC(18,4),
  mode_paiement TEXT,
  reference_virement TEXT,
  statut TEXT
);

-- =========================
-- Foreign Keys (to Module 1 and internal)
-- =========================

ALTER TABLE ligne_da
  ADD CONSTRAINT fk_ligne_da_da
  FOREIGN KEY (demande_achat_id) REFERENCES demande_achat(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_da
  ADD CONSTRAINT fk_ligne_da_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE validation_da
  ADD CONSTRAINT fk_validation_da_da
  FOREIGN KEY (demande_achat_id) REFERENCES demande_achat(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_proforma
  ADD CONSTRAINT fk_ligne_proforma_pf
  FOREIGN KEY (proforma_id) REFERENCES proforma(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_proforma
  ADD CONSTRAINT fk_ligne_proforma_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE proforma
  ADD CONSTRAINT fk_proforma_da
  FOREIGN KEY (demande_achat_id) REFERENCES demande_achat(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE proforma
  ADD CONSTRAINT fk_proforma_fournisseur
  FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_bc
  ADD CONSTRAINT fk_ligne_bc_bc
  FOREIGN KEY (bon_commande_id) REFERENCES bon_commande(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_bc
  ADD CONSTRAINT fk_ligne_bc_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_commande
  ADD CONSTRAINT fk_bc_da
  FOREIGN KEY (demande_achat_id) REFERENCES demande_achat(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE bon_commande
  ADD CONSTRAINT fk_bc_fournisseur
  FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_commande
  ADD CONSTRAINT fk_bc_depot_livraison
  FOREIGN KEY (depot_livraison_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_reception
  ADD CONSTRAINT fk_br_bc
  FOREIGN KEY (bon_commande_id) REFERENCES bon_commande(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_br
  ADD CONSTRAINT fk_ligne_br_br
  FOREIGN KEY (bon_reception_id) REFERENCES bon_reception(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_br
  ADD CONSTRAINT fk_ligne_br_lbc
  FOREIGN KEY (ligne_bc_id) REFERENCES ligne_bc(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_br
  ADD CONSTRAINT fk_ligne_br_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE facture_fournisseur
  ADD CONSTRAINT fk_ff_fournisseur
  FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE facture_fournisseur
  ADD CONSTRAINT fk_ff_bc
  FOREIGN KEY (bon_commande_id) REFERENCES bon_commande(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_facture_fournisseur
  ADD CONSTRAINT fk_lff_ff
  FOREIGN KEY (facture_fournisseur_id) REFERENCES facture_fournisseur(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_facture_fournisseur
  ADD CONSTRAINT fk_lff_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE paiement_fournisseur
  ADD CONSTRAINT fk_pf_ff
  FOREIGN KEY (facture_fournisseur_id) REFERENCES facture_fournisseur(id) ON UPDATE CASCADE ON DELETE RESTRICT;


-- Achats
ALTER TABLE demande_achat
  ADD CONSTRAINT fk_da_demandeur
  FOREIGN KEY (demandeur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE demande_achat
  ADD CONSTRAINT fk_da_service
  FOREIGN KEY (service_id) REFERENCES service(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE validation_da
  ADD CONSTRAINT fk_validation_da_valideur
  FOREIGN KEY (valideur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_commande
  ADD CONSTRAINT fk_bc_acheteur
  FOREIGN KEY (acheteur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE bon_commande
  ADD CONSTRAINT fk_bc_approuve_by
  FOREIGN KEY (approuve_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE bon_reception
  ADD CONSTRAINT fk_br_magasinier
  FOREIGN KEY (magasinier_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE facture_fournisseur
  ADD CONSTRAINT fk_ff_validee_by
  FOREIGN KEY (validee_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;


COMMIT;
