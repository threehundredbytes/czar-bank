delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (11, 1000, now(), 6, 7);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (12, 2500, now(), 7, 8);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (13, 1500, now(), 8, 9);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (14, 250, now(), 9, 10);

alter sequence hibernate_sequence restart 15;