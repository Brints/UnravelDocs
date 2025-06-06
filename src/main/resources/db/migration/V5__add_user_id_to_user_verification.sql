-- V5__add_user_id_to_user_verification.sql

ALTER TABLE user_verification
    ADD COLUMN user_id VARCHAR(36);

ALTER TABLE user_verification
    ADD CONSTRAINT fk_user_verification_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Optional: Ensure one-to-one relationship
ALTER TABLE user_verification
    ADD CONSTRAINT uq_user_verification_user_id UNIQUE (user_id);

-- Backfill user_id in user_verification for existing data
UPDATE user_verification uv
SET user_id = u.id
    FROM users u
WHERE u.user_verification_id = uv.id;