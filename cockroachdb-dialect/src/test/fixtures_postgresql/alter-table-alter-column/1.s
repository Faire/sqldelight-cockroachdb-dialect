CREATE TABLE t1 (
  c1 INTEGER,
  c2 INTEGER DEFAULT 0,
  c3 VARCHAR(25),
  c4 TEXT,
  c5 NUMERIC(10, 2)
);

ALTER TABLE t1
    ALTER c1 SET NOT NULL,
    ALTER COLUMN c1 SET NOT NULL,
    ALTER COLUMN c1 ADD GENERATED ALWAYS AS IDENTITY,
    ALTER COLUMN c2 TYPE BIGINT,
    ALTER COLUMN c3 SET DATA TYPE TEXT,
    ALTER COLUMN c4 TYPE VARCHAR(25),
    ALTER COLUMN c5 TYPE INT USING c5::INTEGER;

ALTER TABLE t1 ALTER COLUMN c2 DROP DEFAULT;
ALTER TABLE t1 ALTER COLUMN c2 DROP IDENTITY;
ALTER TABLE t1 ALTER COLUMN c2 SET DEFAULT 0;
ALTER TABLE t1 ALTER COLUMN c2 SET NOT NULL;
ALTER TABLE t1 ALTER COLUMN c2 DROP NOT NULL;

ALTER TABLE t1 ALTER COLUMN c4 SET DEFAULT 'Test';

ALTER TABLE t1 ALTER COLUMN c1 SET GENERATED BY DEFAULT;
ALTER TABLE t1 ALTER COLUMN c1 SET GENERATED ALWAYS;
