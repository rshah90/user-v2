package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.{Interest, PersonalInfo, responseMessage}
import com.mj.users.mongo.UserDao._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

class UpdateInfoProcessor extends Actor with MessageConfig {

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val updateInterestProcessorLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (personalInfo: PersonalInfo) => {
      val origin = sender()
      val result = updateUserInfoDetails(personalInfo)
        .map(updatedResult => origin ! responseMessage(personalInfo.memberID, "", updateInterestSuccess)
        )
      result.recover {
        case e: Throwable => {
          origin ! responseMessage(personalInfo.memberID, e.getMessage, "")
        }
      }
    }
  }
}
