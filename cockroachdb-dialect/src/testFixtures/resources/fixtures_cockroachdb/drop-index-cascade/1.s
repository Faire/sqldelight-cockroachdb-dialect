CREATE TABLE drop_unique_index(
  id INT NOT NULL,
  uid INT NOT NULL,
  PRIMARY KEY(id),
  UNIQUE INDEX idx_uid (uid)
);

DROP INDEX drop_unique_index@idx_uid CASCADE;
DROP INDEX IF EXISTS drop_unique_index@idx_uid CASCADE;
