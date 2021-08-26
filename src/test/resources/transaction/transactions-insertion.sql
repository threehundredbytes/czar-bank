delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (32, 1000, now(), 27, 28);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (33, 2500, now(), 28, 29);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (34, 1500, now(), 29, 30);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (35, 250, now(), 30, 31);

alter sequence hibernate_sequence restart 36;