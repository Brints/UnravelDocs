CREATE TABLE ocr_data (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    extracted_text TEXT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ocr_data_document_id ON ocr_data(document_id);