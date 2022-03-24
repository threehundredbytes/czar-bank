delete from transaction;

insert into transaction (id, amount, received_amount, destination_bank_account_id, source_bank_account_id, created_at)
values (1, 1000, 1000, 1, 2, now()),
       (2, 2500, 2500, 2, 3, now()),
       (3, 1500, 1500, 3, 4, now()),
       (4, 250, 250, 4, 5, now());

alter sequence transaction_id_sequence restart 5;