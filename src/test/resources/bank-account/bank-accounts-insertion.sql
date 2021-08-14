delete from bank_account;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission) values (1, 'Czar', 0.01);
insert into bank_account_type (id, name, transaction_commission) values (2, 'Nobleman', 0.02);
insert into bank_account_type (id, name, transaction_commission) values (3, 'Junker', 0.03);
insert into bank_account_type (id, name, transaction_commission) values (4, 'Standard', 0.04);
insert into bank_account_type (id, name, transaction_commission) values (5, 'Unused account type', 0.10);

insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (6, 15000, false, '39903336089073190794', 'Owner #1', 1);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (7, 5000, false, '33390474811219980161', 'Owner #2', 2);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (8, 2000, false, '38040432731497506063', 'Owner #3', 3);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (9, 500, false, '36264421013439107929', 'Owner #4', 4);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (10, 1500, false, '32541935657215432384', 'Owner #5', 4);

alter sequence hibernate_sequence restart 11;