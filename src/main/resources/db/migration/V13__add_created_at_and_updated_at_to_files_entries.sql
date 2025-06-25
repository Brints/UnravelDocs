ALTER TABLE document_file_entries
ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Update existing entries to set created_at and updated_at to the current timestamp
UPDATE document_file_entries
SET created_at = NOW(), updated_at = NOW()
WHERE created_at IS NULL OR updated_at IS NULL;
