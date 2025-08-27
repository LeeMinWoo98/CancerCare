-- 기존 테이블이 있다면 삭제
DROP TABLE IF EXISTS email_verification_codes;
DROP TABLE IF EXISTS app_users;

-- app_users 테이블 생성
CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(30) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    birthdate DATE NOT NULL,
    gender ENUM('F','M','N') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_login_id (login_id),
    UNIQUE KEY unique_email (email)
) ENGINE=InnoDB;

-- email_verification_codes 테이블 생성
CREATE TABLE email_verification_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    code VARCHAR(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
