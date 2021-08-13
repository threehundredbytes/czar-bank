delete from bank_account;

insert into bank_account (id, balance, is_closed, number, owner) values (1, 15000, false, '39903336089073190794', 'Owner #1');
insert into bank_account (id, balance, is_closed, number, owner) values (2, 5000, false, '33390474811219980161', 'Owner #2');
insert into bank_account (id, balance, is_closed, number, owner) values (3, 2000, false, '38040432731497506063', 'Owner #3');
insert into bank_account (id, balance, is_closed, number, owner) values (4, 500, false, '36264421013439107929', 'Owner #4');
insert into bank_account (id, balance, is_closed, number, owner) values (5, 1500, false, '32541935657215432384', 'Owner #5');

alter sequence hibernate_sequence restart 6;