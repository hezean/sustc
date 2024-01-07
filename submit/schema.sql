DROP TABLE IF EXISTS "user" CASCADE;
CREATE TABLE "user"
(
    "mid"      BIGINT PRIMARY KEY,
    "name"     TEXT        NOT NULL,
    "sex"      VARCHAR(4)  NOT NULL,
    "birthday" VARCHAR(20),
    "level"    SMALLINT    NOT NULL DEFAULT 0,
    "coin"     INTEGER     NOT NULL DEFAULT 0 CHECK ( coin >= 0 ),
    "sign"     TEXT,
    "identity" BOOLEAN     NOT NULL DEFAULT FALSE,
    "password" VARCHAR(50) NOT NULL,
    "qq"       VARCHAR(40) UNIQUE,
    "wechat"   VARCHAR(50) UNIQUE
);

DROP TABLE IF EXISTS "user_following" CASCADE;
CREATE TABLE "user_following"
(
    "mid"      BIGINT NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,
    "follower" BIGINT NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,

    UNIQUE ("mid", "follower")
);

CREATE INDEX "user_following_mid_idx" ON "user_following" ("mid");
CREATE INDEX "user_following_follower_idx" ON "user_following" ("follower");

DROP TABLE IF EXISTS "video" CASCADE;
CREATE TABLE "video"
(
    "bv"          VARCHAR(20) PRIMARY KEY,
    "title"       TEXT      NOT NULL CHECK ( LENGTH(title) > 0 ),
    "owner"       BIGINT    NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,
    "commit_time" TIMESTAMP NOT NULL,
    "review_time" TIMESTAMP,
    "public_time" TIMESTAMP,
    "duration"    FLOAT     NOT NULL CHECK ( duration >= 10 ),
    "description" TEXT,
    "reviewer"    BIGINT    REFERENCES "user" ("mid") ON DELETE SET NULL
);

CREATE INDEX "video_owner_idx" ON "video" ("owner");
CREATE INDEX "video_reviewer_idx" ON "video" ("reviewer");

DROP TABLE IF EXISTS "video_like" CASCADE;
CREATE TABLE "video_like"
(
    "bv"  VARCHAR(20) NOT NULL REFERENCES "video" ("bv") ON DELETE CASCADE,
    "mid" BIGINT      NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,

    UNIQUE ("bv", "mid")
);

CREATE INDEX "video_like_bv_idx" ON "video_like" ("bv");
CREATE INDEX "video_like_mid_idx" ON "video_like" ("mid");

DROP TABLE IF EXISTS "video_coin" CASCADE;
CREATE TABLE "video_coin"
(
    "bv"  VARCHAR(20) NOT NULL REFERENCES "video" ("bv") ON DELETE CASCADE,
    "mid" BIGINT      NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,

    UNIQUE ("bv", "mid")
);

CREATE INDEX "video_coin_bv_idx" ON "video_coin" ("bv");
CREATE INDEX "video_coin_mid_idx" ON "video_coin" ("mid");

DROP TABLE IF EXISTS "video_fav" CASCADE;
CREATE TABLE "video_fav"
(
    "bv"  VARCHAR(20) NOT NULL REFERENCES "video" ("bv") ON DELETE CASCADE,
    "mid" BIGINT      NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,

    UNIQUE ("bv", "mid")
);

CREATE INDEX "video_fav_bv_idx" ON "video_fav" ("bv");
CREATE INDEX "video_fav_mid_idx" ON "video_fav" ("mid");

DROP TABLE IF EXISTS "video_view" CASCADE;
CREATE TABLE "video_view"
(
    "bv"   VARCHAR(20) NOT NULL REFERENCES "video" ("bv") ON DELETE CASCADE,
    "mid"  BIGINT      NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,
    "time" FLOAT       NOT NULL,

    UNIQUE ("bv", "mid")
);

CREATE INDEX "video_view_bv_idx" ON "video_view" ("bv");
CREATE INDEX "video_view_mid_idx" ON "video_view" ("mid");

