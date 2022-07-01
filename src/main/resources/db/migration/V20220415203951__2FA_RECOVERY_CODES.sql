create sequence recovery_code_sequence start 1 increment 1;

create table recovery_code(
    id bigint not null primary key,
    user_id bigint not null,
    code varchar(19) not null,
    is_used boolean not null,
    foreign key (user_id) references users (id)
);