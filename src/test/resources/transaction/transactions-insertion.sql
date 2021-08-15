delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (14, 1000, now(), 9, 10);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (15, 2500, now(), 10, 11);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (16, 1500, now(), 11, 12);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (17, 250, now(), 12, 13);

alter sequence hibernate_sequence restart 18;