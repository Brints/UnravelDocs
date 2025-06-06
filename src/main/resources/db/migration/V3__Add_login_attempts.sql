-- Create login_attempts table
CREATE TABLE login_attempts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    login_attempts INT DEFAULT 0 NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE NOT NULL,
    blocked_until TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for login_attempts table
CREATE INDEX idx_login_attempts_user_id ON login_attempts(user_id);
CREATE INDEX idx_login_attempts_is_blocked ON login_attempts(is_blocked);
CREATE INDEX idx_login_attempts_blocked_until ON login_attempts(blocked_until);
