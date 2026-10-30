-- ============================================
-- DATABASE COCKTAIL PROJECT
-- Relational structure for managing cocktails,
-- ingredients, and preparations
-- ============================================

-- Select the database (already created automatically by MYSQL_DATABASE)
USE init;

-- ============================================
-- TABLE: cocktail
-- Contains basic cocktail information
-- ============================================
CREATE TABLE cocktail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  descrizione TEXT,
  tempo_preparazione_minutes INT,
  note TEXT
);

-- ============================================
-- TABLE: ingredients
-- Contains the list of all ingredients
-- available for preparing cocktails
-- ============================================
CREATE TABLE ingredienti (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL UNIQUE
);

-- ============================================
-- TABLE: preparation
-- Join table linking cocktails and ingredients.
-- Each record represents one recipe STEP
-- with quantity, unit of measure, and instruction
-- ============================================
CREATE TABLE preparazione (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cocktail_id BIGINT NOT NULL,
  ingrediente_id BIGINT NOT NULL,
  quantita DECIMAL(8,2),
  unita VARCHAR(30),
  step_order INT NOT NULL,
  istruzione TEXT,
  
  -- Referential integrity constraints
  CONSTRAINT fk_prep_cocktail FOREIGN KEY (cocktail_id) 
    REFERENCES cocktail(id) ON DELETE CASCADE,
  CONSTRAINT fk_prep_ingrediente FOREIGN KEY (ingrediente_id) 
    REFERENCES ingredienti(id) ON DELETE RESTRICT
);

-- ============================================
-- INDEXES to improve performance
-- ============================================
CREATE INDEX idx_prep_cocktail ON preparazione(cocktail_id);
CREATE INDEX idx_prep_ingrediente ON preparazione(ingrediente_id);




-- ============================================
-- DATA: Insert cocktails
-- ============================================
INSERT INTO cocktail (id, nome, descrizione, tempo_preparazione_minutes)
VALUES
(1, 'Last Word', 'Cocktail del Proibizionismo a base di gin e Chartreuse.', 2),
(2, 'Paper Plane', 'Cocktail moderno equilibrato tra amaro e agrumi.', 2),
(3, 'Jungle Bird', 'Unico tiki drink con Campari, nato negli anni 70.', 3),
(4, 'Naked & Famous', 'Twist contemporaneo con mezcal, giallo e amaro.', 2),
(5, 'Corpse Reviver #2', 'Classico rigenerante con gin, Cointreau e assenzio.', 3);



-- ============================================
-- DATA: Insert ingredients
-- Complete list of ingredients used
-- in the database cocktails
-- ============================================
INSERT INTO ingredienti (id, nome)
VALUES
(1, 'Gin'),                      -- Distillato base
(2, 'Chartreuse Verde'),         -- Liquore alle erbe francese
(3, 'Maraschino'),               -- Liquore di ciliegie
(4, 'Succo di Lime'),            -- Agrume fresco
(5, 'Amaro Nonino'),             -- Amaro italiano dolce
(6, 'Bourbon'),                  -- Whiskey americano
(7, 'Succo di Limone'),          -- Agrume fresco
(8, 'Aperol'),                   -- Aperitivo italiano
(9, 'Rum Scuro Giamaicano'),     -- Rum invecchiato
(10, 'Campari'),                 -- Bitter italiano
(11, 'Succo di Ananas'),         -- Frutta tropicale
(12, 'Sciroppo di Zucchero'),    -- Dolcificante
(13, 'Mezcal'),                  -- Distillato messicano affumicato
(14, 'Chartreuse Gialla'),       -- Liquore alle erbe più dolce
(15, 'Cointreau'),               -- Liquore all'arancia
(16, 'Lillet Blanc'),            -- Aperitivo vino bianco
(17, 'Assenzio');                -- Distillato all'anice

-- ============================================
-- DATA: Cocktail preparation
-- Each record is one recipe STEP,
-- ordered by step_order
-- ============================================

-- COCKTAIL #1: Last Word
INSERT INTO preparazione (cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
VALUES
(1, 1, 25, 'ml', 1, 'Versare il gin nello shaker con ghiaccio.'),
(1, 2, 25, 'ml', 2, 'Aggiungere Chartreuse verde.'),
(1, 3, 25, 'ml', 3, 'Aggiungere liquore Maraschino.'),
(1, 7, 25, 'ml', 4, 'Aggiungere succo di limone fresco e shakerare.');

-- COCKTAIL #2: Paper Plane
INSERT INTO preparazione (cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
VALUES
(2, 5, 25, 'ml', 1, 'Versare Amaro Nonino nello shaker.'),
(2, 6, 25, 'ml', 2, 'Aggiungere bourbon.'),
(2, 8, 25, 'ml', 3, 'Aggiungere Aperol.'),
(2, 7, 25, 'ml', 4, 'Aggiungere succo di limone fresco e shakerare.');

-- COCKTAIL #3: Jungle Bird
INSERT INTO preparazione (cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
VALUES
(3, 9, 45, 'ml', 1, 'Versare rum scuro nello shaker.'),
(3, 10, 15, 'ml', 2, 'Aggiungere Campari.'),
(3, 11, 45, 'ml', 3, 'Aggiungere succo di ananas.'),
(3, 7, 15, 'ml', 4, 'Aggiungere succo di limone fresco.'),
(3, 12, 10, 'ml', 5, 'Aggiungere sciroppo di zucchero e shakerare.');

-- COCKTAIL #4: Naked & Famous
INSERT INTO preparazione (cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
VALUES
(4, 13, 22.5, 'ml', 1, 'Versare mezcal nello shaker.'),
(4, 8, 22.5, 'ml', 2, 'Aggiungere Aperol.'),
(4, 14, 22.5, 'ml', 3, 'Aggiungere Chartreuse gialla.'),
(4, 7, 22.5, 'ml', 4, 'Aggiungere succo di limone fresco e shakerare.');

-- COCKTAIL #5: Corpse Reviver #2
INSERT INTO preparazione (cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
VALUES
(5, 1, 25, 'ml', 1, 'Versare gin nello shaker.'),
(5, 15, 25, 'ml', 2, 'Aggiungere Cointreau.'),
(5, 16, 25, 'ml', 3, 'Aggiungere Lillet Blanc.'),
(5, 7, 25, 'ml', 4, 'Aggiungere succo di limone fresco.'),
(5, 17, 2, 'dash', 5, 'Aggiungere un tocco di assenzio e shakerare.');

-- ============================================
-- TABLE: user_favoriti
-- Manages users' favorite cocktails
-- The user ID is extracted from the Keycloak JWT
-- ============================================
CREATE TABLE user_favoriti (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  keycloak_user_id VARCHAR(255) NOT NULL COMMENT 'UUID utente da Keycloak JWT (claim sub)',
  cocktail_id BIGINT NOT NULL,
  
  -- Referential integrity constraints
  CONSTRAINT fk_favoriti_cocktail FOREIGN KEY (cocktail_id) 
    REFERENCES cocktail(id) ON DELETE CASCADE,
  
  -- Prevent duplicates: a user can save the same cocktail only once
  CONSTRAINT uk_user_cocktail UNIQUE (keycloak_user_id, cocktail_id)
);

-- Indexes for performance
CREATE INDEX idx_favoriti_user ON user_favoriti(keycloak_user_id);
CREATE INDEX idx_favoriti_cocktail ON user_favoriti(cocktail_id);

-- ============================================
-- END OF DATABASE SCRIPT
-- ============================================
