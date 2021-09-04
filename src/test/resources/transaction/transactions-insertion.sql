delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (34, 1000, now(), 29, 30);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (35, 2500, now(), 30, 31);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (36, 1500, now(), 31, 32);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (37, 250, now(), 32, 33);

alter sequence hibernate_sequence restart 38;