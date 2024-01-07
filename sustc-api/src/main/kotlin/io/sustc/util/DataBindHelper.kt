package io.sustc.util

import io.sustc.dto.AuthInfo
import java.sql.PreparedStatement

fun PreparedStatement.setAuth(auth: AuthInfo) {
    with(auth) {
        setLong(1, mid)
        setString(2, password)
        setString(3, qq)
        setString(4, wechat)
    }
}
