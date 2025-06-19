-- Drop the existing foreign key constraint
ALTER TABLE document_collections
DROP CONSTRAINT fk_document_collection_user;

-- Add the foreign key constraint with ON DELETE CASCADE
ALTER TABLE document_collections
    ADD CONSTRAINT fk_document_collection_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;