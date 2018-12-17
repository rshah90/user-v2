package com.mj.users.processor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.mj.users.config.MessageConfig
import com.mj.users.model.{SecondSignupStep, responseMessage}
import com.mj.users.mongo.UserDao._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignupStepsProcessor extends Actor with MessageConfig {

  implicit val timeout = Timeout(500, TimeUnit.SECONDS)
  val signupStepsProcessorLog = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {

    case (secondStepRequest: SecondSignupStep) => {
      val origin = sender()
      val result = updateUserDetails(secondStepRequest)
        .map(updatedResult =>
              Future {
                insertExperienceDetails(secondStepRequest).map(
                  response => origin ! responseMessage(secondStepRequest.memberID, "", secondSignupSuccess)
                )
              }
        )
      result.recover {
        case e: Throwable => {
          origin ! responseMessage(secondStepRequest.memberID, e.getMessage, "")
        }
      }
    }
  }
}
