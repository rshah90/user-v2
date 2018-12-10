package com.mj.users.restful

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.mj.users.model._
import com.mj.users.mongo.MongoLogic._
import com.mj.users.restful.RestHandler._
import com.mj.users.tools.JsonRepo._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

object Routes {
  //init create mongodb collection
  createUsersCollection()
  createOnlinesCollection()

  def routeLogic(implicit ec: ExecutionContext,
                 system: ActorSystem,
                 materializer: ActorMaterializer) = {
    routeUserRegister ~
      routeUserLogin ~
      routeUserLogout
  }



  def routeUserRegister(implicit ec: ExecutionContext): Route =
    path("api" / "registerUser") {
      post {
        entity(as[RegisterDto]) { dto =>
          complete {
            registerUserCtl(dto.login,
                            dto.nickname,
                            dto.password,
                            dto.repassword,
                            dto.gender) map { json =>
              HttpEntity(ContentTypes.`application/json`, Json.stringify(json))
            }
          }
        }
      }
    }


  def routeUserLogin(implicit ec: ExecutionContext): Route =
    path("api" / "loginUser") {
      post {
        entity(as[LoginDto]) { dto =>
          complete {
            loginCtl(dto.login, dto.password) map { json =>
              HttpEntity(ContentTypes.`application/json`, Json.stringify(json))
            }
          }
        }
      }
    }

  def routeUserLogout(implicit ec: ExecutionContext): Route =
    path("api" / "logoutUser") {
      post {
        entity(as[UidDto]) { dto =>
          complete {
            logoutCtl(dto.uid) map { json =>
              HttpEntity(ContentTypes.`application/json`, Json.stringify(json))
            }
          }
        }
      }
    }



}
