-- User Table
CREATE TABLE users (
    mid BIGINT PRIMARY KEY,
    name VARCHAR(255),
    sex VARCHAR(50),
    birthday DATE,
    level INT,
    sign TEXT,
    identity VARCHAR(50),
    coin INT
);

-- AuthInfo Table
CREATE TABLE auth_info (
    mid BIGINT PRIMARY KEY REFERENCES users(mid),
    password VARCHAR(255),
    qq VARCHAR(255),
    wechat VARCHAR(255)
);


-- Video Table
CREATE TABLE videos (
    bv VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255),
    ownerMid BIGINT REFERENCES users(mid),
    commitTime TIMESTAMP,
    reviewTime TIMESTAMP,
    publicTime TIMESTAMP,
    duration INT,
    description TEXT,
    isPublic BOOLEAN,
    reviewer BIGINT REFERENCES users(mid)
);

-- UserVideoInteraction Table
CREATE TABLE user_video_interaction (
    mid BIGINT REFERENCES users(mid),
    bv VARCHAR(50) REFERENCES videos(bv),
    is_liked BOOLEAN DEFAULT FALSE,
    is_coined BOOLEAN DEFAULT FALSE,
    is_favorited BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (mid, bv)
);


-- UserVideoWatch Table
CREATE TABLE user_video_watch (
    mid BIGINT REFERENCES users(mid),
    bv VARCHAR(50) REFERENCES videos(bv),
    watch_time FLOAT,
    PRIMARY KEY (mid, bv)
);

-- UserRelationships Table
CREATE TABLE user_relationships (
    followerMid BIGINT REFERENCES users(mid),
    followingMid BIGINT REFERENCES users(mid),
    PRIMARY KEY (followerMid, followingMid)
);

-- Danmu Table
CREATE TABLE danmus (
    id SERIAL PRIMARY KEY,
    bv VARCHAR(50) REFERENCES videos(bv),
    mid BIGINT REFERENCES users(mid),
    time FLOAT,
    content TEXT,
    postTime TIMESTAMP
);

-- DanmuLike Table
CREATE TABLE danmu_like (
    danmuId INT REFERENCES danmus(id),
    mid BIGINT REFERENCES users(mid),
    PRIMARY KEY (danmuId, mid)
);

CREATE TABLE video_interactions_aggregates (
    bv VARCHAR(50) PRIMARY KEY,
    like_count INT,
    coin_count INT,
    fav_count INT
);

CREATE TABLE video_stats (
    bv VARCHAR(50) PRIMARY KEY,
    like_rate FLOAT,
    coin_rate FLOAT,
    fav_rate FLOAT
);

-- -- UserVideoFavorite Table
-- CREATE TABLE user_video_favorite (
--     mid BIGINT REFERENCES users(mid),
--     bv VARCHAR(50) REFERENCES videos(bv),
--     PRIMARY KEY (mid, bv)
-- );
--
-- -- UserVideoLike Table
-- CREATE TABLE user_video_like (
--     mid BIGINT REFERENCES users(mid),
--     bv VARCHAR(50) REFERENCES videos(bv),
--     PRIMARY KEY (mid, bv)
-- );
--
-- -- UserVideoCoin Table
-- CREATE TABLE user_video_coin (
--     mid BIGINT REFERENCES users(mid),
--     bv VARCHAR(50) REFERENCES videos(bv),
--     PRIMARY KEY (mid, bv)
-- );


CREATE OR REPLACE FUNCTION recommend_videos_for_user(current_user_id BIGINT, pageSize INT, pageNum INT)
RETURNS TABLE(bv VARCHAR(50)) AS $$
DECLARE
    friend_count INT;
BEGIN
    -- 检查互关数量
    SELECT COUNT(*) INTO friend_count
    FROM user_relationships a
    JOIN user_relationships b ON a.followerMid = b.followingMid AND a.followingMid = b.followerMid
    WHERE a.followingMid = current_user_id;

    -- 如果有互关，执行特定推荐逻辑
    IF friend_count > 0 THEN
        RETURN QUERY
        SELECT vid.bv
        FROM videos vid
        JOIN users usr ON vid.ownerMid = usr.mid
        WHERE vid.bv IN (
            SELECT uvw.bv
            FROM user_video_watch uvw
            JOIN user_relationships ur ON uvw.mid = ur.followerMid OR uvw.mid = ur.followingMid
            WHERE (ur.followerMid = current_user_id OR ur.followingMid = current_user_id)
            AND uvw.mid != current_user_id
        )
        AND vid.bv NOT IN (
            SELECT uvw.bv FROM user_video_watch uvw WHERE uvw.mid = current_user_id
        )
        ORDER BY usr.level DESC, vid.publicTime DESC
        LIMIT pageSize OFFSET pageSize * (pageNum - 1);

    -- 如果没有互关，执行一般推荐逻辑
    ELSE
        RETURN QUERY
        SELECT vid.bv
        FROM videos vid
        JOIN users usr ON vid.ownerMid = usr.mid
        WHERE vid.bv NOT IN (
            SELECT uvw.bv FROM user_video_watch uvw WHERE uvw.mid = current_user_id
        )
        ORDER BY usr.level DESC, vid.publicTime DESC
        LIMIT pageSize OFFSET pageSize * (pageNum - 1);
    END IF;
END;
$$ LANGUAGE plpgsql;