DROP TABLE IF EXISTS "danmu" CASCADE;
CREATE TABLE "danmu"
(
    "id"        BIGSERIAL PRIMARY KEY,
    "bv"        VARCHAR(20)  NOT NULL REFERENCES "video" ("bv") ON DELETE CASCADE,
    "mid"       BIGINT       NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,
    "time"      FLOAT        NOT NULL,
    "content"   VARCHAR(300) NOT NULL CHECK ( LENGTH(content) > 0 ),
    "post_time" TIMESTAMP    NOT NULL
);

CREATE INDEX "danmu_bv_idx" ON "danmu" ("bv");
CREATE INDEX "danmu_mid_idx" ON "danmu" ("mid");

DROP TABLE IF EXISTS "danmu_like" CASCADE;
CREATE TABLE "danmu_like"
(
    "danmu" BIGINT NOT NULL REFERENCES "danmu" ("id") ON DELETE CASCADE,
    "mid"   BIGINT NOT NULL REFERENCES "user" ("mid") ON DELETE CASCADE,

    UNIQUE ("danmu", "mid")
);

CREATE INDEX "danmu_like_danmu_idx" ON "danmu_like" ("danmu");
CREATE INDEX "danmu_like_mid_idx" ON "danmu_like" ("mid");

CREATE OR REPLACE FUNCTION auth(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50)
) RETURNS BIGINT AS
$$
BEGIN
    IF qq_ IS NOT NULL THEN
        RETURN (SELECT u.mid FROM "user" u WHERE u.qq = qq_);
    ELSIF wechat_ IS NOT NULL THEN
        RETURN (SELECT u.mid FROM "user" u WHERE u.wechat = wechat_);
    ELSE
        RETURN (SELECT u.mid FROM "user" u WHERE u.mid = mid_ AND u.password = password_);
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION send_danmu(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20),
    content_ VARCHAR(300),
    time_ FLOAT
) RETURNS BIGINT AS
$$
DECLARE
    sender   BIGINT;
    danmu_id BIGINT;
BEGIN
    sender := auth(mid_, password_, qq_, wechat_);
    IF sender IS NULL THEN
        RETURN -1;
    END IF;

    IF (SELECT v.public_time > NOW() OR v.review_time IS NULL
        FROM video v
        WHERE v.bv = bv_) THEN
        RETURN -1;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM video_view vv WHERE vv.bv = bv_ AND vv.mid = sender) THEN
        RETURN -1;
    END IF;

    IF time_ NOT BETWEEN 0 AND (SELECT v.duration FROM video v WHERE v.bv = bv_) THEN
        RETURN -1;
    END IF;

    INSERT INTO danmu (bv, mid, time, content, post_time)
    VALUES (bv_, sender, time_, content_, NOW())
    RETURNING id INTO danmu_id;

    RETURN danmu_id;

EXCEPTION
    WHEN OTHERS THEN
        RETURN -1;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION display_danmu(
    bv_ VARCHAR(20),
    start_ FLOAT,
    end_ FLOAT,
    filter_ BOOLEAN
) RETURNS SETOF BIGINT AS
$$
BEGIN
    IF (SELECT v.public_time > NOW() OR v.review_time IS NULL OR v.duration < end_ OR start_ < 0
        FROM video v
        WHERE v.bv = bv_) THEN
        RETURN;
    ELSE
        IF NOT filter_ THEN
            RETURN QUERY
                SELECT d.id
                FROM danmu d
                WHERE d.bv = bv_
                  AND d.time >= start_
                  AND d.time <= end_
                ORDER BY d.time;
        ELSE
            RETURN QUERY
                SELECT res.id
                FROM (SELECT DISTINCT ON (d.content) d.id, d.time
                      FROM danmu d
                      WHERE d.bv = bv_
                        AND d.time >= start_
                        AND d.time <= end_
                      ORDER BY d.content, d.post_time) res
                ORDER BY res.time;
        END IF;
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION like_danmu(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    danmu_ BIGINT
) RETURNS BOOLEAN AS
$$
DECLARE
    liker BIGINT;
