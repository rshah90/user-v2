package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model._
import com.mj.users.mongo.UserDao._
import com.mj.users.mongo.MongoConnector.search
import com.mj.users.tools.CommonUtils.sha1
import org.slf4j.LoggerFactory
import reactivemongo.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LoginProcessor extends Actor with MessageConfig{

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val wfsRescheduleWOLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (loginDto: LoginDto) => {
      val origin = sender()

      origin ! responseMessage(loginDto.email, "", loginSuccess)

      val result = search[DBRegisterDto](usersCollection, document("registerDto.email" -> loginDto.email))
        .flatMap(user => Future {
          user.filter(_.registerDto.password == sha1(loginDto.password)).map(_._id).map(uid => {
            loginUpdate(uid,loginDto)
            uid
          })
        }).flatMap(uid =>
        uid match {
          case Some(uid) => Future {
            responseMessage(uid, "", loginSuccess)
          }
          case _ => Future {
            responseMessage("", loginFailed, "")
          }
        }
      ).map(response => origin ! response)

      result.recover {
        case e: Throwable => {
          origin ! Some(responseMessage("", e.getMessage, ""))
        }
      }
    }
  }
}
