-- Luôn trỏ vào đúng phòng
USE defaultdb;

-- =========================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU SÀN ĐẤU GIÁ (AUCTION SYSTEM)
-- =========================================================

-- 1. BẢNG NGƯỜI DÙNG (USERS)
-- Lưu trữ thông tin tài khoản, mật khẩu, quyền hạn và số dư tiền.
CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'BIDDER', -- Quyền: ADMIN, SELLER, BIDDER
    balance DOUBLE DEFAULT 0.0         -- Số dư tài khoản (VNĐ)
    );

-- 2. BẢNG SẢN PHẨM ĐẤU GIÁ (ITEMS)
-- Lưu trữ thông tin các món đồ đang và đã được đưa lên sàn.
CREATE TABLE IF NOT EXISTS items (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL UNIQUE,       -- Tên sản phẩm (Rất quan trọng, dùng để tìm kiếm)
    current_price INT NOT NULL,              -- Giá hiện tại (hoặc Giá khởi điểm lúc mới đăng)
    highest_bidder VARCHAR(50) DEFAULT 'None',-- Người đang trả giá cao nhất (Chưa có thì là 'None')
    image_base64 MEDIUMTEXT,                 -- Lưu chuỗi ảnh Base64 của món đồ
    status VARCHAR(20) DEFAULT 'ACTIVE',     -- Trạng thái: ACTIVE, FINISHED, PAID, CANCELED
    seller_name VARCHAR(50) NOT NULL,        -- Người đăng bán món đồ
    category VARCHAR(50) DEFAULT 'Khác'      -- Danh mục (Điện tử, Xe cộ, Nghệ thuật...)
    );

-- 3. BẢNG LỊCH SỬ ĐẤU GIÁ (BIDS)
-- Lưu lại từng bước giá để vẽ Biểu đồ (Price Curve) và xem Lịch sử cá nhân.
CREATE TABLE IF NOT EXISTS bids (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    item_id INT NOT NULL,                    -- Liên kết với món đồ
                                    username VARCHAR(50) NOT NULL,           -- Người đã đặt giá
    bid_amount INT NOT NULL,                 -- Số tiền đã đặt
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Thời gian đặt giá tự động sinh
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
    );

-- =========================================================
-- THÊM DỮ LIỆU TÀI KHOẢN MẪU ĐỂ TEST HỆ THỐNG
-- =========================================================

-- Tạo sẵn 1 tài khoản ADMIN (Tiền xài không bao giờ hết)
INSERT IGNORE INTO users (username, password, role, balance)
VALUES ('admin', '123456', 'ADMIN', 999999999.0);

-- Tạo sẵn 1 tài khoản SELLER (Chuyên đi đăng đồ)
INSERT IGNORE INTO users (username, password, role, balance)
VALUES ('nguoiban', '123456', 'SELLER', 100000.0);

-- Tạo sẵn 1 tài khoản BIDDER (Người mua chuyên săn sale)
INSERT IGNORE INTO users (username, password, role, balance)
VALUES ('nguoimua', '123456', 'BIDDER', 50000000.0);