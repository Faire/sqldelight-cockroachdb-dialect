CREATE TABLE foo(
  id int NOT NULL,

  internal VARCHAR(255) NOT NULL,
  external VARCHAR(255) NOT NULL,

  INDEX idx_internal (internal),
  PRIMARY KEY (id)
);

CREATE INDEX idx_external_index ON foo (external);

DROP INDEX foo@idx_external_index;
DROP INDEX foo@idx_internal_index;
