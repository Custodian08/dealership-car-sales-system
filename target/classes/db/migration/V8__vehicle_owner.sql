-- Add owner_username to vehicles
alter table vehicles add column if not exists owner_username varchar(255);

-- Optional FK to seller_profiles (username)
do $$
begin
  if not exists (
    select 1
    from pg_constraint c
    join pg_class t on t.oid = c.conrelid
    where c.conname = 'fk_vehicle_owner'
      and t.relname = 'vehicles'
  ) then
    alter table vehicles
      add constraint fk_vehicle_owner
      foreign key (owner_username) references seller_profiles(username)
      on delete set null;
  end if;
end $$;

create index if not exists ix_vehicle_owner on vehicles(owner_username);
