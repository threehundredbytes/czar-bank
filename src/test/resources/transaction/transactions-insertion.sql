delete from transaction;

insert into transaction (id, amount, datetime, destination_bank_account_id, source_bank_account_id)
values (1, 1000, now(), 1, 2),
       (2, 2500, now(), 2, 3),
       (3, 1500, now(), 3, 4),
       (4, 250, now(), 4, 5);

alter sequence transaction_id_sequence restart 5;