package com.mj.users.restful


import com.mj.users.mongo.MongoLogic._
import com.mj.users.tools.CommonUtils._
import play.api.libs.json._
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object RestHandler {
  def registerUserCtl(
                       login: String,
                       nickname: String,
                       password: String,
                       repassword: String,
                       gender: Int)(implicit ec: ExecutionContext): Future[JsObject] = {
    if (password != repassword) {
      Future {
        Json.obj(
          "uid" -> "",
          "errmsg" -> s"password and repassword must be same",
          "successmsg" -> ""
        )
      }
    } else {
      registerUser(login, nickname, password, gender).map {
        case (uid, errmsg) =>
          var successmsg = ""
          if (uid != "") {
            successmsg = "register user success, thank you for join us"
          }
          Json.obj(
            "uid" -> uid,
            "errmsg" -> errmsg,
            "successmsg" -> successmsg
          )
      }
    }
  }


  def loginCtl(login: String, password: String)(
    implicit ec: ExecutionContext): Future[JsObject] = {
    if (password.length < 6) {
      Future {
        Json.obj(
          "uid" -> "",
          "errmsg" -> s"password must at least 6 characters",
          "successmsg" -> "",
          "userToken" -> ""
        )
      }
    } else {
      loginAction(login, password).map {
        case Some(uid) =>
          Json.obj(
            "uid" -> uid,
            "errmsg" -> "",
            "successmsg" -> "login in success"
          )
        case _ =>
          Json.obj(
            "uid" -> "",
            "errmsg" -> "user not exist or password not match",
            "successmsg" -> ""
          )
      }
    }
  }

  def logoutCtl(uid: String)(
    implicit ec: ExecutionContext): Future[JsObject] = {
    logoutAction(uid).map(res => Option(res.errmsg).filterNot(_.isEmpty)).map {
      case Some(errmsg) =>
        Json.obj(
          "errmsg" -> errmsg,
          "successmsg" -> ""
        )
      case _ =>
        Json.obj(
          "errmsg" -> "",
          "successmsg" -> "logout success"
        )
    }
  }



}
