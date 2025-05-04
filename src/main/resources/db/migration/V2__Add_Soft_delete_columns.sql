-- Add deletion date to users table
ALTER TABLE users
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Add deletion at to user verification table to cascade soft delete
ALTER TABLE user_verification
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Create partial indexes for better performance when querying non-deleted records
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_verification_deleted_at ON user_verification(deleted_at) WHERE deleted_at IS NULL;

-- Update the foreign key constraint to include soft delete logic
ALTER TABLE users DROP CONSTRAINT users_user_verification_id_fkey;

ALTER TABLE users ADD CONSTRAINT users_user_verification_id_fkey
    FOREIGN KEY (user_verification_id)
        REFERENCES user_verification(id)
        ON DELETE CASCADE;