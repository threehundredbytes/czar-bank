delete from bank_account;
delete from bank_account_type;

insert into bank_account_type (id, name, transaction_commission) values (4, 'Czar', 0.01);
insert into bank_account_type (id, name, transaction_commission) values (5, 'Nobleman', 0.02);
insert into bank_account_type (id, name, transaction_commission) values (6, 'Junker', 0.03);
insert into bank_account_type (id, name, transaction_commission) values (7, 'Standard', 0.04);
insert into bank_account_type (id, name, transaction_commission) values (8, 'Unused account type', 0.10);

insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (9, 15000, false, '39903336089073190794', 'Owner #1', 4);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (10, 5000, false, '33390474811219980161', 'Owner #2', 5);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (11, 2000, false, '38040432731497506063', 'Owner #3', 6);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (12, 500, false, '36264421013439107929', 'Owner #4', 7);
insert into bank_account (id, balance, is_closed, number, owner, bank_account_type_id) values (13, 1500, false, '32541935657215432384', 'Owner #5', 7);

alter sequence hibernate_sequence restart 14;