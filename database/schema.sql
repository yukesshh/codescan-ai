-- ============================================================
-- AI Code Review Platform - Database Schema
-- Run this in MySQL Workbench or MySQL CLI
-- ============================================================

CREATE DATABASE IF NOT EXISTS ai_code_review CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_code_review;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100),
    total_reviews   INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Code reviews table
CREATE TABLE IF NOT EXISTS code_reviews (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    title               VARCHAR(200) NOT NULL,
    language            VARCHAR(50) NOT NULL,
    original_code       LONGTEXT NOT NULL,
    ai_feedback         LONGTEXT,
    overall_rating      DOUBLE,
    complexity_score    INT,
    complexity_level    VARCHAR(20),
    bugs_found          INT DEFAULT 0,
    suggestions_count   INT DEFAULT 0,
    lines_of_code       INT DEFAULT 0,
    status              ENUM('PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'PENDING',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- Review issues table
CREATE TABLE IF NOT EXISTS review_issues (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id       BIGINT NOT NULL,
    issue_type      ENUM('BUG','CODE_SMELL','SECURITY','PERFORMANCE','STYLE','COMPLEXITY','BEST_PRACTICE') NOT NULL,
    severity        ENUM('CRITICAL','HIGH','MEDIUM','LOW','INFO') NOT NULL,
    line_number     INT,
    description     TEXT,
    suggestion      TEXT,
    FOREIGN KEY (review_id) REFERENCES code_reviews(id) ON DELETE CASCADE,
    INDEX idx_review_id (review_id)
);

-- Sample data (optional - for demo)
-- Password is: password123 (bcrypt encoded)
INSERT INTO users (username, email, password, full_name) VALUES
('demo', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo User')
ON DUPLICATE KEY UPDATE username=username;

SELECT 'Database setup complete!' as Status;
