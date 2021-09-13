delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id)
values (51, 1000, now(), 46, 47),
       (52, 2500, now(), 47, 48),
       (53, 1500, now(), 48, 49),
       (54, 250, now(), 49, 50);

alter sequence hibernate_sequence restart 55;