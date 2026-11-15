create table if not exists person
(
    personId varchar(100) primary key,
    firstName varchar(100) not null,
    lastName varchar(100),
    contact varchar(32) not null,
    email varchar(254),
    company varchar(200),
    jobTitle varchar(200),
    notes varchar(4000),
    favorite boolean not null default false,
    createdAt timestamp not null default current_timestamp,
    updatedAt timestamp not null default current_timestamp
);