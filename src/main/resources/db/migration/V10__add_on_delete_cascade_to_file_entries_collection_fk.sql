-- V10__add_on_delete_cascade_to_file_entries_collection_fk.sql

-- Drop the existing foreign key constraint
ALTER TABLE document_file_entries
DROP CONSTRAINT fk_file_entry_collection;

-- Add the foreign key constraint with ON DELETE CASCADE
ALTER TABLE document_file_entries
    ADD CONSTRAINT fk_file_entry_collection
        FOREIGN KEY (document_collection_id) REFERENCES document_collections(id) ON DELETE CASCADE;