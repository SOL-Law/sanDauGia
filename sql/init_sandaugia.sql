-- Luôn trỏ vào đúng phòng
USE railway;

-- 1. Xây bảng Sản phẩm (items)
CREATE TABLE items (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       start_price DOUBLE NOT NULL,
                       current_price DOUBLE NOT NULL,
                       end_time DATETIME NOT NULL
);

-- 2. Xây bảng Lịch sử (bids) - Có sử dụng Khóa Ngoại (FOREIGN KEY)
CREATE TABLE bids (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      item_id INT,
                      user_id INT,
                      bid_amount DOUBLE NOT NULL,
                      bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Dòng này nói cho MySQL biết: item_id phải lấy từ bảng items, user_id phải lấy từ bảng users
                      FOREIGN KEY (item_id) REFERENCES items(id),
                      FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. Bơm thử 1 cái Laptop Dell của bạn vào bảng Sản phẩm để lát test
INSERT INTO items (name, start_price, current_price, end_time)
VALUES ('Laptop Dell Inspiron 15 3530', 15000000, 15000000, '2026-05-01 20:00:00');