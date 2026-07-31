# 임의의 USER 삽입
insert into member (student_id, password, name, email, department, role)
values('12234063', '1234', 'doyeon', 'doyun20445@gmail.com', 'CS', 'USER');

INSERT INTO member (student_id, password, name, email, department, role)
VALUES (12234063, '$2b$10$1Y8VEZ2RqDs355fCwYFX9ezVnFjYcS6oLGnL1pxVq0ebCDATBigGG', 'doyeon', 'doyun20445@gmail.com', 'CS', 'USER');

# 임의의 ADMIN 삽입
insert into member (student_id, password, name, email, department, role)
values('00000000', '1234', 'admin', 'admin@gmail.com', 'none', 'ADMIN');

insert into category (category_name) values ('전자기기');
insert into location (location_name) values ('60주년 기념관');

select * from category;
select * from location;
select * from member;
select * from post;
