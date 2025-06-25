ALTER TABLE user_verification
    ADD COLUMN user_id VARCHAR(36);

ALTER TABLE user_verification
    ADD CONSTRAINT fk_user_verification_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_verification
    ADD CONSTRAINT uq_user_verification_user_id UNIQUE (user_id);

UPDATE user_verification uv
SET user_id = u.id
    FROM users u
WHERE u.user_verification_id = uv.id;