delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (18, 1000, now(), 13, 14);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (19, 2500, now(), 14, 15);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (20, 1500, now(), 15, 16);
insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id) values (21, 250, now(), 16, 17);

alter sequence hibernate_sequence restart 22;