package io.sustc.service

import io.sustc.dto.AuthInfo
import io.sustc.util.getMany
import io.sustc.util.getOne
import io.sustc.util.setAuth
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class DanmuServiceImpl(private val db: DataSource) : DanmuService {

    override fun sendDanmu(auth: AuthInfo, bv: String, content: String?, time: Float): Long =
        db.getOne("SELECT send_danmu(?,?,?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
            setString(6, content)
            setFloat(7, time)
        }) {
            getLong(1)
        }

    override fun displayDanmu(bv: String, timeStart: Float, timeEnd: Float, filter: Boolean): List<Long> =
        db.getMany("SELECT display_danmu(?,?,?,?)", {
            setString(1, bv)
            setFloat(2, timeStart)
            setFloat(3, timeEnd)
            setBoolean(4, filter)
        }) {
            getLong(1)
        }

    override fun likeDanmu(auth: AuthInfo, id: Long): Boolean =
        db.getOne("SELECT like_danmu(?,?,?,?,?)", {
            setAuth(auth)
            setLong(5, id)
        }) {
            getBoolean(1)
        }
}
