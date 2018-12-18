package com.mj.users.route

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.mj.users.model.JsonRepo._
import com.mj.users.model.{LoginDto, TokenDetails, responseMessage}
import com.mj.users.tools.{CommonUtils, SchedulingValidator}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success}

trait LoginRoute {
  val loginUserLog = LoggerFactory.getLogger(this.getClass.getName)


  def routeLogin(system: ActorSystem): Route = {

    val loginUserProcessor = system.actorSelection("/*/loginProcessor")
    val JWTCredentialsCreation = system.actorSelection("/*/JWTCredentialsCreation")
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)

    path("api" / "loginUser") {
      post {
        entity(as[LoginDto]) { dto =>
          val validatorResp = SchedulingValidator.validateLoginUserRequest(dto)
          validatorResp match {
            case Some(response) => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, validatorResp.toJson.toString)))
            case None =>
              val loginResponse = loginUserProcessor ? dto
              onComplete(loginResponse) {
                case Success(resp) =>
                  resp match {
                    case s: responseMessage => if (s.successmsg.nonEmpty) {
                      val credentials = (JWTCredentialsCreation ? dto.email).mapTo[scalaj.http.HttpResponse[String]]
                      implicit val formats = DefaultFormats
                      onComplete(credentials) {
                        case Success(res) => {
                          res.code match {
                            case 201 => {
                              val parsedResp: TokenDetails = parse(res.body.toString).extract[TokenDetails]
                              val token = CommonUtils.createToken("HS256", parsedResp.key, parsedResp.secret)
                              respondWithHeader(RawHeader("token", token)) {
                                complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                              }

                            }
                            case 400 =>
                              complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", res.body, "").toJson.toString)))
                          }
                        }
                        case Failure(error) => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
                      }
                    } else
                      complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                    case _ => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", resp.toString, "").toJson.toString)))
                  }
                case Failure(error) =>
                  loginUserLog.error("Error is: " + error.getMessage)
                  complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
              }
          }
        }
      }


    }
  }
}
