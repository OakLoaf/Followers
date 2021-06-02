CREATE TABLE IF NOT EXISTS follower_users
(
    uuid CHAR(36) NOT NULL,
    name TEXT NOT NULL,
    follower TEXT NOT NULL,
    followerDisplayName TEXT NOT NULL,
    followerNameEnabled BOOLEAN DEFAULT false,
    followerEnabled BOOLEAN DEFAULT true,
    PRIMARY KEY (uuid)
);