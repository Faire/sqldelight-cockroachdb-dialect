CREATE TABLE foo(
  id INT NOT NULL,
  expired_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
) WITH (ttl_expiration_expression = 'expired_at', ttl_job_cron = '@daily');

ALTER TABLE foo SET (exclude_data_from_backup = true);

ALTER TABLE foo SET (ttl_select_batch_size = 123, ttl_pause = 'on');

ALTER TABLE foo RESET (ttl_job_cron);

ALTER TABLE foo RESET (ttl_expiration_expression, exclude_data_from_backup);
