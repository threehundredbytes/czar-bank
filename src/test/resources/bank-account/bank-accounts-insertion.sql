delete from bank_account;
delete from exchange_rate;
delete from currency;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission, currency_exchange_commission)
values (21, 'Czar', 0.01, 0.01),
       (22, 'Nobleman', 0.02, 0.02),
       (23, 'Junker', 0.03, 0.03),
       (24, 'Standard', 0.04, 0.04),
       (25, 'Unused account type', 0.10, 0.20);

insert into currency (id, code, symbol)
values (26, 'RUB', '₽'),
       (27, 'USD', '$'),
       (28, 'EUR', '€'),
       (29, 'JPY', '¥');

insert into exchange_rate (id, date, exchange_rate, currency_id)
values (31, '2021-09-01', 73.28, 27),
       (32, '2021-09-02', 73.19, 27),
       (33, '2021-09-03', 72.85, 27),
       (34, '2021-09-04', 72.85, 27),
       (35, '2021-09-05', 72.85, 27),

       (36, '2021-09-01', 86.67, 28),
       (37, '2021-09-02', 86.39, 28),
       (38, '2021-09-03', 86.30, 28),
       (39, '2021-09-04', 86.54, 28),
       (40, '2021-09-05', 86.54, 28),

       (41, '2021-09-01', 0.67, 29),
       (42, '2021-09-02', 0.67, 29),
       (43, '2021-09-03', 0.67, 29),
       (44, '2021-09-04', 0.67, 29),
       (45, '2021-09-05', 0.67, 29);

insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id)
values (46, 15000, false, '39903336089073190794', 19, 26, 21),
       (47, 5000, false, '33390474811219980161', 20, 26, 22),
       (48, 2000, false, '38040432731497506063', 18, 26, 23),
       (49, 500, false, '36264421013439107929', 20, 26, 24),
       (50, 1500, false, '32541935657215432384', 19, 26, 24);

alter sequence hibernate_sequence restart 51;