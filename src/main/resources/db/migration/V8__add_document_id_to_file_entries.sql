-- Migration script to add document_id to document_file_entries table
ALTER TABLE document_file_entries ADD COLUMN document_id VARCHAR(36);
UPDATE document_file_entries SET document_id = gen_random_uuid() WHERE document_id IS NULL; -- For PostgreSQL
ALTER TABLE document_file_entries ALTER COLUMN document_id SET NOT NULL;
ALTER TABLE document_file_entries ADD CONSTRAINT uq_document_file_entry_id UNIQUE (document_id);
