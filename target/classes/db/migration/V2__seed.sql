-- Flyway V2: seed sample data for PostgreSQL

-- Ensure uuid generation function is available
create extension if not exists pgcrypto;

-- Customers
insert into customers (id, first_name, last_name, email, phone)
values 
  (gen_random_uuid(), 'John', 'Doe', 'john@example.com', '+100000000')
ON CONFLICT (email) DO NOTHING;

-- Vehicles
insert into vehicles (id, vin, make, model, model_year, status, price, version)
values
  (gen_random_uuid(), 'VIN123', 'Toyota', 'Corolla', 2020, 'AVAILABLE', 15000.00, 0),
  (gen_random_uuid(), 'VIN456', 'Ford',   'Focus',   2019, 'AVAILABLE', 12000.00, 0)
ON CONFLICT (vin) DO NOTHING;
