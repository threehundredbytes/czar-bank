delete from bank_account;
delete from exchange_rate;
delete from currency;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission, currency_exchange_commission)
values (1, 'Czar', 0.01, 0.01),
       (2, 'Nobleman', 0.02, 0.02),
       (3, 'Junker', 0.03, 0.03),
       (4, 'Standard', 0.04, 0.04),
       (5, 'Unused account type', 0.10, 0.20);

alter sequence bank_account_type_id_sequence restart 6;

insert into currency (id, code, symbol)
values (1, 'RUB', '₽'),
       (2, 'USD', '$'),
       (3, 'EUR', '€'),
       (4, 'JPY', '¥');

alter sequence currency_id_sequence restart 5;

insert into exchange_rate (id, date, exchange_rate, currency_id)
values (1, '2021-09-01', 73.28, 2),
       (2, '2021-09-02', 73.19, 2),
       (3, '2021-09-03', 72.85, 2),
       (4, '2021-09-04', 72.85, 2),
       (5, '2021-09-05', 72.85, 2),

       (6, '2021-09-01', 86.67, 3),
       (7, '2021-09-02', 86.39, 3),
       (8, '2021-09-03', 86.30, 3),
       (9, '2021-09-04', 86.54, 3),
       (10, '2021-09-05', 86.54, 3),

       (11, '2021-09-01', 0.67, 4),
       (12, '2021-09-02', 0.67, 4),
       (13, '2021-09-03', 0.67, 4),
       (14, '2021-09-04', 0.67, 4),
       (15, '2021-09-05', 0.67, 4);

alter sequence exchange_rate_id_sequence restart 16;

insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id)
values (1, 15000, false, '39903336089073190794', 4, 1, 1),
       (2, 5000, false, '33390474811219980161', 5, 1, 2),
       (3, 2000, false, '38040432731497506063', 3, 1, 3),
       (4, 500, false, '36264421013439107929', 5, 1, 4),
       (5, 1500, false, '32541935657215432384', 4, 1, 4);

alter sequence bank_account_id_sequence restart 6;