create table if not exists posts
(
    id           uuid default gen_random_uuid() primary key,
    title        varchar(512) not null,
    author_id    uuid         not null,
    content      text         not null,
    status       varchar(32)  not null,
    category_id  uuid         not null,
    reading_time int          not null,
    created_at   timestamptz  not null,
    updated_at   timestamptz  not null,
    constraint posts_author_id_fk foreign key (author_id) references users (id) on delete cascade,
    constraint posts_category_id_fk foreign key (category_id) references categories (id)
);