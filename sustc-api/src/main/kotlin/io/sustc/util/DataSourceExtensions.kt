@file:Suppress("SqlSourceToSinkFlow")

package io.sustc.util

import kotlinx.coroutines.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

@PublishedApi
internal class AutoExecPreparedStatement(
    private val ps: PreparedStatement,
    private val batchSize: Int
) : PreparedStatement by ps {

    private var batchCount = 0

    override fun addBatch() {
        ps.addBatch()
        if (++batchCount % batchSize == 0) {
            ps.executeBatch()
        }
    }
}

fun DataSource.execSimple(sql: String) {
    connection.use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }
}

inline fun <T> DataSource.getOneOrNull(
    sql: String,
    crossinline dataBind: PreparedStatement.() -> Unit,
    crossinline dataExtract: ResultSet.() -> T,
): T? = connection.use { conn ->
    conn.prepareStatement(sql).use { stmt ->
        stmt.dataBind()
        stmt.executeQuery().use { rs ->
            if (rs.next()) rs.dataExtract()
            else null
        }
    }
}

inline fun <T> DataSource.getOne(
    sql: String,
    crossinline dataBind: PreparedStatement.() -> Unit,
    crossinline dataExtract: ResultSet.() -> T,
): T = getOneOrNull(sql, dataBind, dataExtract) ?: throw NoSuchElementException()

inline fun <T> DataSource.getMany(
    sql: String,
    crossinline dataBind: PreparedStatement.() -> Unit,
    crossinline dataExtract: ResultSet.() -> T,
): List<T> = connection.use { conn ->
    conn.prepareStatement(sql).use { stmt ->
        stmt.dataBind()
        stmt.executeQuery().use { rs ->
            val list = mutableListOf<T>()
            while (rs.next()) {
                list.add(rs.dataExtract())
            }
            list
        }
    }
}

inline fun <T> DataSource.batchInsert(
    data: Iterable<T>,
    sql: String,
    batchSize: Int,
    crossinline dataBind: PreparedStatement.(T) -> Unit
) {
    connection.use { conn ->
        conn.autoCommit = false
        try {
            AutoExecPreparedStatement(conn.prepareStatement(sql), batchSize).use { stmt ->
                data.forEach { item ->
                    stmt.dataBind(item)
                }
                stmt.executeBatch()
            }
            conn.commit()
        } catch (e: SQLException) {
            conn.rollback()
            throw e
        }
    }
}

suspend fun <T> DataSource.batchInsertAutoSplit(
    data: List<T>,
    sql: String,
    chunkSize: Int,
    batchSize: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    dataBind: PreparedStatement.(T) -> Unit,
) = withContext(dispatcher) {
    val chunksNum = data.size / chunkSize
    val chunkRanges = (0 until chunksNum)
        .map { it * chunkSize..(it + 1) * chunkSize } as MutableList<IntRange>
    chunkRanges += ((chunksNum * chunkSize)..data.size)
    chunkRanges.map {
        async { batchInsert(data.subList(it.first, it.last), sql, batchSize, dataBind = dataBind) }
    }.awaitAll()
}
