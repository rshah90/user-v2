package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.{Interest, responseMessage}
import com.mj.users.mongo.UserDao._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

class GetEmployeePositionProcessor extends Actor with MessageConfig {

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val getEmployeePositionProcessor = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (msg: String) => {
      val origin = sender()
      /*val result = updateUserInterestDetails(interestReq)
        .map(updatedResult => origin ! responseMessage("", "", secondSignupSuccess)
        )
      result.recover {
        case e: Throwable => {
          origin ! responseMessage("", e.getMessage, "")
        }
      }*/
    }
  }
}
