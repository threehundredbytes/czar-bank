create sequence permission_id_sequence start 1 increment 1;

create table permission(
    id bigint not null primary key,
    name varchar(100) unique
);


create sequence role_id_sequence start 1 increment 1;

create table role(
    id bigint not null primary key,
    name varchar(100) not null unique
);

create table role_permission(
    role_id bigint not null,
    permission_id bigint not null,
    primary key(role_id, permission_id)
);


create sequence user_id_sequence start 1 increment 1;

create table users(
    id bigint not null primary key,
    user_id varchar(10) not null unique,
    username varchar(32) not null unique,
    password varchar(60) not null,
    email varchar(254) not null unique,
    is_email_verified boolean not null,
    is_account_expired boolean not null,
    is_account_locked boolean not null,
    is_credentials_expired boolean not null,
    is_enabled boolean not null
);

create table user_role(
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id)
);

create sequence refresh_token_session_sequence start 1 increment 1;

create table refresh_token_session(
    id bigint not null primary key,
    created_at timestamp,
    is_revoked boolean,
    refresh_token varchar(36) not null unique,
    user_id bigint,
    foreign key (user_id) references users (id)
);


create sequence blacklisted_access_token_id_sequence start 1 increment 1;

create table blacklisted_access_token(
    id bigint not null primary key,
    access_token varchar(4096) not null,
    created_at timestamp
);


create sequence email_verification_token_id_sequence start 1 increment 1;

create table email_verification_token(
    id bigint not null primary key,
    created_at timestamp,
    email_verification_token varchar(36) not null,
    user_id bigint,
    foreign key (user_id) references users (id)
);


create sequence currency_id_sequence start 1 increment 1;

create table currency(
    id bigint not null primary key,
    code varchar(3) not null unique,
    symbol varchar(4) not null unique
);


create sequence exchange_rate_id_sequence start 1 increment 1;

create table exchange_rate(
    id bigint not null primary key,
    exchange_rate numeric(6, 2) not null,
    date date not null,
    currency_id bigint,
    foreign key (currency_id) references currency (id)
);


create sequence bank_account_type_id_sequence start 1 increment 1;

create table bank_account_type(
    id bigint not null primary key,
    name varchar(100) not null unique,
    currency_exchange_commission numeric(6, 6) not null,
    transaction_commission numeric(6, 6) not null
);


create sequence bank_account_id_sequence start 1 increment 1;

create table bank_account(
    id bigint not null primary key,
    number varchar(20) not null unique,
    balance numeric(20, 2) not null,
    is_closed boolean,
    owner_id bigint,
    used_currency_id bigint,
    bank_account_type_id bigint,
    foreign key (owner_id) references users (id),
    foreign key (used_currency_id) references currency (id),
    foreign key (bank_account_type_id) references bank_account_type (id)
);


create sequence transaction_id_sequence start 1 increment 1;

create table transaction(
    id bigint not null primary key,
    amount numeric(20, 2) not null,
    received_amount numeric(20, 2) not null,
    created_at timestamp,
    source_bank_account_id bigint,
    destination_bank_account_id bigint,
    foreign key (source_bank_account_id) references bank_account (id),
    foreign key (destination_bank_account_id) references bank_account (id)
);