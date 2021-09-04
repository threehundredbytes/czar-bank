delete from bank_account;
delete from currency;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission) values (21, 'Czar', 0.01);
insert into bank_account_type (id, name, transaction_commission) values (22, 'Nobleman', 0.02);
insert into bank_account_type (id, name, transaction_commission) values (23, 'Junker', 0.03);
insert into bank_account_type (id, name, transaction_commission) values (24, 'Standard', 0.04);
insert into bank_account_type (id, name, transaction_commission) values (25, 'Unused account type', 0.10);

insert into currency (id, code, symbol) values (26, 'RUB', '₽');
insert into currency (id, code, symbol) values (27, 'USD', '$');
insert into currency (id, code, symbol) values (28, 'EUR', '€');

insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id) values (29, 15000, false, '39903336089073190794', 19, 26, 21);
insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id) values (30, 5000, false, '33390474811219980161', 20, 26, 22);
insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id) values (31, 2000, false, '38040432731497506063', 18, 26, 23);
insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id) values (32, 500, false, '36264421013439107929', 20, 26, 24);
insert into bank_account (id, balance, is_closed, number, owner_id, used_currency_id, bank_account_type_id) values (33, 1500, false, '32541935657215432384', 19, 26, 24);

alter sequence hibernate_sequence restart 34;