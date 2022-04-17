alter table users add column two_factor_authentication_secret_key varchar(32);
alter table users add column is_two_factor_authentication_enabled boolean;

update users set is_two_factor_authentication_enabled = false where id between 1 and 3;

alter table users alter column is_two_factor_authentication_enabled SET not null;
