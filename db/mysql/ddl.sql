# 컬러명 변경
ALTER TABLE post
CHANGE original_filename original_file_name VARCHAR(255);
ALTER TABLE post
CHANGE stored_filename stored_file_name VARCHAR(255);

# not null 제약 조건 해제
ALTER TABLE post
MODIFY location_id BIGINT;
ALTER TABLE post
MODIFY stored_location VARCHAR(255);

# 세부 발견 장소 추가
ALTER TABLE post
ADD COLUMN location_detail VARCHAR(255) AFTER location_id;

# item 테이블 -> post_category 로 변경
DROP TABLE item;
CREATE TABLE post_category
(
    post_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES post(post_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

ALTER TABLE post DROP FOREIGN KEY post_ibfk_1;
ALTER TABLE post ADD CONSTRAINT post_ibfk_1
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE;

ALTER TABLE post_category DROP FOREIGN KEY post_category_ibfk_1;
ALTER TABLE post_category ADD CONSTRAINT post_category_ibfk_1
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE;

ALTER TABLE post_category DROP FOREIGN KEY post_category_ibfk_2;
ALTER TABLE post_category ADD CONSTRAINT post_category_ibfk_2
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE;

ALTER TABLE comment DROP FOREIGN KEY comment_ibfk_1;
ALTER TABLE comment ADD CONSTRAINT comment_ibfk_1
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE;

ALTER TABLE comment DROP FOREIGN KEY comment_ibfk_2;
ALTER TABLE comment ADD CONSTRAINT comment_ibfk_2
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE;


ALTER TABLE receiver DROP FOREIGN KEY receiver_ibfk_1;
ALTER TABLE receiver ADD CONSTRAINT receiver_ibfk_1
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE;

ALTER TABLE notification DROP FOREIGN KEY notification_ibfk_1;
ALTER TABLE notification ADD CONSTRAINT notification_ibfk_1
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE;

ALTER TABLE receiver ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE receiver ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE post DROP COLUMN original_file_name;
ALTER TABLE post DROP COLUMN stored_file_name;

# 알림을 누르면 이동할 링크 필드 추가
ALTER TABLE notification ADD column link varchar(255) after message;