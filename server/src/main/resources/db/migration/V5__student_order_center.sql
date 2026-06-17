CREATE TABLE student_orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  student_user_id BIGINT NOT NULL,
  source_submission_id BIGINT NULL,
  service_category VARCHAR(50) NOT NULL,
  target_type VARCHAR(50),
  title VARCHAR(500) NOT NULL,
  current_stage VARCHAR(100) NOT NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  currency_code VARCHAR(10) NOT NULL DEFAULT 'CNY',
  payment_status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
  order_status VARCHAR(50) NOT NULL DEFAULT 'NEW',
  consultant_name VARCHAR(255),
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_orders_user FOREIGN KEY (student_user_id) REFERENCES student_users(id),
  CONSTRAINT fk_student_orders_submission FOREIGN KEY (source_submission_id) REFERENCES submissions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_order_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  stage_code VARCHAR(100) NOT NULL,
  stage_label VARCHAR(255) NOT NULL,
  progress_note TEXT,
  visible_to_student BOOLEAN NOT NULL DEFAULT TRUE,
  operator_name VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_order_progress_order FOREIGN KEY (order_id) REFERENCES student_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_order_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  file_category VARCHAR(100) NOT NULL,
  file_name VARCHAR(500) NOT NULL,
  file_url VARCHAR(1000) NOT NULL,
  visible_to_student BOOLEAN NOT NULL DEFAULT TRUE,
  uploaded_by VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_order_files_order FOREIGN KEY (order_id) REFERENCES student_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invoice_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  student_user_id BIGINT NOT NULL,
  invoice_title VARCHAR(255) NOT NULL,
  tax_number VARCHAR(100),
  invoice_type VARCHAR(50) NOT NULL DEFAULT 'ELECTRONIC',
  invoice_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  receiver_email VARCHAR(255),
  receiver_phone VARCHAR(50),
  receiver_address VARCHAR(500),
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  remark TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_requests_order FOREIGN KEY (order_id) REFERENCES student_orders(id),
  CONSTRAINT fk_invoice_requests_user FOREIGN KEY (student_user_id) REFERENCES student_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE consultation_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_user_id BIGINT NULL,
  source_submission_id BIGINT NULL,
  contact_name VARCHAR(255) NOT NULL,
  mobile VARCHAR(50),
  email VARCHAR(255),
  source_channel VARCHAR(50) NOT NULL DEFAULT 'WEB',
  subject VARCHAR(255) NOT NULL,
  content TEXT,
  consultant_name VARCHAR(255),
  follow_up_status VARCHAR(50) NOT NULL DEFAULT 'NEW',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_consultation_records_user FOREIGN KEY (student_user_id) REFERENCES student_users(id),
  CONSTRAINT fk_consultation_records_submission FOREIGN KEY (source_submission_id) REFERENCES submissions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_student_orders_user_created ON student_orders(student_user_id, created_at DESC);
CREATE INDEX idx_student_orders_status_stage ON student_orders(order_status, current_stage);
CREATE INDEX idx_student_order_progress_order_created ON student_order_progress(order_id, created_at DESC);
CREATE INDEX idx_student_order_files_order_created ON student_order_files(order_id, created_at DESC);
CREATE INDEX idx_invoice_requests_user_status ON invoice_requests(student_user_id, status);
CREATE INDEX idx_consultation_records_status_created ON consultation_records(follow_up_status, created_at DESC);
