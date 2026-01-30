BEGIN;

-- =========================
-- Module 6 : Administration / Sécurité
-- =========================

CREATE TABLE role (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  description TEXT
);

CREATE TABLE permission (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  module TEXT,
  action TEXT,
  description TEXT
);

CREATE TABLE service (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  libelle TEXT,
  responsable_id BIGINT, -- FK added after utilisateur creation
  parent_id BIGINT
);

CREATE TABLE utilisateur (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  login TEXT NOT NULL UNIQUE,
  nom TEXT,
  prenom TEXT,
  email TEXT,
  service_id BIGINT,
  site_id BIGINT,
  manager_id BIGINT,
  actif BOOLEAN,
  last_login TIMESTAMP
);

CREATE TABLE utilisateur_role (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  utilisateur_id BIGINT,
  role_id BIGINT,
  depot_id BIGINT,
  site_id BIGINT,
  montant_max NUMERIC(18,4),
  date_debut TIMESTAMP,
  date_fin TIMESTAMP
);

CREATE TABLE role_permission (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  role_id BIGINT,
  permission_id BIGINT
);

CREATE TABLE delegation (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  delegant_id BIGINT,
  delegataire_id BIGINT,
  role_id BIGINT,
  date_debut TIMESTAMP,
  date_fin TIMESTAMP,
  justification TEXT,
  statut TEXT
);

CREATE TABLE audit_log (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  utilisateur_id BIGINT,
  table_name TEXT,
  record_id BIGINT,
  action TEXT,
  avant TEXT,
  apres TEXT,
  ip_address TEXT,
  created_at TIMESTAMP
);

-- =========================
-- Foreign Keys (internal + to Module 1)
-- =========================

ALTER TABLE service
  ADD CONSTRAINT fk_service_parent
  FOREIGN KEY (parent_id) REFERENCES service(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE service
  ADD CONSTRAINT fk_service_responsable
  FOREIGN KEY (responsable_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE utilisateur
  ADD CONSTRAINT fk_utilisateur_service
  FOREIGN KEY (service_id) REFERENCES service(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE utilisateur
  ADD CONSTRAINT fk_utilisateur_site
  FOREIGN KEY (site_id) REFERENCES site(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE utilisateur
  ADD CONSTRAINT fk_utilisateur_manager
  FOREIGN KEY (manager_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE utilisateur_role
  ADD CONSTRAINT fk_utilisateur_role_user
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE utilisateur_role
  ADD CONSTRAINT fk_utilisateur_role_role
  FOREIGN KEY (role_id) REFERENCES role(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE utilisateur_role
  ADD CONSTRAINT fk_utilisateur_role_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE utilisateur_role
  ADD CONSTRAINT fk_utilisateur_role_site
  FOREIGN KEY (site_id) REFERENCES site(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE role_permission
  ADD CONSTRAINT fk_role_permission_role
  FOREIGN KEY (role_id) REFERENCES role(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE role_permission
  ADD CONSTRAINT fk_role_permission_permission
  FOREIGN KEY (permission_id) REFERENCES permission(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE delegation
  ADD CONSTRAINT fk_delegation_delegant
  FOREIGN KEY (delegant_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE delegation
  ADD CONSTRAINT fk_delegation_delegataire
  FOREIGN KEY (delegataire_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE delegation
  ADD CONSTRAINT fk_delegation_role
  FOREIGN KEY (role_id) REFERENCES role(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE audit_log
  ADD CONSTRAINT fk_audit_log_user
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

-- =========================
-- Cross-Module Foreign Keys added now that utilisateur/service exist
-- =========================


-- Referentials
ALTER TABLE article
  ADD CONSTRAINT fk_article_created_by
  FOREIGN KEY (created_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE depot
  ADD CONSTRAINT fk_depot_responsable
  FOREIGN KEY (responsable_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;




COMMIT;
