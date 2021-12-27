-- do not touch this shit
drop table if exists Order_Product;
drop table if exists Product_Image;
drop table if exists Project_Image;
drop table if exists Project;
drop table if exists image;
drop table if exists Product;
drop table if exists "order";
drop table if exists "user";
drop table if exists Role;

create table Project
(
    id          serial primary key,
    description varchar(1024),
    title       varchar(256),
    isPublished   bit,
    date date
);
create table Role
(
    id   serial primary key,
    name varchar(256)
);
create table "user"
(
    id        serial primary key,
    firstname varchar(128) not null,
    lastname  varchar(128) not null,
    email     varchar(248) not null,
    password  varchar(2048), --hoffentlich verhasht oder so
    enabled   bit not null,
    telephonenumber varchar(30),
    role_id   int not null,
    UNIQUE(email),
    constraint FK_Role_id foreign key (Role_id) references Role (id)
);

create table Image
(
    id   serial primary key,
    path varchar(512) not null
);

create table Product
(
    id          serial primary key,
    name        varchar(254) not null,
    description varchar(1024),
    isPublished   bit,
    price       float
);
create table "order"
(
    id        serial primary key,
    orderdate date not null,
    isOrdered   bit not null,
    order_address varchar(1024),
    user_id   int,
    constraint FK_Order_User foreign key (user_id) references "user" (id)
);

create table Project_Image
(
    image_id   int,
    project_id int,
    constraint PK_Projekt_Image primary key (image_id, project_id),
    constraint FK_Project_id foreign key (project_id) references Project (id),
    constraint FK_Image_id_Project foreign key (image_id) references Image (id)
);

create table Product_Image
(
    image_id   int,
    product_id int,
    constraint PK_Product_Image primary key (image_id, product_id),
    constraint FK_Product_id foreign key (product_id) references Product (id),
    constraint FK_Image_id_Product foreign key (image_id) references Image (id)
);

create table Order_Product
(
    Order_Product_id serial primary key,
    Order_id     int,
    Product_id   int,
    amount       int,
    retail_price float,
    Extrawurscht varchar(1024),
    constraint FK_Order_Product_Product_id foreign key (Product_id) references Product (id),
    constraint FK_Order_Product_Order_id foreign key (Order_id) references "order" (id)
);

