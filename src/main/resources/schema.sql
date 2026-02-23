CREATE DATABASE booking_db;
USE booking_db;
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    created_at DATETIME NOT NULL,
    seats VARCHAR(1000) NOT NULL
);
ALTER TABLE booking
ADD COLUMN payment_transaction_id BIGINT;
CREATE TABLE seat_lock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    locked BOOLEAN NOT NULL,
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_number)
);

CREATE INDEX idx_booking_user ON booking(user_id);
CREATE INDEX idx_booking_idempotency ON booking(idempotency_key);
CREATE INDEX idx_seat_show ON seat_lock(show_id);
