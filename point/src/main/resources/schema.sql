CREATE TABLE IF NOT EXISTS point_transactions (
    id BIGSERIAL PRIMARY KEY,
    member_id VARCHAR(100) NOT NULL,
    partner_id VARCHAR(100) NOT NULL,
    partner_type VARCHAR(20) NOT NULL,
    amount DECIMAL NOT NULL,
    points DECIMAL NOT NULL,
    transaction_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_member_id_transaction_time
    ON point_transactions (member_id, transaction_time);
