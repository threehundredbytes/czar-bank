delete from user_role;
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

insert into role (id, name) values (13, 'ADMIN');
insert into role (id, name) values (14, 'EMPLOYEE');
insert into role (id, name) values (15, 'CLIENT');

insert into role_permission (role_id, permission_id)
values (13, 1), (13, 2), (13, 3), (13, 4),
       (13, 5), (13, 6), (13, 7), (13, 8),
       (13, 9), (13, 10), (13, 11), (13, 12);

insert into role_permission (role_id, permission_id)
values (14, 5), (14, 6), (14, 7), (14, 8),
       (14, 9), (14, 10), (14, 11), (14, 12);

insert into users (id, user_id, username, email, password, is_account_expired, is_account_locked, is_credentials_expired, is_enabled) values
(16, '1234567890', 'admin', 'admin@czarbank.org', 'password', false, false, false, true),
(17, '0192837465', 'employee', 'employee@czarbank.org', 'password', false, false, false, true),
(18, '6574832910', 'client', 'client@czarbank.org', 'password', false, false, false, true),
(19, '6574832910', 'alekseev', 'alekseev@czarbank.org', 'password', false, false, false, true),
(20, '6574832910', 'markov', 'markov@czarbank.org', 'password', false, false, false, true);

insert into user_role (user_id, role_id)
values (16, 13),
       (17, 14),
       (18, 15),
       (19, 15),
       (20, 15);

alter sequence hibernate_sequence restart 22;