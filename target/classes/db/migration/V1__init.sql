-- Flyway V1: initial schema for PostgreSQL

create table if not exists app_users (
  id uuid not null primary key,
  username varchar(255) not null unique,
  password varchar(255) not null,
  role varchar(32) not null,
  enabled boolean not null
);

create table if not exists customers (
  id uuid not null primary key,
  first_name varchar(255),
  last_name varchar(255),
  email varchar(255) unique,
  phone varchar(64)
);

create table if not exists vehicles (
  id uuid not null primary key,
  vin varchar(64) not null unique,
  make varchar(255) not null,
  model varchar(255) not null,
  model_year integer,
  status varchar(32) not null,
  price numeric(19,2) not null,
  version integer
);

create table if not exists reservations (
  id uuid not null primary key,
  vehicle_id uuid not null,
  customer_id uuid not null,
  reserved_by_username varchar(255) not null,
  deposit numeric(19,2) not null,
  created_at timestamp,
  expires_at timestamp,
  status varchar(32) not null,
  version integer,
  constraint fk_res_vehicle foreign key (vehicle_id) references vehicles(id),
  constraint fk_res_customer foreign key (customer_id) references customers(id)
);

create index if not exists idx_res_vehicle_status on reservations(vehicle_id, status);

create table if not exists sales (
  id uuid not null primary key,
  vehicle_id uuid not null,
  customer_id uuid not null,
  salesperson_username varchar(255) not null,
  price numeric(19,2),
  sale_date timestamp,
  constraint fk_sale_vehicle foreign key (vehicle_id) references vehicles(id),
  constraint fk_sale_customer foreign key (customer_id) references customers(id)
);
