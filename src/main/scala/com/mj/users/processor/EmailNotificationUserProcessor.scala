package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.responseMessage
import com.mj.users.mongo.UserDao.emailVerification
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class EmailNotificationUserProcessor extends Actor with MessageConfig {

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val wfsRescheduleWOLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (memberID: String) => {
      val origin = sender()
      val result = emailVerification(memberID)
        .flatMap(upResult => Future {
          responseMessage("", "", emailSuccess)
        }).map(response => origin ! response
      )
      result.recover {
        case e: Throwable => {
          origin ! Some(responseMessage("", e.getMessage, ""))
        }

      }
    }

  }
}

