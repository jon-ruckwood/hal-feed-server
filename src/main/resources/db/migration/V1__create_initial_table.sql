CREATE TABLE feed
(
  id VARCHAR(128),
  published_date DATETIME NOT NULL,
  payload TEXT,
  PRIMARY KEY (id)
);

CREATE INDEX feed_published_idx ON feed (published_date);