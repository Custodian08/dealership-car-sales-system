-- Flyway migration: create seller_profiles table
CREATE TABLE IF NOT EXISTS seller_profiles (
    username        VARCHAR(100) PRIMARY KEY,
    type            VARCHAR(20) NOT NULL,
    phone           VARCHAR(100),
    email           VARCHAR(200),
    -- PERSON
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    -- COMPANY
    company_name    VARCHAR(200),
    inn             VARCHAR(32),
    kpp             VARCHAR(32),
    address         VARCHAR(500),
    contact_name    VARCHAR(200)
);
