-- E-ship 数据库初始化脚本
-- 创建数据库
CREATE DATABASE eship;

-- 使用数据库
CREATE SCHEMA IF NOT EXISTS easyGo;
SET search_path TO easyGo;
COMMENT ON SCHEMA easyGo IS 'E-ship业务核心 Schema，存放应用相关隐私数据';

-- 1. 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    company_name VARCHAR(200),
    credit_score INT DEFAULT 100,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    lock_until TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('SHIP_OWNER', 'RENTER', 'ADMIN')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

COMMENT ON TABLE tb_user IS '用户表';
COMMENT ON COLUMN tb_user.username IS '用户名';
COMMENT ON COLUMN tb_user.password IS '密码（加密）';
COMMENT ON COLUMN tb_user.email IS '邮箱';
COMMENT ON COLUMN tb_user.role IS '角色：SHIP_OWNER(船东)/RENTER(租家)/ADMIN(管理员)';
COMMENT ON COLUMN tb_user.company_name IS '公司名称';
COMMENT ON COLUMN tb_user.credit_score IS '信用评分（0-100）';
COMMENT ON COLUMN tb_user.status IS '账号状态：ACTIVE(正常)/LOCKED(锁定)/DISABLED(禁用)';
COMMENT ON COLUMN tb_user.lock_until IS '锁定截止时间';
COMMENT ON COLUMN tb_user.failed_login_attempts IS '登录失败次数';

-- 2. 船舶表
CREATE TABLE IF NOT EXISTS tb_ship (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    ship_name VARCHAR(200) NOT NULL,
    ship_type VARCHAR(50) NOT NULL,
    tonnage DECIMAL(10, 2),
    build_year INT,
    classification_society VARCHAR(100),
    daily_rent DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    rating DECIMAL(3, 2) DEFAULT 5.00,
    image_url VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_ship_type CHECK (ship_type IN ('CONTAINER', 'BULK', 'TANKER', 'PASSENGER')),
    CONSTRAINT chk_ship_status CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE'))
);

COMMENT ON TABLE tb_ship IS '船舶表';
COMMENT ON COLUMN tb_ship.owner_id IS '船东ID';
COMMENT ON COLUMN tb_ship.ship_name IS '船舶名称';
COMMENT ON COLUMN tb_ship.ship_type IS '船舶类型：CONTAINER(集装箱船)/BULK(散货船)/TANKER(油船)/PASSENGER(客船)';
COMMENT ON COLUMN tb_ship.tonnage IS '载重吨位';
COMMENT ON COLUMN tb_ship.build_year IS '建造年份';
COMMENT ON COLUMN tb_ship.classification_society IS '船级社认证';
COMMENT ON COLUMN tb_ship.daily_rent IS '日租金';
COMMENT ON COLUMN tb_ship.status IS '状态：AVAILABLE(可租)/RENTED(已租)/MAINTENANCE(维护中)';
COMMENT ON COLUMN tb_ship.rating IS '综合评分（0-5）';

-- 3. 合约表
CREATE TABLE IF NOT EXISTS tb_contract (
    id BIGSERIAL PRIMARY KEY,
    ship_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    daily_rent DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    purpose TEXT,
    special_requirements TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    reject_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ship_id) REFERENCES tb_ship(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    FOREIGN KEY (renter_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_contract_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

COMMENT ON TABLE tb_contract IS '合约表';
COMMENT ON COLUMN tb_contract.status IS '状态：PENDING(待审核)/APPROVED(已同意)/REJECTED(已拒绝)/IN_PROGRESS(进行中)/COMPLETED(已完成)/CANCELLED(已取消)';

-- 4. 验证码表
CREATE TABLE IF NOT EXISTS tb_verification_code (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_at TIMESTAMP NOT NULL,   
    used BOOLEAN DEFAULT FALSE,
    CONSTRAINT chk_code_type CHECK (type IN ('LOGIN', 'RESET_PASSWORD'))
);

COMMENT ON TABLE tb_verification_code IS '验证码表';
COMMENT ON COLUMN tb_verification_code.type IS '类型：LOGIN(登录)/RESET_PASSWORD(重置密码)';
COMMENT ON COLUMN tb_verification_code.expire_at IS '过期时间(10min内未使用则过期)';

-- 5. 登录日志表
CREATE TABLE IF NOT EXISTS tb_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    fail_reason VARCHAR(200),
    CONSTRAINT chk_login_status CHECK (status IN ('SUCCESS', 'FAILED'))
);

COMMENT ON TABLE tb_login_log IS '登录日志表';

-- 6. 船舶收藏表
CREATE TABLE IF NOT EXISTS tb_ship_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ship_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    FOREIGN KEY (ship_id) REFERENCES tb_ship(id) ON DELETE CASCADE,
    UNIQUE (user_id, ship_id)
);

COMMENT ON TABLE tb_ship_favorite IS '船舶收藏表';

-- 7. 评价表
CREATE TABLE IF NOT EXISTS tb_review (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES tb_contract(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);

COMMENT ON TABLE tb_review IS '评价表';
COMMENT ON COLUMN tb_review.rating IS '评分（1-5）';

-- 创建索引
CREATE INDEX idx_user_email ON tb_user(email);
CREATE INDEX idx_user_role ON tb_user(role);
CREATE INDEX idx_ship_owner ON tb_ship(owner_id);
CREATE INDEX idx_ship_type ON tb_ship(ship_type);
CREATE INDEX idx_ship_status ON tb_ship(status);
CREATE INDEX idx_contract_ship ON tb_contract(ship_id);
CREATE INDEX idx_contract_owner ON tb_contract(owner_id);
CREATE INDEX idx_contract_renter ON tb_contract(renter_id);
CREATE INDEX idx_contract_status ON tb_contract(status);
CREATE INDEX idx_verification_email ON tb_verification_code(email);
CREATE INDEX idx_login_user ON tb_login_log(user_id);

-- 插入管理员账号（密码需要在应用中加密后更新）
INSERT INTO tb_user (username, password, email, role, company_name, credit_score, status)
VALUES ('admin', '$2a$10$placeholder', '188043648@qq.com', 'ADMIN', 'E-ship', 100, 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- -- 插入测试数据（可选，用于开发测试）
-- -- 测试船东
-- INSERT INTO tb_user (username, password, email, role, company_name, credit_score)
-- VALUES
--     ('张船东', '$2a$10$placeholder', 'shipowner1@test.com', 'SHIP_OWNER', '远洋航运公司', 95),
--     ('李船东', '$2a$10$placeholder', 'shipowner2@test.com', 'SHIP_OWNER', '海运集团', 88)
-- ON CONFLICT (username) DO NOTHING;

-- -- 测试租家
-- INSERT INTO tb_user (username, password, email, role, company_name, credit_score)
-- VALUES
--     ('王租家', '$2a$10$placeholder', 'renter1@test.com', 'RENTER', '贸易有限公司', 92),
--     ('赵租家', '$2a$10$placeholder', 'renter2@test.com', 'RENTER', '物流公司', 85)
-- ON CONFLICT (username) DO NOTHING;

-- -- 测试船舶
-- INSERT INTO tb_ship (owner_id, ship_name, ship_type, tonnage, build_year, daily_rent, status, rating)
-- VALUES
--     (2, '远洋一号', 'CONTAINER', 50000.00, 2018, 15000.00, 'AVAILABLE', 4.8),
--     (2, '远洋二号', 'BULK', 80000.00, 2020, 20000.00, 'AVAILABLE', 4.9),
--     (3, '海运之星', 'TANKER', 100000.00, 2019, 25000.00, 'RENTED', 4.7);
