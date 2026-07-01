ALTER TABLE submissions
  ADD COLUMN service_type VARCHAR(100) NULL,
  ADD COLUMN expedited BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN contact_phone VARCHAR(50) NULL,
  ADD COLUMN special_requirements TEXT NULL;

CREATE TABLE submission_authors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  author_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NULL,
  institution VARCHAR(500) NULL,
  is_corresponding BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_submission_authors_submission
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_submission_authors_submission_order
  ON submission_authors(submission_id, sort_order, id);

INSERT INTO submission_authors (
  submission_id, author_name, email, institution, is_corresponding, sort_order
)
SELECT id, author_name, email, NULL, TRUE, 0
FROM submissions;
