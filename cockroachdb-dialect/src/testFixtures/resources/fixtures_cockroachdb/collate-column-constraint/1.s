CREATE TABLE foo (
  id INT NOT NULL,
  bar VARCHAR(255) COLLATE en_US_u_ks_level2 NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE foo
  ADD COLUMN baz VARCHAR(255) COLLATE en_US_u_ks_level2 NOT NULL;
