CREATE TABLE foo_col_hash_default(
  id UUID NOT NULL PRIMARY KEY USING HASH
);

CREATE TABLE foo_col_hash_option(
  id UUID NOT NULL PRIMARY KEY USING HASH WITH ( bucket_count = 42 )
);
