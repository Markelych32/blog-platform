create table if not exists categories
(
    id   uuid default gen_random_uuid() primary key,
    name varchar(64) unique not null
);