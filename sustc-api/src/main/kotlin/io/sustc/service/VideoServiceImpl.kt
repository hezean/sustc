package io.sustc.service

import io.sustc.dto.AuthInfo
import io.sustc.dto.PostVideoReq
import io.sustc.util.getMany
import io.sustc.util.getOne
import io.sustc.util.getOneOrNull
import io.sustc.util.setAuth
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class VideoServiceImpl(private val db: DataSource) : VideoService {

    override fun postVideo(auth: AuthInfo, req: PostVideoReq): String? =
        db.getOneOrNull("SELECT post_vid(?,?,?,?,?,?,?,?)", {
            setAuth(auth)
            with(req) {
                setString(5, title)
                setString(6, description)
                setFloat(7, duration)
                setTimestamp(8, publicTime)
            }
        }) {
            getString(1)
        }

    override fun deleteVideo(auth: AuthInfo, bv: String): Boolean =
        db.getOne("SELECT delete_vid(?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
        }) {
            getBoolean(1)
        }

    override fun updateVideoInfo(auth: AuthInfo, bv: String, req: PostVideoReq): Boolean =
        db.getOne("SELECT update_vid(?,?,?,?,?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
            with(req) {
                setString(6, title)
                setString(7, description)
                setTimestamp(8, publicTime)
                setFloat(9, duration)
            }
        }) {
            getBoolean(1)
        }

    override fun searchVideo(auth: AuthInfo, keywords: String, pageSize: Int, pageNum: Int): List<String> =
        db.getMany("SELECT search_vid(?,?,?,?,?,?,?)", {
            setAuth(auth)
            setString(5, keywords)
            setInt(6, pageSize)
            setInt(7, pageNum)
        }) {
            getString(1)
        }

    override fun getAverageViewRate(bv: String): Double =
        db.getOne("SELECT view_rate(?)", {
            setString(1, bv)
        }) {
            getDouble(1)
        }

    override fun getHotspot(bv: String): Set<Int> =
        db.getMany("SELECT hotspot(?)", {
            setString(1, bv)
        }) {
            getInt(1)
        }.toSet()

    override fun reviewVideo(auth: AuthInfo, bv: String): Boolean =
        db.getOne("SELECT review(?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
        }) {
            getBoolean(1)
        }

    override fun coinVideo(auth: AuthInfo, bv: String): Boolean =
        db.getOne("SELECT coin(?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
        }) {
            getBoolean(1)
        }

    override fun likeVideo(auth: AuthInfo, bv: String): Boolean =
        db.getOne("SELECT like_vid(?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
        }) {
            getBoolean(1)
        }

    override fun collectVideo(auth: AuthInfo, bv: String): Boolean =
        db.getOne("SELECT fav_vid(?,?,?,?,?)", {
            setAuth(auth)
            setString(5, bv)
        }) {
            getBoolean(1)
        }
}
