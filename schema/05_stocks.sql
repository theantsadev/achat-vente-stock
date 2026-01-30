BEGIN;

-- =========================
-- Module 3 : Stocks
-- =========================

CREATE TABLE mouvement_stock (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  article_id BIGINT,
  depot_id BIGINT,
  type_mouvement TEXT,
  quantite NUMERIC(18,4),
  unite TEXT,
  cout_unitaire NUMERIC(18,6),
  valeur_totale NUMERIC(18,4),
  document_id BIGINT,
  type_document TEXT,
  date_mouvement DATE,
  created_at TIMESTAMP,
  created_by BIGINT, -- FK added in Module 6
  emplacement TEXT,
  lot_numero TEXT,
  dluo DATE
);

CREATE TABLE lot (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  article_id BIGINT,
  fournisseur_id BIGINT,
  date_fabrication DATE,
  dluo DATE,
  dlc DATE,
  statut TEXT,
  motif_blocage TEXT,
  bloque_at TIMESTAMP
);

CREATE TABLE stock_disponible (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  article_id BIGINT,
  depot_id BIGINT,
  emplacement TEXT,
  lot_numero TEXT,
  quantite_physique NUMERIC(18,4),
  quantite_reservee NUMERIC(18,4),
  quantite_disponible NUMERIC(18,4),
  valeur_stock NUMERIC(18,4),
  last_update TIMESTAMP
);

CREATE TABLE reservation_stock (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  article_id BIGINT,
  depot_id BIGINT,
  lot_numero TEXT,
  quantite_reservee NUMERIC(18,4),
  commande_client_id BIGINT, -- FK added in Module 4
  date_reservation DATE,
  date_expiration DATE,
  statut TEXT
);

CREATE TABLE transfert_stock (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  depot_source_id BIGINT,
  depot_destination_id BIGINT,
  demandeur_id BIGINT, -- FK added in Module 6
  date_demande DATE,
  date_expedition DATE,
  date_reception DATE,
  statut TEXT,
  valide_by BIGINT -- FK added in Module 6
);

CREATE TABLE ligne_transfert (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  transfert_id BIGINT,
  article_id BIGINT,
  quantite_demandee NUMERIC(18,4),
  quantite_expedie NUMERIC(18,4),
  quantite_recue NUMERIC(18,4),
  lot_numero TEXT
);

-- =========================
-- Foreign Keys (to Module 1 and internal)
-- =========================

ALTER TABLE mouvement_stock
  ADD CONSTRAINT fk_ms_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE mouvement_stock
  ADD CONSTRAINT fk_ms_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE lot
  ADD CONSTRAINT fk_lot_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE lot
  ADD CONSTRAINT fk_lot_fournisseur
  FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE stock_disponible
  ADD CONSTRAINT fk_sd_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE stock_disponible
  ADD CONSTRAINT fk_sd_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE reservation_stock
  ADD CONSTRAINT fk_rs_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE reservation_stock
  ADD CONSTRAINT fk_rs_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

-- Add cross-module FK once both tables exist
ALTER TABLE reservation_stock
  ADD CONSTRAINT fk_rs_cc
  FOREIGN KEY (commande_client_id) REFERENCES commande_client(id) ON UPDATE CASCADE ON DELETE SET NULL;


ALTER TABLE transfert_stock
  ADD CONSTRAINT fk_ts_depot_source
  FOREIGN KEY (depot_source_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE transfert_stock
  ADD CONSTRAINT fk_ts_depot_destination
  FOREIGN KEY (depot_destination_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_transfert
  ADD CONSTRAINT fk_lt_ts
  FOREIGN KEY (transfert_id) REFERENCES transfert_stock(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_transfert
  ADD CONSTRAINT fk_lt_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;


-- Stocks
ALTER TABLE transfert_stock
  ADD CONSTRAINT fk_ts_demandeur
  FOREIGN KEY (demandeur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE transfert_stock
  ADD CONSTRAINT fk_ts_valide_by
  FOREIGN KEY (valide_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE mouvement_stock
  ADD CONSTRAINT fk_ms_created_by
  FOREIGN KEY (created_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;


COMMIT;