BEGIN
    liker := auth(mid_, password_, qq_, wechat_);
    IF liker IS NULL THEN
        RETURN FALSE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM danmu d WHERE d.id = danmu_) THEN
        RETURN FALSE;
    END IF;

    IF EXISTS (SELECT 1 FROM danmu_like dl WHERE dl.danmu = danmu_ AND dl.mid = liker) THEN
        DELETE FROM danmu_like dl WHERE dl.danmu = danmu_ AND dl.mid = liker;
        RETURN FALSE;
    ELSE
        IF NOT EXISTS (SELECT 1
                       FROM video_view vv
                       WHERE vv.bv = (SELECT d.bv FROM danmu d WHERE d.id = danmu_)
                         AND vv.mid = liker) THEN
            RETURN FALSE;
        END IF;

        INSERT INTO danmu_like (danmu, mid) VALUES (danmu_, liker);
        RETURN TRUE;
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION recommend_next_video(
    bv_ VARCHAR(50)
) RETURNS SETOF VARCHAR(20) AS
$$
BEGIN
    RETURN QUERY
        SELECT v2.bv
        FROM video_view v1
                 JOIN video_view v2 ON v1.mid = v2.mid
        WHERE v1.bv = bv_
          AND v2.bv != bv_
        GROUP BY v2.bv
        ORDER BY COUNT(1) DESC, v2.bv
        LIMIT 5;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION general_rec(
    size_ INTEGER,
    page_ INTEGER
) RETURNS SETOF VARCHAR(20) AS
$$
BEGIN
    RETURN QUERY
        WITH view_count AS (SELECT vv.bv, COUNT(1) AS count
                            FROM video_view vv
                            GROUP BY vv.bv),
             like_count AS (SELECT vl.bv, COUNT(1) AS count
                            FROM video_like vl
                            GROUP BY vl.bv),
             coin_count AS (SELECT vc.bv, COUNT(1) AS count
                            FROM video_coin vc
                            GROUP BY vc.bv),
             fav_count AS (SELECT vf.bv, COUNT(1) AS count
                           FROM video_fav vf
                           GROUP BY vf.bv),
             danmu_count AS (SELECT d.bv, COUNT(1) AS count
                             FROM danmu d
                             GROUP BY d.bv),
             finish_rate AS (SELECT v.bv, AVG(vv.time / v.duration) AS rate
                             FROM video v
                                      JOIN video_view vv ON v.bv = vv.bv
                             GROUP BY v.bv)
        SELECT res.bv
        FROM (SELECT v.bv,
                     LEAST(COALESCE(like_count.count * 1.0 / view_count.count, 0), 1)
                         + LEAST(COALESCE(coin_count.count * 1.0 / view_count.count, 0), 1)
                         + LEAST(COALESCE(fav_count.count * 1.0 / view_count.count, 0), 1)
                         + COALESCE(danmu_count.count * 1.0 / view_count.count, 0)
                         + COALESCE(finish_rate.rate, 0) score
              FROM video v
                       LEFT JOIN view_count ON v.bv = view_count.bv
                       LEFT JOIN like_count ON v.bv = like_count.bv
                       LEFT JOIN coin_count ON v.bv = coin_count.bv
                       LEFT JOIN fav_count ON v.bv = fav_count.bv
                       LEFT JOIN danmu_count ON v.bv = danmu_count.bv
                       LEFT JOIN finish_rate ON v.bv = finish_rate.bv) res
        ORDER BY res.score DESC
        LIMIT size_ OFFSET (page_ - 1) * size_;

EXCEPTION
    WHEN OTHERS THEN
        RETURN;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION rec_for_user(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    size_ INTEGER,
    page_ INTEGER
) RETURNS SETOF VARCHAR(20) AS
$$
DECLARE
    target   BIGINT;
    identity BOOLEAN;
