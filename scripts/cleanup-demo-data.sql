-- Run only after a database backup. This removes records created by the old
-- demo initializer and automated QA suites, while preserving unmarked data.
START TRANSACTION;

CREATE TEMPORARY TABLE cleanup_student_ids (id BIGINT PRIMARY KEY);
INSERT INTO cleanup_student_ids (id)
SELECT id
FROM student_users
WHERE display_name IN ('张同学', '李同学', '快速注册测试', 'Codex QA');

CREATE TEMPORARY TABLE cleanup_submission_ids (id BIGINT PRIMARY KEY);
INSERT IGNORE INTO cleanup_submission_ids (id)
SELECT id
FROM submissions
WHERE LOWER(email) IN ('zhang@example.com', 'li@example.com')
   OR LOWER(email) LIKE 'qa-%'
   OR LOWER(email) LIKE 'neg-qa-%'
   OR LOWER(email) LIKE 'codex-%'
   OR LOWER(author_name) LIKE 'codex%'
   OR UPPER(paper_title) LIKE 'QA-%'
   OR UPPER(paper_title) LIKE 'NEG-QA-%'
   OR UPPER(paper_title) LIKE 'CODEX %'
   OR UPPER(paper_title) LIKE 'REGRESSION TEST%';

CREATE TEMPORARY TABLE cleanup_order_ids (id BIGINT PRIMARY KEY);
INSERT IGNORE INTO cleanup_order_ids (id)
SELECT id
FROM student_orders
WHERE student_user_id IN (SELECT id FROM cleanup_student_ids)
   OR source_submission_id IN (SELECT id FROM cleanup_submission_ids);

-- An old demo student may own manually-created test orders. Their linked
-- submissions must be removed before the account can be deleted.
INSERT IGNORE INTO cleanup_submission_ids (id)
SELECT source_submission_id
FROM student_orders
WHERE id IN (SELECT id FROM cleanup_order_ids)
  AND source_submission_id IS NOT NULL;

DELETE FROM invoice_requests
WHERE order_id IN (SELECT id FROM cleanup_order_ids)
   OR student_user_id IN (SELECT id FROM cleanup_student_ids);

DELETE FROM consultation_records
WHERE student_user_id IN (SELECT id FROM cleanup_student_ids)
   OR source_submission_id IN (SELECT id FROM cleanup_submission_ids);

DELETE FROM student_order_files WHERE order_id IN (SELECT id FROM cleanup_order_ids);
DELETE FROM student_order_progress WHERE order_id IN (SELECT id FROM cleanup_order_ids);
DELETE FROM student_orders WHERE id IN (SELECT id FROM cleanup_order_ids);
DELETE FROM submission_files WHERE submission_id IN (SELECT id FROM cleanup_submission_ids);
DELETE FROM submissions WHERE id IN (SELECT id FROM cleanup_submission_ids);
DELETE FROM student_users WHERE id IN (SELECT id FROM cleanup_student_ids);

SELECT
  (SELECT COUNT(*) FROM cleanup_student_ids) AS removed_students,
  (SELECT COUNT(*) FROM cleanup_submission_ids) AS removed_submissions,
  (SELECT COUNT(*) FROM cleanup_order_ids) AS removed_orders;

COMMIT;
