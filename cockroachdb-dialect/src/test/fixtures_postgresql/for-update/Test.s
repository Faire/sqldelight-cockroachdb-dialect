CREATE TABLE run(
  status TEXT NOT NULL
);

SELECT * FROM run WHERE status = 'WAITING' LIMIT 1 FOR UPDATE;
SELECT * FROM run WHERE status = 'WAITING' LIMIT 1 FOR UPDATE SKIP LOCKED;