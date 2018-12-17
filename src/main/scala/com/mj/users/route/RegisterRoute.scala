package com.mj.users.route

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.mj.users.model.JsonRepo._
import com.mj.users.model.{Consumer, RegisterDto, RegisterDtoResponse, responseMessage}
import com.mj.users.tools.SchedulingValidator
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success}


trait RegisterRoute {
  val registerUserLog = LoggerFactory.getLogger(this.getClass.getName)


  def routeRegister(system: ActorSystem): Route = {

    val registerUserProcessor = system.actorSelection("/*/registerProcessor")
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)
    val kongoConsumerDispatcher = system.actorSelection("/*/kongoConsumerDispatcher")
    path("api" / "registerUser") {
      post {
        entity(as[RegisterDto]) { dto =>
          val validatorResp = SchedulingValidator.validateRegisterUserRequest(dto)
          validatorResp match {
            case Some(response) => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, validatorResp.toJson.toString)))
            case None =>
              val userResponse = registerUserProcessor ? dto
              onComplete(userResponse) {
                case Success(resp) =>
                  resp match {
                    case s: RegisterDtoResponse => {
                      val consumerCreation = (kongoConsumerDispatcher ? Consumer(dto.email)).mapTo[scalaj.http.HttpResponse[String]]
                      onComplete(consumerCreation) {
                        case Success(res) => {
                          complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                        }
                        case Failure(error) => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
                      }
                    }
                    case s: responseMessage =>
                      complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                    case _ => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", resp.toString, "").toJson.toString)))
                  }
                case Failure(error) =>
                  registerUserLog.error("Error is: " + error.getMessage)
                  complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
              }
          }
        }
      }
    }
  }
}
