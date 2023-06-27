CREATE TABLE foo(
  id INT NOT NULL,
  name STRING NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO foo(id, name) VALUES (1, 'bar');

SELECT *
FROM foo
WHERE name = 'bar';