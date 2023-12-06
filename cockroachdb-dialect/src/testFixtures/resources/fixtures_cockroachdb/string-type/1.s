CREATE TABLE foo(
  id INT NOT NULL,
  name STRING NOT NULL,
  code STRING(4) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO foo(id, name, code) VALUES (1, 'bar', 'ABCD');

SELECT *
FROM foo
WHERE name = 'bar';
