package io.sustc.service

import io.sustc.dto.DanmuRecord
import io.sustc.dto.UserRecord
import io.sustc.dto.VideoRecord
import io.sustc.util.batchInsertAutoSplit
import io.sustc.util.execSimple
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class DatabaseServiceImpl(
    private val db: DataSource,
    @Value("\${spring.datasource.hikari.maximum-pool-size:10}") private val maxPoolSize: Int,
) : DatabaseService {

    override fun importData(
        danmuRecords: List<DanmuRecord>,
        userRecords: List<UserRecord>,
        videoRecords: List<VideoRecord>,
    ) = runBlocking {
        db.batchInsertAutoSplit(
            userRecords,
            "INSERT INTO \"user\" VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            chunkSize = userRecords.size / maxPoolSize + 1,
            batchSize = 50,
        ) {
            setLong(1, it.mid)
            setString(2, it.name)
            setString(3, it.sex)
            setString(4, it.birthday)
            setShort(5, it.level)
            setInt(6, it.coin)
            setString(7, it.sign)
            setBoolean(8, it.identity == UserRecord.Identity.SUPERUSER)
            setString(9, it.password)
            setString(10, if (it.qq == "") null else it.qq)
            setString(11, if (it.wechat == "") null else it.wechat)
            addBatch()
        }

        db.batchInsertAutoSplit(
            videoRecords,
            "INSERT INTO video VALUES (?,?,?,?,?,?,?,?,?)",
            chunkSize = videoRecords.size / maxPoolSize + 1,
            batchSize = 50,
        ) {
            setString(1, it.bv)
            setString(2, it.title)
            setLong(3, it.ownerMid)
            setTimestamp(4, it.commitTime)
            setTimestamp(5, it.reviewTime)
            setTimestamp(6, it.publicTime)
            setFloat(7, it.duration)
            setString(8, it.description)
            setLong(9, it.reviewer)
            addBatch()
        }

        db.batchInsertAutoSplit(
            userRecords,
            "INSERT INTO user_following VALUES (?,?)",
            chunkSize = userRecords.size / maxPoolSize + 1,
            batchSize = 100,
        ) {
            it.following.forEach { following ->
                setLong(1, following)
                setLong(2, it.mid)
                addBatch()
            }
        }

        db.batchInsertAutoSplit(
            videoRecords,
            "INSERT INTO video_like VALUES (?,?)",
            chunkSize = videoRecords.size / maxPoolSize + 1,
            batchSize = 100,
        ) {
            it.like.forEach { user ->
                setString(1, it.bv)
                setLong(2, user)
                addBatch()
            }
        }

        db.batchInsertAutoSplit(
            videoRecords,
            "INSERT INTO video_coin VALUES (?,?)",
            chunkSize = videoRecords.size / maxPoolSize + 1,
            batchSize = 100,
        ) {
            it.coin.forEach { user ->
                setString(1, it.bv)
                setLong(2, user)
                addBatch()
            }
        }

        db.batchInsertAutoSplit(
            videoRecords,
            "INSERT INTO video_fav VALUES (?,?)",
            chunkSize = videoRecords.size / maxPoolSize + 1,
            batchSize = 100,
        ) {
            it.favorite.forEach { user ->
                setString(1, it.bv)
                setLong(2, user)
                addBatch()
            }
        }

        db.batchInsertAutoSplit(
            videoRecords,
            "INSERT INTO video_view VALUES (?,?,?)",
            chunkSize = videoRecords.size / maxPoolSize + 1,
            batchSize = 100,
        ) {
            val viewers = it.viewerMids.size
            for (i in 0 until viewers) {
                setString(1, it.bv)
                setLong(2, it.viewerMids[i])
                setFloat(3, it.viewTime[i])
                addBatch()
            }
        }

        for (i in danmuRecords.indices) {
            danmuRecords[i].id = i
        }

        db.batchInsertAutoSplit(
            danmuRecords,
            "INSERT INTO danmu VALUES (?,?,?,?,?,?)",
            chunkSize = danmuRecords.size / maxPoolSize + 1,
            batchSize = 50,
        ) {
            setInt(1, it.id)
            setString(2, it.bv)
            setLong(3, it.mid)
            setFloat(4, it.time)
            setString(5, it.content)
            setTimestamp(6, it.postTime)
            addBatch()
        }

        db.batchInsertAutoSplit(
            danmuRecords,
            "INSERT INTO danmu_like VALUES (?,?)",
            chunkSize = danmuRecords.size / maxPoolSize + 1,
            batchSize = 500,
        ) {
            it.likedBy.forEach { user ->
                setInt(1, it.id)
                setLong(2, user)
                addBatch()
            }
        }

        db.execSimple("ALTER SEQUENCE danmu_id_seq RESTART WITH ${danmuRecords.size + 1}")
    }

    override fun truncate() = db.execSimple(
        """
        DO $$
        DECLARE
            tables CURSOR FOR
                SELECT tablename
                FROM pg_tables
                WHERE schemaname = 'public';
        BEGIN
            FOR t IN tables
            LOOP
                EXECUTE 'TRUNCATE TABLE ' || QUOTE_IDENT(t.tablename) || ' CASCADE;';
            END LOOP;
        END $$
        """.trimIndent()
    )

    override fun sum(a: Int, b: Int): Int = a + b

    override fun getGroupMembers(): List<Int> = listOf(12011323)
}