BEGIN
    target := auth(mid_, password_, qq_, wechat_);
    IF target IS NULL THEN
        RETURN;
    ELSE
        identity := (SELECT u.identity FROM "user" u WHERE u.mid = target);

        CREATE TEMPORARY TABLE friends ON COMMIT DROP AS
        SELECT uf1.follower AS mid
        FROM user_following uf1
                 JOIN user_following uf2 ON uf1.follower = uf2.mid AND uf1.mid = uf2.follower
        WHERE uf1.mid = target;

        CREATE TEMPORARY TABLE options ON COMMIT DROP AS
        SELECT DISTINCT vv.bv
        FROM video_view vv
                 JOIN friends f ON vv.mid = f.mid
        EXCEPT
        SELECT vv.bv
        FROM video_view vv
                 JOIN video v2 ON vv.bv = v2.bv
        WHERE vv.mid = target
           OR (v2.review_time IS NULL AND NOT identity)
           OR ((v2.public_time IS NULL OR v2.public_time > NOW()) AND NOT identity);

        IF NOT EXISTS (SELECT 1 FROM options) THEN
            RETURN QUERY
                SELECT general_rec(size_, page_);
        ELSE
            RETURN QUERY
                SELECT o.bv
                FROM options o
                         JOIN video v ON o.bv = v.bv
                ORDER BY (SELECT COUNT(1)
                          FROM video_view vv
                          WHERE vv.bv = o.bv
                            AND vv.mid IN (SELECT f.mid FROM friends f)) DESC,
                         (SELECT u.level
                          FROM "user" u
                                   JOIN video v ON u.mid = v.owner
                          WHERE v.bv = o.bv) DESC,
                         v.public_time DESC
                LIMIT size_ OFFSET (page_ - 1) * size_;
        END IF;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RETURN;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION rec_friends(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    size_ INTEGER,
    page_ INTEGER
) RETURNS SETOF BIGINT AS
$$
DECLARE
    target BIGINT;
BEGIN
    target := auth(mid_, password_, qq_, wechat_);
    IF target IS NULL THEN
        RETURN;
    ELSE
        RETURN QUERY
            WITH my_followings AS (SELECT uf.mid FROM user_following uf WHERE uf.follower = target)
            SELECT res.follower
            FROM (SELECT uf.follower, COUNT(1) AS count
                  FROM user_following uf
                           JOIN my_followings mf ON uf.mid = mf.mid
                  WHERE uf.follower NOT IN (SELECT mf.mid FROM my_followings mf)
                    AND uf.follower != target
                  GROUP BY uf.follower) res
                     JOIN "user" u ON res.follower = u.mid
            ORDER BY res.count DESC, u.level DESC, u.mid
            LIMIT size_ OFFSET (page_ - 1) * size_;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RETURN;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION register(
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    name_ TEXT,
    sex_ VARCHAR(4),
    birthday_ VARCHAR(20),
    sign_ TEXT
) RETURNS BIGINT AS
$$
DECLARE
    rgmid BIGINT;
BEGIN
    rgmid := EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 10000000 + RANDOM() * 100000;
    INSERT INTO "user" (mid, name, sex, birthday, sign, password, qq, wechat)
    VALUES (rgmid, name_, sex_, birthday_, sign_, password_, qq_, wechat_);
    RETURN rgmid;

EXCEPTION
    WHEN OTHERS THEN
        RETURN -1;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_account(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    target_ BIGINT
) RETURNS BOOLEAN AS
$$
DECLARE
    deleter         BIGINT;
    identity        BOOLEAN;
    target_identity BOOLEAN;
