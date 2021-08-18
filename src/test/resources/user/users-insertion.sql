delete from users;
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

insert into role (id, name) values (13, 'ROLE_ADMIN');
insert into role (id, name) values (14, 'ROLE_EMPLOYEE');
insert into role (id, name) values (15, 'ROLE_CLIENT');

insert into users (id, user_id, username, email, password) values (16, '1234567890', 'admin', 'admin@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (17, '0192837465', 'employee', 'employee@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (18, '6574832910', 'client', 'client@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (19, '0659483721', 'unused', 'unused@czarbank.org', 'password');

alter sequence hibernate_sequence restart 20;