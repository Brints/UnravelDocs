-- V12__add_on_delete_cascade_to_ocr_data_fk.sql
ALTER TABLE ocr_data
    ADD CONSTRAINT fk_ocr_data_document_file_entries
        FOREIGN KEY (document_id) REFERENCES document_file_entries(document_id) ON DELETE CASCADE;