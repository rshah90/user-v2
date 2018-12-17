package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.{UidDto, responseMessage}
import com.mj.users.mongo.UserDao.onlinesCollection
import com.mj.users.mongo.MongoConnector.remove
import org.slf4j.LoggerFactory
import reactivemongo.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class LogoutProcessor extends Actor with MessageConfig{

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val wfsRescheduleWOLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (logoutDto: UidDto) => {
      val origin = sender()
      val result = remove(onlinesCollection, document("uid" -> logoutDto.uid))
        .flatMap(upResult => Future{responseMessage("", "", logoutSuceess)}).map(response => origin ! response)

      result.recover {
        case e: Throwable => {
          origin ! Some(responseMessage("", e.getMessage, ""))
        }
      }
    }
  }
}
