ALTER TABLE submissions
  ADD COLUMN upload_token_hash CHAR(64) NULL,
  ADD COLUMN upload_token_expires_at DATETIME NULL,
  ADD COLUMN upload_token_used_at DATETIME NULL;

ALTER TABLE submission_files
  CHANGE COLUMN file_url storage_key VARCHAR(1000) NOT NULL,
  ADD COLUMN content_type VARCHAR(150) NOT NULL DEFAULT 'application/octet-stream';

ALTER TABLE student_order_files
  CHANGE COLUMN file_url storage_key VARCHAR(1000) NOT NULL,
  ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN content_type VARCHAR(150) NOT NULL DEFAULT 'application/octet-stream';

CREATE INDEX idx_submissions_upload_token ON submissions(upload_token_hash, upload_token_expires_at);