BEGIN
    deleter := auth(mid_, password_, qq_, wechat_);
    IF deleter IS NULL THEN
        RETURN FALSE;
    END IF;
    IF target_ = deleter THEN
        DELETE FROM "user" u WHERE u.mid = deleter;
        RETURN TRUE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM "user" u WHERE u.mid = target_) THEN
        RETURN FALSE;
    END IF;

    identity := (SELECT u.identity FROM "user" u WHERE u.mid = deleter);
    IF NOT identity THEN
        RETURN FALSE;
    END IF;

    target_identity := (SELECT u.identity FROM "user" u WHERE u.mid = target_);
    IF target_identity THEN
        RETURN FALSE;
    END IF;

    DELETE FROM "user" u WHERE u.mid = target_;
    RETURN TRUE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION follow(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    target_ BIGINT
) RETURNS BOOLEAN AS
$$
DECLARE
    operator BIGINT;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;
    IF target_ = operator THEN
        RETURN FALSE;
    END IF;

    IF EXISTS (SELECT 1
               FROM user_following uf
               WHERE uf.mid = target_
                 AND uf.follower = operator) THEN
        DELETE FROM user_following uf WHERE uf.mid = target_ AND uf.follower = operator;
        RETURN FALSE;
    ELSE
        INSERT INTO user_following (mid, follower) VALUES (target_, operator);
        RETURN TRUE;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION user_info(mid_ BIGINT)
    RETURNS TABLE
            (
                mid       BIGINT,
                coin      INTEGER,
                following BIGINT[],
                follower  BIGINT[],
                watched   VARCHAR(20)[],
                liked     VARCHAR(20)[],
                collected VARCHAR(20)[],
                posted    VARCHAR(20)[]
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT u.mid,
               u.coin,
               (SELECT ARRAY_AGG(uf.mid) FROM user_following uf WHERE uf.follower = u.mid),
               (SELECT ARRAY_AGG(uf.follower) FROM user_following uf WHERE uf.mid = u.mid),
               (SELECT ARRAY_AGG(vv.bv) FROM video_view vv WHERE vv.mid = u.mid),
               (SELECT ARRAY_AGG(vl.bv) FROM video_like vl WHERE vl.mid = u.mid),
               (SELECT ARRAY_AGG(vf.bv) FROM video_fav vf WHERE vf.mid = u.mid),
               (SELECT ARRAY_AGG(v.bv) FROM video v WHERE v.owner = u.mid)
        FROM "user" u
        WHERE u.mid = mid_;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION post_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    title_ TEXT,
    description_ TEXT,
    duration_ FLOAT,
    public_time_ TIMESTAMP
) RETURNS VARCHAR(20) AS
$$
DECLARE
    owner_ BIGINT;
    rgbv_  VARCHAR(20);
BEGIN
    IF public_time_ IS NULL OR public_time_ < NOW() THEN
        RETURN NULL;
    END IF;

    owner_ := auth(mid_, password_, qq_, wechat_);
    IF owner_ IS NULL THEN
        RETURN NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM video WHERE video.title = title_ AND video.owner = owner_) THEN
        RETURN NULL;
    END IF;

    rgbv_ := (SELECT 'BV' || (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 100000 + RANDOM() * 100)::BIGINT);
    INSERT INTO video (bv, title, owner, commit_time, duration, description, public_time)
    VALUES (rgbv_, title_, mid_, NOW(), duration_, description_, public_time_);
    RETURN rgbv_;

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20)
) RETURNS BOOLEAN AS
$$
DECLARE
    operator  BIGINT;
    owner     BIGINT;
    superuser BOOLEAN;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    owner := (SELECT v.owner FROM video v WHERE v.bv = bv_);
    IF owner IS NULL THEN
        RETURN FALSE;
    END IF;

    IF operator != owner THEN
        superuser := (SELECT u.identity FROM "user" u WHERE u.mid = operator);
        IF NOT superuser THEN
            RETURN FALSE;
        END IF;
    END IF;

    DELETE FROM video v WHERE v.bv = bv_;
    RETURN TRUE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20),
    title_ TEXT,
    description_ TEXT,
    public_time_ TIMESTAMP,
    duration_ FLOAT
) RETURNS BOOLEAN AS
$$
DECLARE
    operator      BIGINT;
    owner_        BIGINT;
    reviewed      BOOLEAN;
    current_video RECORD;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    IF public_time_ IS NULL OR public_time_ < NOW() THEN
        RETURN FALSE;
    END IF;

    SELECT * INTO current_video FROM video v WHERE v.bv = bv_;
    IF current_video IS NULL THEN
        RETURN FALSE;
    END IF;

    owner_ := current_video.owner;
    IF owner_ <> operator THEN
        RETURN FALSE;
    END IF;
    IF current_video.duration <> duration_ THEN
        RETURN FALSE;
    END IF;
    IF current_video.title = title_ AND current_video.description = description_ AND
       current_video.public_time = public_time_ THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (SELECT 1 FROM video v WHERE v.title = title_ AND v.owner = owner_ AND v.bv <> bv_) THEN
        RETURN FALSE;
    END IF;

    reviewed := (SELECT v.review_time IS NOT NULL FROM video v WHERE v.bv = bv_);

    UPDATE video v
    SET title       = title_,
        description = description_,
        commit_time = NOW(),
        public_time = public_time_,
        review_time = NULL,
        reviewer    = NULL
    WHERE v.bv = bv_;

    RETURN reviewed;

EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION keyword_match(
    keywords_ TEXT[],
    target_ TEXT
) RETURNS SMALLINT AS
$$
DECLARE
    kwd TEXT;
    cnt INT;
BEGIN
    cnt := 0;
    FOREACH kwd IN ARRAY keywords_
        LOOP
            cnt := cnt +
                   (SELECT (LENGTH(target_) - LENGTH(REPLACE(LOWER(target_), LOWER(kwd), ''))) /
                           LENGTH(kwd));
        END LOOP;

    RETURN cnt;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION search_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    keywords_ TEXT,
    size_ INTEGER,
    page_ INTEGER
) RETURNS SETOF VARCHAR(20) AS
$$
DECLARE
    operator  BIGINT;
    superuser BOOLEAN;
    now_      TIMESTAMP;
    kwd_split TEXT[];
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    now_ := NOW();
    kwd_split := (SELECT ARRAY_AGG(k)
                  FROM UNNEST(STRING_TO_ARRAY(keywords_, ' ')) k
                  WHERE LENGTH(k) > 0);

    IF ARRAY_LENGTH(kwd_split, 1) IS NOT NULL AND operator IS NOT NULL THEN
        superuser := (SELECT u.identity FROM "user" u WHERE u.mid = operator);
        IF NOT superuser THEN
            RETURN QUERY
                WITH visible AS (SELECT v.bv, v.title, v.description, u.name owner_name
                                 FROM video v
                                          JOIN "user" u ON v.owner = u.mid
                                 WHERE v.owner = operator
                                    OR (v.public_time IS NULL OR v.public_time <= now_)
                                     AND v.review_time IS NOT NULL),
                     view_count AS (SELECT vv.bv, COUNT(1) AS count
                                    FROM video_view vv
                                    GROUP BY vv.bv)
                SELECT bv
                FROM (SELECT v.bv,
                             keyword_match(kwd_split, v.title)
                                 + keyword_match(kwd_split, v.description)
                                 + keyword_match(kwd_split, v.owner_name) rel
                      FROM visible v) res
                WHERE rel > 0
                ORDER BY rel DESC,
                         (SELECT count FROM view_count vc WHERE vc.bv = res.bv) DESC
                LIMIT size_ OFFSET (page_ - 1) * size_;
        ELSE
            RETURN QUERY
                WITH visible AS (SELECT v.bv, v.title, v.description, u.name owner_name
                                 FROM video v
                                          JOIN "user" u ON v.owner = u.mid),
                     view_count AS (SELECT vv.bv, COUNT(1) AS count
                                    FROM video_view vv
                                    GROUP BY vv.bv)
                SELECT bv
                FROM (SELECT v.bv,
                             keyword_match(kwd_split, v.title)
                                 + keyword_match(kwd_split, v.description)
                                 + keyword_match(kwd_split, v.owner_name) rel
                      FROM visible v) res
                WHERE rel > 0
                ORDER BY rel DESC,
                         (SELECT count FROM view_count vc WHERE vc.bv = res.bv) DESC
                LIMIT size_ OFFSET (page_ - 1) * size_;
        END IF;
    END IF;

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
        RETURN;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION view_rate(
    bv_ VARCHAR(20)
) RETURNS DOUBLE PRECISION AS
$$
BEGIN
    RETURN (SELECT COALESCE(AVG(vv.time / v.duration), -1)
            FROM video v
                     JOIN video_view vv ON v.bv = vv.bv
            WHERE v.bv = bv_);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION hotspot(
    bv_ VARCHAR(20)
) RETURNS SETOF INTEGER AS
$$
BEGIN
    RETURN QUERY
        WITH chunks AS (SELECT TRUNC(time / 10)::INTEGER cid, COUNT(1) cnt
                        FROM danmu
                        WHERE bv = bv_
                        GROUP BY cid)
        SELECT cid
        FROM chunks
        WHERE cnt = (SELECT MAX(cnt) FROM chunks);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION review(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20)
) RETURNS BOOLEAN AS
$$
DECLARE
    operator BIGINT;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    IF EXISTS (SELECT 1 FROM video v WHERE v.bv = bv_ AND (v.review_time IS NOT NULL OR v.owner = operator)) THEN
        RETURN FALSE;
    END IF;

    IF NOT (SELECT u.identity FROM "user" u WHERE u.mid = operator) THEN
        RETURN FALSE;
    END IF;

    UPDATE video v
    SET review_time = NOW(),
        reviewer    = operator
    WHERE v.bv = bv_
      AND v.review_time IS NULL;

    RETURN found;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION coin(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20)
) RETURNS BOOLEAN AS
$$
DECLARE
    operator BIGINT;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    IF NOT EXISTS (SELECT 1
                   FROM video v
                   WHERE v.bv = bv_
                     AND v.owner != operator
                     AND v.review_time IS NOT NULL
                     AND v.public_time <= NOW()) THEN
        RETURN FALSE;
    END IF;

    INSERT INTO video_coin (bv, mid)
    VALUES (bv_, operator);

    UPDATE "user" u
    SET coin = coin - 1
    WHERE u.mid = operator;

    RETURN TRUE;

