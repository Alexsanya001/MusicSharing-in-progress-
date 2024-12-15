ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT unique_username UNIQUE (username);

ALTER TABLE users
    ADD CONSTRAINT length_username
        CHECK (CHAR_LENGTH(username) BETWEEN 3 AND 32);

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT unique_email UNIQUE (email);

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;
