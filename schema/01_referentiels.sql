BEGIN;

-- =========================
-- Module 1 : Référentiels
-- =========================



CREATE TABLE entite_legale (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  raison_sociale TEXT,
  siret TEXT,
  pays TEXT,
  forme_juridique TEXT
);

CREATE TABLE site (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  ville TEXT,
  pays TEXT,
  entite_legale_id BIGINT
);

CREATE TABLE famille_article (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  parent_id BIGINT,
  tracabilite_lot_defaut BOOLEAN,
  methode_valorisation_defaut TEXT
);

CREATE TABLE article (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  designation TEXT,
  famille_id BIGINT,
  unite_mesure TEXT,
  achetable BOOLEAN,
  vendable BOOLEAN,
  stockable BOOLEAN,
  tracabilite_lot BOOLEAN,
  dluo_obligatoire BOOLEAN,
  methode_valorisation TEXT,
  prix_achat_moyen NUMERIC(18,4),
  prix_vente_public NUMERIC(18,4),
  stock_minimum INTEGER,
  stock_maximum INTEGER,
  statut TEXT,
  created_at TIMESTAMP,
  created_by BIGINT -- FK added in Module 6
);

CREATE TABLE fournisseur (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  raison_sociale TEXT,
  adresse TEXT,
  email TEXT,
  telephone TEXT,
  conditions_paiement TEXT,
  delai_livraison_jours INTEGER,
  statut TEXT,
  motif_blocage TEXT,
  date_blocage TIMESTAMP
);

CREATE TABLE tarif (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  date_debut TIMESTAMP,
  date_fin TIMESTAMP
);

CREATE TABLE tarif_ligne (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  tarif_id BIGINT,
  article_id BIGINT,
  prix_unitaire_ht NUMERIC(18,4),
  remise_pourcent NUMERIC(9,4)
);

CREATE TABLE client (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  raison_sociale TEXT,
  adresse TEXT,
  email TEXT,
  limite_credit NUMERIC(18,4),
  tarif_id BIGINT,
  conditions_paiement TEXT,
  statut TEXT,
  motif_blocage TEXT
);

CREATE TABLE depot (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  adresse TEXT,
  site_id BIGINT,
  responsable_id BIGINT, -- FK added in Module 6
  type TEXT,
  actif BOOLEAN
);

CREATE TABLE taxe (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  taux_pourcent NUMERIC(9,4),
  pays TEXT
);

-- =========================
-- Foreign Keys (within Module 1)
-- =========================

ALTER TABLE site
  ADD CONSTRAINT fk_site_entite_legale
  FOREIGN KEY (entite_legale_id) REFERENCES entite_legale(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE famille_article
  ADD CONSTRAINT fk_famille_article_parent
  FOREIGN KEY (parent_id) REFERENCES famille_article(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE article
  ADD CONSTRAINT fk_article_famille
  FOREIGN KEY (famille_id) REFERENCES famille_article(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE tarif_ligne
  ADD CONSTRAINT fk_tarif_ligne_tarif
  FOREIGN KEY (tarif_id) REFERENCES tarif(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE tarif_ligne
  ADD CONSTRAINT fk_tarif_ligne_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE client
  ADD CONSTRAINT fk_client_tarif
  FOREIGN KEY (tarif_id) REFERENCES tarif(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE depot
  ADD CONSTRAINT fk_depot_site
  FOREIGN KEY (site_id) REFERENCES site(id) ON UPDATE CASCADE ON DELETE RESTRICT;





COMMIT;
