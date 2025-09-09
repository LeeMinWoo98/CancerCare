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

-- user_profiles 테이블 생성(없으면)
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL PRIMARY KEY,
    cancer_type ENUM('LIVER','STOMACH','COLON','BREAST','CERVICAL','LUNG') NULL,
    stage ENUM('S1','S2','S3A','S3B','S4') NULL,
    height_cm INT NULL,
    weight_kg INT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
        REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
