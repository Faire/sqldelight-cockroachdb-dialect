CREATE TABLE foo(
  id INT NOT NULL,
  bar1 INT NOT NULL,
  bar2 BOOL NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_storing1 (bar1) WHERE (bar1 > 0)
);

CREATE INDEX idx_storing2 ON foo (bar2) WHERE (bar2 = TRUE);
