insert into permission (id, name)
values (1, 'ROLE_CREATE'),
       (2, 'ROLE_READ'),
       (3, 'ROLE_UPDATE'),
       (4, 'ROLE_DELETE');

insert into permission (id, name)
values (5, 'USER_CREATE'),
       (6, 'USER_READ'),
       (7, 'USER_UPDATE'),
       (8, 'USER_DELETE');

insert into permission (id, name)
values (9, 'BANK_ACCOUNT_TYPE_CREATE'),
       (10, 'BANK_ACCOUNT_TYPE_UPDATE'),
       (11, 'BANK_ACCOUNT_TYPE_DELETE');

insert into permission (id, name)
values (12, 'BANK_ACCOUNT_CREATE'),
       (13, 'BANK_ACCOUNT_READ'),
       (14, 'BANK_ACCOUNT_UPDATE'),
       (15, 'BANK_ACCOUNT_DELETE');

insert into permission (id, name)
values (16, 'TRANSACTION_CREATE'),
       (17, 'TRANSACTION_READ');

insert into permission (id, name)
values (18, 'CURRENCY_CREATE');

alter sequence permission_id_sequence restart 19;

insert into role (id, name)
values (1, 'ADMIN'),
       (2, 'EMPLOYEE'),
       (3, 'CLIENT');

alter sequence role_id_sequence restart 4;

insert into role_permission (role_id, permission_id)
values (1, 1), (1, 2), (1, 3), (1, 4),
       (1, 5), (1, 6), (1, 7), (1, 8),
       (1, 9), (1, 10), (1, 11), (1, 12),
       (1, 13), (1, 14), (1, 15), (1, 16),
       (1, 17), (1, 18);

insert into role_permission (role_id, permission_id)
values (2, 6), (2, 12), (2, 13), (2, 14),
       (2, 15), (2, 16), (2, 17);

insert into users (id, user_id, username, email, password, is_email_verified, is_account_expired, is_account_locked, is_credentials_expired, is_enabled)
values (1, '1234567890', 'admin', 'admin@czarbank.org', 'password', true, false, false, false, true),
       (2, '1192837465', 'employee', 'employee@czarbank.org', 'password', true, false, false, false, true),
       (3, '3982938801', 'client', 'client@czarbank.org', 'password', true, false, false, false, true);

alter sequence user_id_sequence restart 4;

insert into user_role (user_id, role_id)
values (1, 1),
       (2, 2),
       (3, 3);

create extension if not exists pgcrypto;

update users set password = crypt(password, gen_salt('bf', 8));