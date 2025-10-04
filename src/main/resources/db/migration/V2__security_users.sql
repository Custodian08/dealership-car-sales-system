-- Spring Security JDBC schema (users, authorities)
create table if not exists users (
  username varchar(255) not null primary key,
  password varchar(255) not null,
  enabled boolean not null
);

create table if not exists authorities (
  username varchar(255) not null,
  authority varchar(255) not null,
  constraint fk_authorities_users foreign key (username) references users(username)
);

create unique index if not exists ix_auth_username_authority on authorities(username, authority);
