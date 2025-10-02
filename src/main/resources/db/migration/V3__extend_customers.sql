-- V3: extend customers with passport info and address
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS passport_number varchar(64),
ADD COLUMN IF NOT EXISTS passport_issued_by varchar(255),
ADD COLUMN IF NOT EXISTS passport_issue_date date,
ADD COLUMN IF NOT EXISTS address text;
