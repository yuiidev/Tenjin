drop database if exists tenjin_test;
create database tenjin_test;

use tenjin_test;

create table users (
    `id` char(36) not null primary key,
    `username` varchar(16) not null unique,
    `email` varchar(30) not null unique,
    `created_at` timestamp not null,
    `updated_at` timestamp not null
);

create table blog_posts (
    `id` char(36) not null primary key,
    `title` varchar(20) not null,
    `slug` varchar(20) not null,
    `author_id` char(36) not null,
    foreign key (author_id) references users(id)
);

insert into users values('4350c451-a5b0-48b0-bd14-acdddffed260', 'Demozo', 'hello@demozo.net', NOW(), NOW());
insert into users values('0ed72857-62e3-47dc-b2f0-b16d7e857083', 'ShadowParallax', 'lorenzo@demozo.net', NOW(), NOW());
insert into users values('8a6523ea-53ff-44ed-b396-514a957ec31c', 'Equive', 'misc@demozo.net', NOW(), NOW());
insert into users values('880dc0b1-b90a-4c48-accb-819d273a5b31', 'Exodean', 'games@demozo.net', NOW(), NOW());

insert into blog_posts values('b78c95cd-4eba-46b8-92f0-cdc2080e608a', 'Test Title', 'test-title', '4350c451-a5b0-48b0-bd14-acdddffed260');
insert into blog_posts values('fddf428b-276b-4569-b90d-5a12347e6914', 'Test Title 2', 'test-title-2', '4350c451-a5b0-48b0-bd14-acdddffed260');