-- V_X__create_document_collections_and_entries.sql

-- Assuming you are dropping the old 'documents' table and creating new ones.
-- If migrating data, this would be more complex.
DROP TABLE IF EXISTS documents; -- Or RENAME if you want to migrate data

CREATE TABLE document_collections (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    collection_status VARCHAR(50) NOT NULL,
    upload_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_document_collection_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE document_file_entries (
    document_collection_id VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_id VARCHAR(255),
    file_url VARCHAR(1024),
    file_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_status VARCHAR(50) NOT NULL,
    error_message TEXT,
    CONSTRAINT fk_file_entry_collection FOREIGN KEY (document_collection_id) REFERENCES document_collections(id)
);

-- Add indexes as needed, e.g., on document_collection_id in document_file_entries
CREATE INDEX idx_doc_file_entries_coll_id ON document_file_entries(document_collection_id);