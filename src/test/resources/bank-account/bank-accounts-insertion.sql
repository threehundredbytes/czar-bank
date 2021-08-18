delete from bank_account;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission) values (20, 'Czar', 0.01);
insert into bank_account_type (id, name, transaction_commission) values (21, 'Nobleman', 0.02);
insert into bank_account_type (id, name, transaction_commission) values (22, 'Junker', 0.03);
insert into bank_account_type (id, name, transaction_commission) values (23, 'Standard', 0.04);
insert into bank_account_type (id, name, transaction_commission) values (24, 'Unused account type', 0.10);

insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (25, 15000, false, '39903336089073190794', 'Owner #1', 20);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (26, 5000, false, '33390474811219980161', 'Owner #2', 21);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (27, 2000, false, '38040432731497506063', 'Owner #3', 22);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (28, 500, false, '36264421013439107929', 'Owner #4', 23);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (29, 1500, false, '32541935657215432384', 'Owner #5', 23);

alter sequence hibernate_sequence restart 30;