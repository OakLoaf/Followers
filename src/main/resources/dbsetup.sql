CREATE TABLE IF NOT EXISTS follower_users
(
    uuid CHAR(36) NOT NULL,
    name TEXT NOT NULL,
    follower TEXT NOT NULL,
    followerDisplayName TEXT NOT NULL,
    followerNameEnabled BOOLEAN DEFAULT false,
    followerEnabled BOOLEAN DEFAULT true,
    randomFollower BOOLEAN DEFAULT false,
    PRIMARY KEY (uuid)
);

ALTER TABLE follower_users
ADD COLUMN IF NOT EXISTS randomFollower BOOLEAN DEFAULT false;