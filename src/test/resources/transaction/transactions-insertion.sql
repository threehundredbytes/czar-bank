delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (30, 1000, now(), 25, 26);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (31, 2500, now(), 26, 27);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (32, 1500, now(), 27, 28);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (33, 250, now(), 28, 29);

alter sequence hibernate_sequence restart 34;