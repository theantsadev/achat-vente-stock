BEGIN;

-- =========================
-- Module 4 : Ventes
-- =========================

CREATE TABLE devis (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  client_id BIGINT,
  commercial_id BIGINT, -- FK added in Module 6
  date_devis DATE,
  date_validite DATE,
  montant_total_ht NUMERIC(18,4),
  remise_globale_pourcent NUMERIC(9,4),
  montant_tva NUMERIC(18,4),
  montant_total_ttc NUMERIC(18,4),
  statut TEXT,
  conditions_commerciales TEXT
);

CREATE TABLE ligne_devis (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  devis_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  remise_pourcent NUMERIC(9,4),
  montant_ligne_ht NUMERIC(18,4)
);

CREATE TABLE commande_client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  devis_id BIGINT,
  client_id BIGINT,
  commercial_id BIGINT, -- FK added in Module 6
  date_commande DATE,
  date_livraison_prevue DATE,
  montant_total_ht NUMERIC(18,4),
  montant_tva NUMERIC(18,4),
  montant_total_ttc NUMERIC(18,4),
  statut TEXT,
  stock_reserve BOOLEAN
);

CREATE TABLE ligne_commande_client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  commande_client_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  remise_pourcent NUMERIC(9,4),
  montant_ligne_ht NUMERIC(18,4),
  quantite_preparee NUMERIC(18,4),
  quantite_livree NUMERIC(18,4)
);

CREATE TABLE bon_livraison (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  commande_client_id BIGINT,
  magasinier_id BIGINT, -- FK added in Module 6
  depot_id BIGINT,
  date_livraison DATE,
  statut TEXT,
  observations TEXT
);

CREATE TABLE ligne_bl (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  bon_livraison_id BIGINT,
  ligne_commande_id BIGINT,
  article_id BIGINT,
  quantite_livree NUMERIC(18,4),
  lot_numero TEXT,
  dluo DATE
);

CREATE TABLE facture_client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  commande_client_id BIGINT,
  bon_livraison_id BIGINT,
  client_id BIGINT,
  date_facture DATE,
  date_echeance DATE,
  montant_ht NUMERIC(18,4),
  montant_tva NUMERIC(18,4),
  montant_ttc NUMERIC(18,4),
  statut TEXT
);

CREATE TABLE ligne_facture_client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  facture_client_id BIGINT,
  article_id BIGINT,
  quantite NUMERIC(18,4),
  prix_unitaire_ht NUMERIC(18,4),
  montant_ligne_ht NUMERIC(18,4)
);

CREATE TABLE avoir_client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  facture_client_id BIGINT,
  client_id BIGINT,
  date_avoir DATE,
  montant_ht NUMERIC(18,4),
  montant_tva NUMERIC(18,4),
  montant_ttc NUMERIC(18,4),
  motif TEXT,
  statut TEXT,
  valide_by BIGINT -- FK added in Module 6
);

CREATE TABLE encaissement (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  facture_client_id BIGINT,
  date_encaissement DATE,
  montant_encaisse NUMERIC(18,4),
  mode_paiement TEXT,
  reference TEXT,
  statut TEXT
);

-- =========================
-- Foreign Keys (to Module 1/3 and internal)
-- =========================

ALTER TABLE ligne_devis
  ADD CONSTRAINT fk_ld_devis
  FOREIGN KEY (devis_id) REFERENCES devis(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_devis
  ADD CONSTRAINT fk_ld_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE devis
  ADD CONSTRAINT fk_devis_client
  FOREIGN KEY (client_id) REFERENCES client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_commande_client
  ADD CONSTRAINT fk_lcc_cc
  FOREIGN KEY (commande_client_id) REFERENCES commande_client(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_commande_client
  ADD CONSTRAINT fk_lcc_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE commande_client
  ADD CONSTRAINT fk_cc_devis
  FOREIGN KEY (devis_id) REFERENCES devis(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE commande_client
  ADD CONSTRAINT fk_cc_client
  FOREIGN KEY (client_id) REFERENCES client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_livraison
  ADD CONSTRAINT fk_bl_cc
  FOREIGN KEY (commande_client_id) REFERENCES commande_client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE bon_livraison
  ADD CONSTRAINT fk_bl_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_bl
  ADD CONSTRAINT fk_lbl_bl
  FOREIGN KEY (bon_livraison_id) REFERENCES bon_livraison(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_bl
  ADD CONSTRAINT fk_lbl_lcc
  FOREIGN KEY (ligne_commande_id) REFERENCES ligne_commande_client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_bl
  ADD CONSTRAINT fk_lbl_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE facture_client
  ADD CONSTRAINT fk_fc_cc
  FOREIGN KEY (commande_client_id) REFERENCES commande_client(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE facture_client
  ADD CONSTRAINT fk_fc_bl
  FOREIGN KEY (bon_livraison_id) REFERENCES bon_livraison(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE facture_client
  ADD CONSTRAINT fk_fc_client
  FOREIGN KEY (client_id) REFERENCES client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_facture_client
  ADD CONSTRAINT fk_lfc_fc
  FOREIGN KEY (facture_client_id) REFERENCES facture_client(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_facture_client
  ADD CONSTRAINT fk_lfc_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE avoir_client
  ADD CONSTRAINT fk_ac_fc
  FOREIGN KEY (facture_client_id) REFERENCES facture_client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE avoir_client
  ADD CONSTRAINT fk_ac_client
  FOREIGN KEY (client_id) REFERENCES client(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE encaissement
  ADD CONSTRAINT fk_enc_fc
  FOREIGN KEY (facture_client_id) REFERENCES facture_client(id) ON UPDATE CASCADE ON DELETE RESTRICT;


-- Ventes
ALTER TABLE bon_livraison
  ADD CONSTRAINT fk_bl_magasinier
  FOREIGN KEY (magasinier_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE commande_client
  ADD CONSTRAINT fk_cc_commercial
  FOREIGN KEY (commercial_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE devis
  ADD CONSTRAINT fk_devis_commercial
  FOREIGN KEY (commercial_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE avoir_client
  ADD CONSTRAINT fk_ac_valide_by
  FOREIGN KEY (valide_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;


COMMIT;