EXCEPTION
    WHEN OTHERS THEN
        RETURN FALSE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION like_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20)
) RETURNS BOOLEAN AS
$$
DECLARE
    operator BIGINT;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    IF NOT EXISTS (SELECT 1
                   FROM video v
                   WHERE v.bv = bv_
                     AND v.owner != operator
                     AND v.review_time IS NOT NULL
                     AND v.public_time <= NOW()) THEN
        RETURN FALSE;
    END IF;

    IF EXISTS (SELECT 1 FROM video_like vl WHERE vl.bv = bv_ AND vl.mid = operator) THEN
        DELETE FROM video_like vl WHERE vl.bv = bv_ AND vl.mid = operator;
        RETURN FALSE;
    ELSE
        INSERT INTO video_like (bv, mid)
        VALUES (bv_, operator);
        RETURN TRUE;
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fav_vid(
    mid_ BIGINT,
    password_ VARCHAR(50),
    qq_ VARCHAR(40),
    wechat_ VARCHAR(50),
    bv_ VARCHAR(20)
) RETURNS BOOLEAN AS
$$
DECLARE
    operator BIGINT;
BEGIN
    operator := auth(mid_, password_, qq_, wechat_);
    IF operator IS NULL THEN
        RETURN FALSE;
    END IF;

    IF NOT EXISTS (SELECT 1
                   FROM video v
                   WHERE v.bv = bv_
                     AND v.owner != operator
                     AND v.review_time IS NOT NULL
                     AND v.public_time <= NOW()) THEN
        RETURN FALSE;
    END IF;

    IF EXISTS (SELECT 1 FROM video_fav vf WHERE vf.bv = bv_ AND vf.mid = operator) THEN
        DELETE FROM video_fav vf WHERE vf.bv = bv_ AND vf.mid = operator;
        RETURN FALSE;
    ELSE
        INSERT INTO video_fav (bv, mid)
        VALUES (bv_, operator);
        RETURN TRUE;
    END IF;
END
$$ LANGUAGE plpgsql;
