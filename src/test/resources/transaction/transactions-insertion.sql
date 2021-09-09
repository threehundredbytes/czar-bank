delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (44, 1000, now(), 39, 40);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (45, 2500, now(), 40, 41);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (46, 1500, now(), 41, 42);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (47, 250, now(), 42, 43);

alter sequence hibernate_sequence restart 48;