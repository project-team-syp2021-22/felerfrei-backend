insert into role (id, name) values (1, 'ROLE_USER');
insert into role (id, name) values (2, 'ROLE_ADMIN');

-- password: password
insert into "user" (firstname, lastname, email, password, enabled, telephonenumber, role_id)
values ('Test', 'User', 'user@test.com', '$2a$10$DehSr/P.ahoGRuGfOfnFJulJ59b9jRyZiiICsB86uvdNO3VEFjgp.', true, '+123456789', 1);
insert into "user" (firstname, lastname, email, password, enabled, telephonenumber, role_id)
values ('Admin', 'User', 'admin@test.com', '$2a$10$DehSr/P.ahoGRuGfOfnFJulJ59b9jRyZiiICsB86uvdNO3VEFjgp.', true, '+123456789', 2);

insert into product (id, name, description, ispublished, price)
values (1, 'Sessel', 'Holzsessel aus Holz', true, 10.00);
insert into product (id, name, description, ispublished, price)
values (2, 'Tisch', 'Nicht veröffentlicht', false, 10.00);
insert into product (id, name, description, ispublished, price)
values (3, 'Heisl', 'Gute Stück!', true, 69.00);

insert into project (id, description, title, ispublished, date)
values (1, 'Bei diesem Projekt habe ich die Küche montiert!', 'Küchenmontage', true, '2019-01-01');
insert into project (id, description, title, ispublished, date)
values (2, 'Hier wurde ein Tisch gebaut lol', 'Supa Tisch', false, '2019-01-01');

insert into "order" (id, orderdate, isordered, order_address, user_id)
values (1, '2019-01-01', true, 'Musterstraße 1, 12345 Musterstadt', 1);
insert into "order" (id, orderdate, isordered, order_address, user_id)
values (2, '2019-01-01', false, 'Musterstraße 1, 12345 Musterstadt', 2);

insert into order_product (order_product_id, order_id, product_id, amount, retail_price, extrawurscht)
values (1, 1, 1, 2, 10.00, 'Kaffee');
insert into order_product (order_product_id, order_id, product_id, amount, retail_price, extrawurscht)
values (2, 1, 3, 1, 10.00, null);
insert into order_product (order_product_id, order_id, product_id, amount, retail_price, extrawurscht)
values (3, 2, 3, 1, 10.00, null);
insert into order_product (order_product_id, order_id, product_id, amount, retail_price, extrawurscht)
values (4, 2, 1, 5, 10.00, 'Bitte doppelt so groß!');


