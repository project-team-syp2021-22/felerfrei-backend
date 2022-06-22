BEGIN;

create table role
(
    id   integer not null
        primary key,
    name varchar(256)
);

alter table role
    owner to felerfrei_sa;
insert into role (id, name) values (1, 'ROLE_USER');
insert into role (id, name) values (2, 'ROLE_ADMIN');

COMMIT;