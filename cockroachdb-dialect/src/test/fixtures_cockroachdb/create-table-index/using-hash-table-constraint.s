CREATE TABLE foo_table_hash_default(
  id UUID NOT NULL,
  PRIMARY KEY (id) USING HASH
);

CREATE TABLE foo_table_hash_option(
  id UUID NOT NULL,
  PRIMARY KEY (id) USING HASH WITH ( bucket_count = 42 )
);
