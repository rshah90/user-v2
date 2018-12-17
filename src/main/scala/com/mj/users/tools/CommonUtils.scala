package com.mj.users.tools

import java.security.MessageDigest
import java.util.concurrent.TimeUnit

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import org.joda.time.DateTime
import reactivemongo.bson.Macros.Annotations.Key


object CommonUtils {

  case class CustomException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)

  def consoleLog(logType: String, msg: String): Unit = {
    val timeStr = new DateTime().toString("yyyy-MM-dd HH:mm:ss")
    println(s"[$logType] $timeStr: $msg")
  }



  def sha1(str: String) =
    MessageDigest
      .getInstance("SHA-1")
      .digest(str.getBytes)
      .map("%02x".format(_))
      .mkString

  def isEmail(email: String): Boolean = {
    """(?=[^\s]+)(?=(\w+)@([\w\.]+))""".r.findFirstIn(email).isDefined
  }

  def setClaims(keyToken: String, expiryPeriodInDays: Long) = JwtClaimsSet(
    Map("iss" -> keyToken/*,
      "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
        .toMillis(expiryPeriodInDays))*/)
  )


  def createToken(headerToken: String , payload : String , secret : String): String = {
    println("headerToken:"+headerToken)
    println("payload:"+payload)
    println("secret:"+secret)
    val header =  JwtHeader(headerToken)
    val claimsSet = setClaims(payload , 1)
    JsonWebToken(header, claimsSet, secret)
  }



}
