package com.mj.users.model

import java.util.Date

//mongoDB schema
sealed trait BaseMongoObj {
  var _id: String
}

case class User(var _id: String,
                login: String,
                nickname: String,
                password: String,
                gender: Int,
                avatar: String,
                lastLogin: Long = 0,
                loginCount: Int = 0,
                sessionsStatus: List[SessionStatus] = List(),
                dateline: Long = System.currentTimeMillis())
    extends BaseMongoObj

case class SessionStatus(sessionid: String, newCount: Int)

case class UserStatus(uid: String, online: Boolean)



case class Online(var _id: String, uid: String, dateline: Date = new Date())
    extends BaseMongoObj

//mongoDB update result
case class UpdateResult(n: Int, errmsg: String)

