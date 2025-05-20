create table if not exists users
(
    id         uuid default gen_random_uuid() primary key,
    email      varchar(64) unique not null,
    password   varchar            not null,
    name       varchar(64)        not null,
    created_at timestamptz        not null
);