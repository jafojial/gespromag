-- Schema initial : categories, utilisateurs, produits, mouvements de stock

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(30) NOT NULL CHECK (role IN ('ADMINISTRATEUR', 'GESTIONNAIRE')),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id                BIGSERIAL PRIMARY KEY,
    sku               VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    category_id       BIGINT NOT NULL REFERENCES categories (id),
    purchase_price    NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (purchase_price >= 0),
    selling_price     NUMERIC(12, 2) NOT NULL CHECK (selling_price > 0),
    quantity          INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    minimum_quantity  INTEGER NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0),
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_products_name ON products (name);

CREATE TABLE stock_movements (
    id                 BIGSERIAL PRIMARY KEY,
    product_id         BIGINT NOT NULL REFERENCES products (id),
    type               VARCHAR(20) NOT NULL CHECK (type IN ('ENTREE', 'SORTIE', 'AJUSTEMENT')),
    quantity           INTEGER NOT NULL CHECK (quantity >= 0),
    previous_quantity  INTEGER NOT NULL CHECK (previous_quantity >= 0),
    new_quantity       INTEGER NOT NULL CHECK (new_quantity >= 0),
    comment            TEXT,
    created_by         BIGINT NOT NULL REFERENCES users (id),
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_product_id ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);
