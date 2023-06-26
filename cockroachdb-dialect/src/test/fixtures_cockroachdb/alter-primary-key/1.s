CREATE TABLE foo(
  id int NOT NULL,

  external_index VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE foo ALTER PRIMARY KEY USING COLUMNS (external_index);
