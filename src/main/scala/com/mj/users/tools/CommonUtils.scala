package com.mj.users.tools

import java.security.MessageDigest
import java.text.SimpleDateFormat

import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsNumber, JsString, JsValue}

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




}
