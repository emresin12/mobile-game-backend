CREATE TABLE IF NOT EXISTS users
(
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)                                       NOT NULL,
    level    INT                                               NOT NULL,
    coins    INT                                               NOT NULL,
    country  ENUM ('FRANCE', 'GERMANY', 'TURKEY', 'UK', 'USA') NOT NULL
);

CREATE TABLE IF NOT EXISTS tournaments
(
    tournament_id INT AUTO_INCREMENT PRIMARY KEY,
    date          DATE    NOT NULL,
    is_active     BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS tournament_groups
(
    group_id      INT AUTO_INCREMENT PRIMARY KEY,
    is_active     BOOLEAN NOT NULL,
    tournament_id INT,
    FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tournament_rewards
(
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    reward  INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS group_members
(
    group_id INT,
    user_id  INT,
    country  ENUM ('FRANCE', 'GERMANY', 'TURKEY', 'UK', 'USA') NOT NULL,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES tournament_groups (group_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_group_country ON group_members (group_id, country);


CREATE TABLE IF NOT EXISTS tournament_entries
(
    user_id       INT,
    tournament_id INT,
    group_id      INT,
    score         INT NOT NULL,
    PRIMARY KEY (user_id, tournament_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id),
    FOREIGN KEY (group_id) REFERENCES tournament_groups (group_id)
);
