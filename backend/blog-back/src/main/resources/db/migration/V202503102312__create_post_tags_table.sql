create table if not exists post_tags
(
    post_id uuid references posts (id),
    tag_id  uuid references tags (id),
    constraint post_tags_pk primary key (post_id, tag_id)
);