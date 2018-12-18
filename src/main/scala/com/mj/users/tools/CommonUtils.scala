package com.mj.users.tools

import java.security.MessageDigest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.time.{Instant, ZoneId, ZonedDateTime}
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

  def setClaims(keyToken: String, expiryPeriodInDays: Long) =
{
  val startDate =Calendar.getInstance()
  val now = Calendar.getInstance()
  now.add(Calendar.MINUTE, 10)
    JwtClaimsSet(
      Map("iss" -> keyToken,
        "iat" -> TimeUnit.MILLISECONDS.toSeconds(startDate.getTimeInMillis),
        "exp" -> TimeUnit.MILLISECONDS.toSeconds(now.getTimeInMillis)
      ))
  }

  def createToken(headerToken: String , payload : String , secret : String): String = {
    println("headerToken:"+headerToken)
    println("payload:"+payload)
    println("secret:"+secret)
    val header =  JwtHeader(headerToken)
    val claimsSet = setClaims(payload , 1)
    JsonWebToken(header, claimsSet, secret)
  }



}
