delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (15, 1000, now(), 10, 11);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (16, 2500, now(), 11, 12);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (17, 1500, now(), 12, 13);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (18, 250, now(), 13, 14);

alter sequence hibernate_sequence restart 19;