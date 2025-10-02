-- V4: interactions (calls, meetings, test-drives)
create table if not exists interactions (
  id uuid not null primary key,
  customer_id uuid not null references customers(id),
  vehicle_id uuid references vehicles(id),
  type varchar(32) not null,
  notes text,
  occurred_at timestamp not null,
  employee_username varchar(255) not null
);

create index if not exists idx_interactions_customer on interactions(customer_id, occurred_at desc);
