delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id)
values (59, 1000, now(), 54, 55),
       (60, 2500, now(), 55, 56),
       (61, 1500, now(), 56, 57),
       (62, 250, now(), 57, 58);

alter sequence hibernate_sequence restart 63;