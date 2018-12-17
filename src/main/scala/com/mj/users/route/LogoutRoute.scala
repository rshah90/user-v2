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
import com.mj.users.model.{LoginDto, UidDto, responseMessage}
import com.mj.users.tools.SchedulingValidator
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success}


trait LogoutRoute  {
  val logoutUserLog = LoggerFactory.getLogger(this.getClass.getName)


  def routeLogout(system: ActorSystem): Route = {

    val logoutUserProcessor = system.actorSelection("/*/logoutProcessor")
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)

    path("api" / "logoutUser") {
      post {
        entity(as[UidDto]) { dto =>
              val logoutResponse = logoutUserProcessor ? dto
              onComplete(logoutResponse) {
                case Success(resp) =>
                  resp match {
                    case s: responseMessage  => if (s.successmsg.nonEmpty)
                      complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                    else
                      complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                    case _ => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", resp.toString, "").toJson.toString)))
                  }
                case Failure(error) =>
                  logoutUserLog.error("Error is: " + error.getMessage)
                  complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
              }

        }
      }
    }
  }
}
