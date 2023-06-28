CREATE TABLE foo(
  id INT NOT NULL,
  bar1 VARCHAR(255) NOT NULL,
  bar2 VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_storing1 (bar1) STORING (missing)
);

CREATE INDEX idx_storing2 ON foo (bar2) STORING (missing);
