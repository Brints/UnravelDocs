CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_id VARCHAR(255) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT NOT NULL,
    document_status VARCHAR(50) NOT NULL,
    upload_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_status ON documents(document_status);
CREATE INDEX idx_documents_upload_timestamp ON documents(upload_timestamp);
-- Add a check constraint to ensure that the status is one of the allowed values
ALTER TABLE documents ADD CONSTRAINT check_document_status CHECK (document_status IN ('UPLOADED', 'FAILED_OCR', 'DELETED', 'PROCESSING', 'PROCESSED'));
-- Add a check constraint to ensure that the file size is a positive number
ALTER TABLE documents ADD CONSTRAINT check_file_size_positive CHECK (file_size > 0);
-- Add a check constraint to ensure that the file type is not empty
ALTER TABLE documents ADD CONSTRAINT check_file_type_not_empty CHECK (file_type IS NOT NULL AND file_type <> '');