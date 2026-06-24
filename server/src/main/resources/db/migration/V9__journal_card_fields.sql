ALTER TABLE journals
  ADD COLUMN image_url VARCHAR(1000),
  ADD COLUMN journal_level VARCHAR(255),
  ADD COLUMN impact_factor_label VARCHAR(100),
  ADD COLUMN impact_factor_value DECIMAL(8,3),
  ADD COLUMN journal_partition VARCHAR(255),
  ADD COLUMN acceptance_time VARCHAR(100),
  ADD COLUMN submission_deadline_text VARCHAR(100),
  ADD COLUMN submission_deadline_date DATE,
  ADD COLUMN discipline_category VARCHAR(255),
  ADD COLUMN cas_zone VARCHAR(100),
  ADD COLUMN jcr_quartile VARCHAR(100),
  ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;

UPDATE journals
SET
  journal_level = COALESCE(NULLIF(journal_level, ''), index_type),
  acceptance_time = COALESCE(NULLIF(acceptance_time, ''), cycle),
  submission_deadline_text = COALESCE(NULLIF(submission_deadline_text, ''), '长期征稿'),
  discipline_category = COALESCE(NULLIF(discipline_category, ''), field_name),
  impact_factor_label = COALESCE(NULLIF(impact_factor_label, ''), '-'),
  journal_partition = COALESCE(NULLIF(journal_partition, ''), '-'),
  cas_zone = COALESCE(NULLIF(cas_zone, ''), '-'),
  jcr_quartile = COALESCE(NULLIF(jcr_quartile, ''), '-');

CREATE INDEX idx_journals_category ON journals(type, discipline_category, journal_level, published);
CREATE INDEX idx_journals_deadline ON journals(submission_deadline_date);
