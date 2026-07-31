# **************************************************************************
# Member

DROP TABLE if EXISTS member CASCADE;
CREATE TABLE member
(
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN'),
    refresh_token VARCHAR(255),
    refresh_expiry DATETIME
);

# **************************************************************************
# Posting

DROP TABLE if EXISTS location CASCADE;
CREATE TABLE location
(
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL
);

DROP TABLE if EXISTS post CASCADE;
CREATE TABLE post
(
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    location_id BIGINT,
    location_detail VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    stored_location VARCHAR(255),
    status ENUM('UNCOMPLETED', 'COMPLETED', 'POLICE'),
    post_type ENUM('LOST', 'FIND', 'NOTICE'),
    is_personal BOOLEAN DEFAULT FALSE,
    student_id VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location(location_id)
);

DROP TABLE if EXISTS post_image CASCADE;
CREATE TABLE post_image
(
    post_image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    original_file_name VARCHAR(255),
    stored_file_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
);

DROP TABLE if EXISTS category CASCADE;
CREATE TABLE category
(
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL
);

DROP TABLE if EXISTS post_category CASCADE;
CREATE TABLE post_category
(
    post_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

DROP TABLE if EXISTS comment CASCADE;
CREATE TABLE comment
(
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

# **************************************************************************
# Receiver(수령인)

DROP TABLE if EXISTS receiver CASCADE;
CREATE TABLE receiver
(
    receiver_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    student_id VARCHAR(10),
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
);

# **************************************************************************
# Notification(알림)

DROP TABLE if EXISTS notification CASCADE;
CREATE TABLE notification
(
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

DROP TABLE if EXISTS fcm_token CASCADE;
CREATE TABLE fcm_token
(
    fcm_token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL UNIQUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
)
