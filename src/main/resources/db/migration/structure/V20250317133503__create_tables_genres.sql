-- Genres
CREATE TABLE IF NOT EXISTS genres
(
    id         SERIAL PRIMARY KEY,
    genre_name VARCHAR(20)
);

-- Genres-music
CREATE TABLE IF NOT EXISTS music_genres
(
    music_id BIGINT REFERENCES music (id) ON DELETE CASCADE,
    genre_id INT REFERENCES genres (id),
    PRIMARY KEY (music_id, genre_id)
);
