delete from bank_account;
delete from exchange_rate;
delete from currency;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission, currency_exchange_commission)
values (30, 'Czar', 0.01, 0.01),
       (31, 'Nobleman', 0.02, 0.02),
       (32, 'Junker', 0.03, 0.03),
       (33, 'Standard', 0.04, 0.04),
       (34, 'Unused account type', 0.10, 0.20);

insert into currency (id, code, symbol)
values (35, 'RUB', '₽'),
       (36, 'USD', '$'),
       (37, 'EUR', '€'),
       (38, 'JPY', '¥');

insert into exchange_rate (id, date, exchange_rate, currency_id)
values (39, '2021-09-01', 73.28, 36),
       (40, '2021-09-02', 73.19, 36),
       (41, '2021-09-03', 72.85, 36),
       (42, '2021-09-04', 72.85, 36),
       (43, '2021-09-05', 72.85, 36),

       (44, '2021-09-01', 86.67, 37),
       (45, '2021-09-02', 86.39, 37),
       (46, '2021-09-03', 86.30, 37),
       (47, '2021-09-04', 86.54, 37),
       (48, '2021-09-05', 86.54, 37),

       (49, '2021-09-01', 0.67, 38),
       (50, '2021-09-02', 0.67, 38),
       (51, '2021-09-03', 0.67, 38),
       (52, '2021-09-04', 0.67, 38),
       (53, '2021-09-05', 0.67, 38);

insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id)
values (54, 15000, false, '39903336089073190794', 28, 35, 30),
       (55, 5000, false, '33390474811219980161', 29, 35, 31),
       (56, 2000, false, '38040432731497506063', 27, 35, 32),
       (57, 500, false, '36264421013439107929', 29, 35, 33),
       (58, 1500, false, '32541935657215432384', 28, 35, 33);

alter sequence hibernate_sequence restart 59;