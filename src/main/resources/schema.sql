-- do not touch this shit
drop table if exists Order_Product;
drop table if exists Product_Image;
drop table if exists Project_Image;
drop table if exists Project;
drop table if exists image;
drop table if exists Product;
drop table if exists "order";
drop table if exists verification_token;
drop table if exists "user";
drop table if exists Role;

create table Project
(
    id          serial primary key,
    description varchar(1024),
    title       varchar(256),
    isPublished bool,
    date        date
);
create table Role
(
    id   serial primary key,
    name varchar(256)
);
create table "user"
(
    id              serial primary key,
    firstname       varchar(128) not null,
    lastname        varchar(128) not null,
    email           varchar(248) not null,
    password        varchar(2048), --hoffentlich verhasht oder so
    enabled         bool         not null,
    telephonenumber varchar(30),
    role_id         int          not null,
    UNIQUE (email),
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
    isPublished bool,
    price       float check ( price > 0 ),
    material    varchar(256)
);
create table "order"
(
    id            serial primary key,
    orderdate     date not null,
    isOrdered     bool not null,
    order_address varchar(1024),
    user_id       int,
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
    Order_id         int,
    Product_id       int,
    amount           int check (amount > 0),
    retail_price     float,
    Extrawurscht     varchar(1024),
    constraint FK_Order_Product_Product_id foreign key (Product_id) references Product (id),
    constraint FK_Order_Product_Order_id foreign key (Order_id) references "order" (id)
);

create or replace function remove_product_from_cart(in order_content_id int, in delAmount int)
    returns boolean
as
$$
begin
    if (select amount - delAmount from Order_Product where Order_Product_id = order_content_id) <= 0 then
        delete from Order_Product where Order_Product_id = order_content_id;
        return true;
    end if;
    update Order_Product set amount = amount - delAmount where Order_Product_id = order_content_id;
    return false;
end;
$$
    language plpgsql;

create or replace function set_order_content_amount(in order_content_id int, in newAmount int)
    returns boolean
as
$$
begin
    if (select o.amount from Order_Product o where o.Order_Product_id = order_content_id) <> newAmount then
        update Order_Product set amount = newAmount where Order_Product_id = order_content_id;
        return true;
    end if;
end;
$$
    language plpgsql;

create or replace function update_product(in productId int, in newName varchar(255), in newDescription varchar(1024),
                                          in newPrice float, in newIsPublished bool, in newMaterial varchar(255))
    returns boolean
as
$$
begin
    update product
    set name        = newName,
        description = newDescription,
        price       = newPrice,
        material    = newMaterial,
        isPublished = newIsPublished
    where id = productId;
    update Order_Product set retail_price = newPrice where Product_id = productId
    and (select isOrdered from "order" where id = Order_id) = false;
    return true;
end;
$$
    language plpgsql;