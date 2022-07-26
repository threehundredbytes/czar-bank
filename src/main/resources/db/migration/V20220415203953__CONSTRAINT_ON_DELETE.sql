alter table bank_account drop constraint bank_account_owner_id_fkey;
alter table bank_account add constraint bank_account_owner_id_fkey
    foreign key (owner_id) references users(id) on delete cascade;

alter table email_verification_token drop constraint email_verification_token_user_id_fkey;
alter table email_verification_token add constraint email_verification_token_user_id_fkey
    foreign key (user_id) references users(id) on delete cascade;

alter table recovery_code drop constraint recovery_code_user_id_fkey;
alter table recovery_code add constraint recovery_code_user_id_fkey
    foreign key (user_id) references users(id) on delete cascade;

alter table refresh_token_session drop constraint refresh_token_session_user_id_fkey;
alter table refresh_token_session add constraint refresh_token_session_user_id_fkey
    foreign key (user_id) references users(id) on delete cascade;