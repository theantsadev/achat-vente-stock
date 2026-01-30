BEGIN;

-- =========================
-- Module 5 : Inventaires
-- =========================

CREATE TABLE inventaire (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  depot_id BIGINT,
  type TEXT,
  date_debut DATE,
  date_fin DATE,
  statut TEXT,
  responsable_id BIGINT, -- FK added in Module 6
  bloque_mouvements BOOLEAN
);

CREATE TABLE ligne_inventaire (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  inventaire_id BIGINT,
  article_id BIGINT,
  emplacement TEXT,
  lot_numero TEXT,
  quantite_theorique NUMERIC(18,4),
  quantite_comptee_1 NUMERIC(18,4),
  quantite_comptee_2 NUMERIC(18,4),
  quantite_retenue NUMERIC(18,4),
  ecart_quantite NUMERIC(18,4),
  ecart_valeur NUMERIC(18,4),
  compteur_1_id BIGINT, -- FK added in Module 6
  compteur_2_id BIGINT, -- FK added in Module 6
  comptage_1_at TIMESTAMP,
  comptage_2_at TIMESTAMP
);

CREATE TABLE ajustement_stock (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero TEXT NOT NULL UNIQUE,
  inventaire_id BIGINT,
  article_id BIGINT,
  depot_id BIGINT,
  lot_numero TEXT,
  quantite_avant NUMERIC(18,4),
  quantite_apres NUMERIC(18,4),
  ecart NUMERIC(18,4),
  valeur_ajustement NUMERIC(18,4),
  motif TEXT,
  justification TEXT,
  demande_by BIGINT, -- FK added in Module 6
  valide_by BIGINT,  -- FK added in Module 6
  demande_at TIMESTAMP,
  valide_at TIMESTAMP,
  statut TEXT
);

-- =========================
-- Foreign Keys (to Modules 1/3 and internal)
-- =========================

ALTER TABLE inventaire
  ADD CONSTRAINT fk_inv_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ligne_inventaire
  ADD CONSTRAINT fk_li_inv
  FOREIGN KEY (inventaire_id) REFERENCES inventaire(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ligne_inventaire
  ADD CONSTRAINT fk_li_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ajustement_stock
  ADD CONSTRAINT fk_as_inv
  FOREIGN KEY (inventaire_id) REFERENCES inventaire(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ajustement_stock
  ADD CONSTRAINT fk_as_article
  FOREIGN KEY (article_id) REFERENCES article(id) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ajustement_stock
  ADD CONSTRAINT fk_as_depot
  FOREIGN KEY (depot_id) REFERENCES depot(id) ON UPDATE CASCADE ON DELETE RESTRICT;

-- Inventaires
ALTER TABLE inventaire
  ADD CONSTRAINT fk_inv_responsable
  FOREIGN KEY (responsable_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ligne_inventaire
  ADD CONSTRAINT fk_li_compteur_1
  FOREIGN KEY (compteur_1_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ligne_inventaire
  ADD CONSTRAINT fk_li_compteur_2
  FOREIGN KEY (compteur_2_id) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ajustement_stock
  ADD CONSTRAINT fk_as_demande_by
  FOREIGN KEY (demande_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE ajustement_stock
  ADD CONSTRAINT fk_as_valide_by
  FOREIGN KEY (valide_by) REFERENCES utilisateur(id) ON UPDATE CASCADE ON DELETE SET NULL;



COMMIT;
