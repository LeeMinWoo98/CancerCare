-- 기존 테이블이 있다면 삭제
DROP TABLE IF EXISTS email_verification_codes;
DROP TABLE IF EXISTS app_users;

-- 유저 테이블
CREATE TABLE app_users (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  login_id varchar(30) NOT NULL,
  name varchar(50) NOT NULL,
  email varchar(254) NOT NULL,
  password varchar(100) NOT NULL,
  gender enum('M','F','N') NOT NULL DEFAULT 'N',
  birthdate date NOT NULL,
  created_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY login_id (`login_id`),
  UNIQUE KEY email (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4


-- 암 테이블
CREATE TABLE cancers (
  cancer_id int(11) NOT NULL AUTO_INCREMENT,
  cancer_name varchar(50) NOT NULL,
  description text DEFAULT NULL,
  symptoms text DEFAULT NULL,
  PRIMARY KEY (`cancer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 

-- 채팅 테이블
CREATE TABLE chat_messages (
  message_id bigint(20) NOT NULL AUTO_INCREMENT,
  diagnosis_id int(11) NOT NULL,
  message_text text NOT NULL,
  sender enum('user','chatbot') NOT NULL,
  created_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`message_id`),
  KEY diagnosis_id (`diagnosis_id`),
  CONSTRAINT chat_messages_ibfk_1 FOREIGN KEY (`diagnosis_id`) REFERENCES diagnoses (`diagnosis_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 

--암 예측 테이블
CREATE TABLE diagnoses (
  diagnosis_id int(11) NOT NULL AUTO_INCREMENT,
  login_id varchar(30) NOT NULL,
  cancer_id int(11) NOT NULL,
  image_url varchar(255) DEFAULT NULL,
  certainty_score float DEFAULT NULL,
  diagnosed_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`diagnosis_id`),
  KEY idx_diag_user_id (`login_id`),
  KEY idx_diag_cancer_id (`cancer_id`),
  CONSTRAINT fk_diagnoses_cancer FOREIGN KEY (`cancer_id`) REFERENCES cancers (`cancer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_diagnoses_user FOREIGN KEY (`login_id`) REFERENCES app_users (`login_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 

-- 이메일 인증
CREATE TABLE email_verification_codes (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  email varchar(254) NOT NULL,
  code varchar(6) NOT NULL,
  expires_at datetime NOT NULL,
  created_at datetime NOT NULL DEFAULT current_timestamp(),
  attempt_count int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 

-- 건강 뉴스 테이블
CREATE TABLE health_news (
  article_id bigint(20) NOT NULL AUTO_INCREMENT,
  title varchar(255) NOT NULL,
  original_link text NOT NULL,
  link text NOT NULL,
  description text NOT NULL,
  pub_date datetime NOT NULL,
  created_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 
