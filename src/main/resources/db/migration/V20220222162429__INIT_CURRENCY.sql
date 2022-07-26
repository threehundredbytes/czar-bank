insert into currency (id, code, symbol)
values (1, 'RUB', '₽'),
       (2, 'USD', '$'),
       (3, 'EUR', '€');

alter sequence currency_id_sequence restart 4;