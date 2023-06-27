CREATE TABLE foo_one(
  id INT NOT NULL,
  bar VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_bar (bar)
);

CREATE TABLE foo_two(
  id INT NOT NULL,
  bar VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_bar (bar)
);