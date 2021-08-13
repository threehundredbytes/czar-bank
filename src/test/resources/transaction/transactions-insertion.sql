delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (6, 1000, now(), 1, 2);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (7, 2500, now(), 2, 3);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (8, 1500, now(), 3, 4);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (9, 250, now(), 4, 5);

alter sequence hibernate_sequence restart 10;