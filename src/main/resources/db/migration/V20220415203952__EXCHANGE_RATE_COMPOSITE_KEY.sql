drop sequence exchange_rate_id_sequence;

alter table exchange_rate drop column id;
alter table exchange_rate alter column currency_id set not null;
alter table exchange_rate add primary key (date, currency_id);