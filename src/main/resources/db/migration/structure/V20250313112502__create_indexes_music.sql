CREATE INDEX idx_music_author_id ON music(author_id);
CREATE INDEX idx_music_files_music_id ON music_files(music_id);
CREATE INDEX idx_purchases_user_id ON purchases(user_id);
CREATE INDEX idx_purchases_music_id ON purchases(music_id);
