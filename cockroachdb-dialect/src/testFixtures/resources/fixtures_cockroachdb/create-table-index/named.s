CREATE TABLE foo_named(
  id INT NOT NULL,
  bar1 VARCHAR(255) NOT NULL,
  bar2 VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_bar1 (bar1),
  UNIQUE INDEX idx_bar2 (bar2)
);
