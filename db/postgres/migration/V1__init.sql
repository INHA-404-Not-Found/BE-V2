-- **************************************************************************
-- Member

DROP TABLE IF EXISTS member CASCADE;
CREATE TABLE member
(
    member_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id     BIGINT       NOT NULL,
    password       VARCHAR(255) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    department     VARCHAR(255) NOT NULL,
    role           VARCHAR(20) CHECK (role IN ('USER', 'ADMIN')),
    refresh_token  VARCHAR(255),
    refresh_expiry TIMESTAMP
);

-- **************************************************************************
-- Posting

DROP TABLE IF EXISTS location CASCADE;
CREATE TABLE location
(
    location_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL
);

DROP TABLE IF EXISTS post CASCADE;
CREATE TABLE post
(
    post_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id        BIGINT       NOT NULL,
    location_id      BIGINT,
    location_detail  VARCHAR(255),
    title            VARCHAR(255) NOT NULL,
    content          TEXT,
    stored_location  VARCHAR(255),
    status           VARCHAR(20) CHECK (status IN ('UNCOMPLETED', 'COMPLETED', 'POLICE')),
    post_type        VARCHAR(20) CHECK (post_type IN ('LOST', 'FIND', 'NOTICE')),
    is_personal      BOOLEAN DEFAULT FALSE,
    student_id       VARCHAR(10),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location (location_id)
);

DROP TABLE IF EXISTS post_image CASCADE;
CREATE TABLE post_image
(
    post_image_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id             BIGINT NOT NULL,
    original_file_name  VARCHAR(255),
    stored_file_name    VARCHAR(255),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS category CASCADE;
CREATE TABLE category
(
    category_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS post_category CASCADE;
CREATE TABLE post_category
(
    post_category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id           BIGINT NOT NULL,
    category_id       BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS comment CASCADE;
CREATE TABLE comment
(
    comment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT NOT NULL,
    member_id  BIGINT NOT NULL,
    content    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
);

-- **************************************************************************
-- Receiver(수령인)

DROP TABLE IF EXISTS receiver CASCADE;
CREATE TABLE receiver
(
    receiver_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id      BIGINT       NOT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30)  NOT NULL,
    student_id   VARCHAR(10),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE
);

-- **************************************************************************
-- Notification(알림)

DROP TABLE IF EXISTS notification CASCADE;
CREATE TABLE notification
(
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id        BIGINT       NOT NULL,
    title             VARCHAR(255) NOT NULL,
    message           TEXT         NOT NULL,
    link              VARCHAR(255),
    is_read           BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS fcm_token CASCADE;
CREATE TABLE fcm_token
(
    fcm_token_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id    BIGINT       NOT NULL,
    fcm_token    VARCHAR(255) NOT NULL UNIQUE,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
);
