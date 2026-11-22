CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;
CREATE TABLE IF NOT EXISTS registration (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  student_id CHAR(9)      NOT NULL,
  name       VARCHAR(60)  NOT NULL,
  event_name VARCHAR(40)  NOT NULL,
  UNIQUE KEY uq_student_event (student_id, event_name)   -- final guard against double registration
);
