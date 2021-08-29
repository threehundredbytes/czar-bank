delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (31, 1000, now(), 26, 27);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (32, 2500, now(), 27, 28);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (33, 1500, now(), 28, 29);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (34, 250, now(), 29, 30);

alter sequence hibernate_sequence restart 35;