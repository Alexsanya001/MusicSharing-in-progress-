INSERT INTO users (username,
                   password,
                   email,
                   first_name,
                   last_name,
                   role)
VALUES ('admin',
        '${admin_password}',
        '${admin_email}',
        'John',
        'Doe',
        'ROLE_ADMIN');