CREATE TABLE feed
(
  id INT AUTO_INCREMENT,
  published_date DATETIME NOT NULL,
  payload TEXT,
  PRIMARY KEY (id)
);

CREATE INDEX feed_published_idx ON feed (published_date);