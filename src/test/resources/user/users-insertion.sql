delete from user_role;
delete from refresh_token_session;
delete from email_verification_token;
delete from users;
delete from role_permission;
delete from role;
delete from permission;

insert into permission (id, name) values (1, 'BANK_ACCOUNT_TYPE_CREATE');
insert into permission (id, name) values (2, 'BANK_ACCOUNT_TYPE_READ');
insert into permission (id, name) values (3, 'BANK_ACCOUNT_TYPE_UPDATE');
insert into permission (id, name) values (4, 'BANK_ACCOUNT_TYPE_DELETE');

insert into permission (id, name) values (5, 'BANK_ACCOUNT_CREATE');
insert into permission (id, name) values (6, 'BANK_ACCOUNT_READ');
insert into permission (id, name) values (7, 'BANK_ACCOUNT_UPDATE');
insert into permission (id, name) values (8, 'BANK_ACCOUNT_DELETE');

insert into permission (id, name) values (9, 'TRANSACTION_CREATE');
insert into permission (id, name) values (10, 'TRANSACTION_READ');
insert into permission (id, name) values (11, 'TRANSACTION_UPDATE');
insert into permission (id, name) values (12, 'TRANSACTION_DELETE');

insert into permission (id, name) values (13, 'ROLE_CREATE');
insert into permission (id, name) values (14, 'ROLE_READ');
insert into permission (id, name) values (15, 'ROLE_UPDATE');
insert into permission (id, name) values (16, 'ROLE_DELETE');

insert into permission (id, name) values (17, 'USER_CREATE');
insert into permission (id, name) values (18, 'USER_READ');
insert into permission (id, name) values (19, 'USER_UPDATE');
insert into permission (id, name) values (20, 'USER_DELETE');

insert into permission (id, name) values (21, 'CURRENCY_CREATE');

alter sequence permission_id_sequence restart 22;

insert into role (id, name) values (1, 'ADMIN');
insert into role (id, name) values (2, 'EMPLOYEE');
insert into role (id, name) values (3, 'CLIENT');

alter sequence role_id_sequence restart 4;

insert into role_permission (role_id, permission_id)
values (1, 1), (1, 2), (1, 3), (1, 4),
       (1, 5), (1, 6), (1, 7), (1, 8),
       (1, 9), (1, 10), (1, 11), (1, 12),
       (1, 13), (1, 14), (1, 15), (1, 16),
       (1, 17), (1, 18), (1, 19), (1, 20),
       (1, 21);

insert into role_permission (role_id, permission_id)
values (2, 5), (2, 6), (2, 7), (2, 8),
       (2, 9), (2, 10), (2, 11), (2, 12),
       (2, 19);

insert into users (id, user_id, username, email, password, is_email_verified, is_account_expired, is_account_locked, is_credentials_expired, is_enabled) values
(1, '1234567890', 'admin', 'admin@czarbank.org', 'password', true, false, false, false, true),
(2, '1192837465', 'employee', 'employee@czarbank.org', 'password', true, false, false, false, true),
(3, '3982938801', 'client', 'client@czarbank.org', 'password', true, false, false, false, true),
(4, '4895628364', 'alekseev', 'alekseev@czarbank.org', 'password', true, false, false, false, true),
(5, '9255122787', 'markov', 'markov@czarbank.org', 'password', true, false, false, false, true);

alter sequence user_id_sequence restart 6;

insert into user_role (user_id, role_id)
values (1, 1),
       (2, 2),
       (3, 3),
       (4, 3),
       (5, 3);

create extension if not exists pgcrypto;

update users set password = crypt(password, gen_salt('bf', 8));