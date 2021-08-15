delete from users;

insert into users (id, user_id, username, email, password) values (1, '1234567890', 'admin', 'admin@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (2, '0192837465', 'employee', 'employee@czarbank.org', 'password');
insert into users (id, user_id, username, email, password) values (3, '6574832910', 'client', 'client@czarbank.org', 'password');

alter sequence hibernate_sequence restart 4;