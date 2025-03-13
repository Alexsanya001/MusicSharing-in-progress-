-- Music metadata
CREATE TABLE IF NOT EXISTS music
(
    id        BIGSERIAL PRIMARY KEY,
    title     VARCHAR NOT NULL,
    price     DECIMAL(10, 2),
    author_id BIGINT  NOT NULL DEFAULT 1 REFERENCES users (id) ON DELETE SET DEFAULT
);

-- Music files
CREATE TABLE IF NOT EXISTS music_files
(
    id       BIGSERIAL PRIMARY KEY,
    file_url TEXT UNIQUE   NOT NULL,
    music_id BIGINT UNIQUE NOT NULL REFERENCES music (id) ON DELETE CASCADE
);

-- Purchased tracks
CREATE TABLE IF NOT EXISTS purchases
(
    music_id BIGINT NOT NULL REFERENCES music (id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, music_id)
);