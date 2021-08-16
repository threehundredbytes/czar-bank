delete from users;
delete from role;

insert into role (id, name) values (1, 'ADMIN');
insert into role (id, name) values (2, 'EMPLOYEE');
insert into role (id, name) values (3, 'CLIENT');

insert into users (id, user_id, username, email, password) values (4, '1234567890', 'admin', 'admin@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (5, '0192837465', 'employee', 'employee@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (6, '6574832910', 'client', 'client@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (7, '0659483721', 'unused', 'unused@czarbank.org', 'password');

alter sequence hibernate_sequence restart 8;