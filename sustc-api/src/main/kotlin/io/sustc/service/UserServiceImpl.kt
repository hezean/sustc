package io.sustc.service

import io.sustc.dto.AuthInfo
import io.sustc.dto.RegisterUserReq
import io.sustc.dto.UserInfoResp
import io.sustc.util.getOne
import io.sustc.util.getOneOrNull
import io.sustc.util.isValidDate
import io.sustc.util.setAuth
import org.springframework.stereotype.Service
import java.util.Arrays
import javax.sql.DataSource

@Service
class UserServiceImpl(private val db: DataSource) : UserService {

    override fun register(req: RegisterUserReq): Long {
        if (!req.birthday.isValidDate()) {
            return -1
        }
        return db.getOne("SELECT register(?,?,?,?,?,?,?)", {
            with(req) {
                setString(1, password)
                setString(2, qq)
                setString(3, wechat)
                setString(4, name)
                setString(
                    5, when (sex) {
                        RegisterUserReq.Gender.MALE -> "男"
                        RegisterUserReq.Gender.FEMALE -> "女"
                        else -> "保密"
                    }
                )
                setString(6, birthday)
                setString(7, sign)
            }
        }) {
            getLong(1)
        }
    }

    override fun deleteAccount(auth: AuthInfo, mid: Long): Boolean =
        db.getOne("SELECT delete_account(?,?,?,?,?)", {
            setAuth(auth)
            setLong(5, mid)
        }) {
            getBoolean(1)
        }

    override fun follow(auth: AuthInfo, followeeMid: Long): Boolean =
        db.getOne("SELECT follow(?,?,?,?,?)", {
            setAuth(auth)
            setLong(5, followeeMid)
        }) {
            getBoolean(1)
        }

    @Suppress("UNCHECKED_CAST")
    override fun getUserInfo(mid: Long): UserInfoResp? =
        db.getOneOrNull("SELECT * FROM user_info(?)", {
            setLong(1, mid)
        }) {
            UserInfoResp.builder()
                .mid(getLong("mid"))
                .coin(getInt("coin"))
                .following((getArray("following")?.array as? Array<Long>)?.toLongArray() ?: longArrayOf())
                .follower((getArray("follower")?.array as? Array<Long>)?.toLongArray() ?: longArrayOf())
                .watched(getArray("watched")?.array as? Array<String> ?: emptyArray())
                .liked(getArray("liked")?.array as? Array<String> ?: emptyArray())
                .collected(getArray("collected")?.array as? Array<String> ?: emptyArray())
                .posted(getArray("posted")?.array as? Array<String> ?: emptyArray())
                .build()
        }
}
