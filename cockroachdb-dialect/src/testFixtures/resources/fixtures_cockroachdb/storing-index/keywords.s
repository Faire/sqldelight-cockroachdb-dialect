CREATE TABLE foo_keyword(
  id INT NOT NULL,
  bar1 VARCHAR(255) NOT NULL,
  bar2 VARCHAR(255) NOT NULL,
  bar3 VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_storing1 ON foo_keyword (bar1) STORING (bar2);
CREATE INDEX idx_storing2 ON foo_keyword (bar2) COVERING (bar3);
CREATE INDEX idx_storing3 ON foo_keyword (bar3) INCLUDE (bar1);
