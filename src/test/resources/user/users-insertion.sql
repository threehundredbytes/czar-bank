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

insert into role (id, name) values (22, 'ADMIN');
insert into role (id, name) values (23, 'EMPLOYEE');
insert into role (id, name) values (24, 'CLIENT');

insert into role_permission (role_id, permission_id)
values (22, 1), (22, 2), (22, 3), (22, 4),
       (22, 5), (22, 6), (22, 7), (22, 8),
       (22, 9), (22, 10), (22, 11), (22, 12),
       (22, 13), (22, 14), (22, 15), (22, 16),
       (22, 17), (22, 18), (22, 19), (22, 20),
       (22, 21);

insert into role_permission (role_id, permission_id)
values (23, 5), (23, 6), (23, 7), (23, 8),
       (23, 9), (23, 10), (23, 11), (23, 12),
       (23, 19);

insert into users (id, user_id, username, email, password, is_email_verified, is_account_expired, is_account_locked, is_credentials_expired, is_enabled) values
(25, '1234567890', 'admin', 'admin@czarbank.org', 'password', true, false, false, false, true),
(26, '0192837465', 'employee', 'employee@czarbank.org', 'password', true, false, false, false, true),
(27, '6574832910', 'client', 'client@czarbank.org', 'password', true, false, false, false, true),
(28, '6574832910', 'alekseev', 'alekseev@czarbank.org', 'password', true, false, false, false, true),
(29, '6574832910', 'markov', 'markov@czarbank.org', 'password', true, false, false, false, true);

insert into user_role (user_id, role_id)
values (25, 22),
       (26, 23),
       (27, 24),
       (28, 24),
       (29, 24);

alter sequence hibernate_sequence restart 30;