CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR (32),
    password TEXT,
    email VARCHAR (255),
    firstname VARCHAR (255),
    lastname VARCHAR (255),
    role VARCHAR (5)
);