package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.{Interest, SecondSignupStep, responseMessage}
import com.mj.users.mongo.UserDao._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpdateInterestProcessor extends Actor with MessageConfig {

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val updateInterestProcessorLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (interestReq: Interest) => {
      val origin = sender()
      val result = updateUserInterestDetails(interestReq)
        .map(updatedResult => origin ! responseMessage(interestReq.memberID, "", updateInterestSuccess)
        )
      result.recover {
        case e: Throwable => {
          origin ! responseMessage(interestReq.memberID, e.getMessage, "")
        }
      }
    }
  }
}
