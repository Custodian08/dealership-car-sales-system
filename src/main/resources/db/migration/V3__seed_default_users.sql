-- Seed default demo users with {noop} passwords
insert into users(username, password, enabled) values
  ('admin','{noop}admin', true) on conflict (username) do nothing;
insert into users(username, password, enabled) values
  ('emp','{noop}emp', true) on conflict (username) do nothing;
insert into users(username, password, enabled) values
  ('guest','{noop}guest', true) on conflict (username) do nothing;
insert into users(username, password, enabled) values
  ('accountant','{noop}accountant', true) on conflict (username) do nothing;
insert into users(username, password, enabled) values
  ('lister','{noop}lister', true) on conflict (username) do nothing;
insert into users(username, password, enabled) values
  ('seller','{noop}seller', true) on conflict (username) do nothing;

-- Roles
insert into authorities(username, authority) values ('admin','ROLE_ADMIN') on conflict do nothing;
insert into authorities(username, authority) values ('emp','ROLE_EMPLOYEE') on conflict do nothing;
insert into authorities(username, authority) values ('guest','ROLE_GUEST') on conflict do nothing;
insert into authorities(username, authority) values ('accountant','ROLE_ACCOUNTANT') on conflict do nothing;
insert into authorities(username, authority) values ('lister','ROLE_LISTER') on conflict do nothing;
insert into authorities(username, authority) values ('seller','ROLE_SALESPERSON') on conflict do nothing;
