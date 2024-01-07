package io.sustc.service

import io.sustc.dto.AuthInfo
import io.sustc.util.getMany
import io.sustc.util.setAuth
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class RecommenderServiceImpl(private val db: DataSource) : RecommenderService {

    override fun recommendNextVideo(bv: String): List<String> =
        db.getMany("SELECT recommend_next_video(?)", {
            setString(1, bv)
        }) {
            getString(1)
        }

    override fun generalRecommendations(pageSize: Int, pageNum: Int): List<String> =
        db.getMany("SELECT general_rec(?,?)", {
            setInt(1, pageSize)
            setInt(2, pageNum)
        }) {
            getString(1)
        }

    override fun recommendVideosForUser(auth: AuthInfo, pageSize: Int, pageNum: Int): List<String> =
        db.getMany("SELECT rec_for_user(?,?,?,?,?,?)", {
            setAuth(auth)
            setInt(5, pageSize)
            setInt(6, pageNum)
        }) {
            getString(1)
        }

    override fun recommendFriends(auth: AuthInfo, pageSize: Int, pageNum: Int): List<Long> =
        db.getMany("SELECT rec_friends(?,?,?,?,?,?)", {
            setAuth(auth)
            setInt(5, pageSize)
            setInt(6, pageNum)
        }) {
            getLong(1)
        }
}
