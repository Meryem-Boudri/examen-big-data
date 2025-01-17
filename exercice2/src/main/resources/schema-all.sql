DROP TABLE IF EXISTS sales_report;
CREATE TABLE sales_report (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              product VARCHAR(255),
                              category VARCHAR(255),
                              revenue DOUBLE,
                              quantity_sold INT
);
CREATE TABLE category_sales (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                category VARCHAR(255) NOT NULL,
                                total_revenue DECIMAL(10, 2) NOT NULL
);

-- Table pour le chiffre d'affaires moyen par produit dans chaque catégorie
CREATE TABLE category_product_avg_revenue (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- ID unique
                                              category VARCHAR(255) NOT NULL,        -- Nom de la catégorie
                                              product VARCHAR(255) NOT NULL,         -- Nom du produit
                                              avg_revenue DECIMAL NOT NULL            -- Chiffre d'affaires moyen pour ce produit dans cette catégorie
);