package com.mj.users.dispatcher

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, DiagnosticActorLogging}
import akka.util.Timeout
import spray.json._
import scalaj.http.{Http, HttpResponse}
import com.mj.users.config.Application._
import scala.concurrent.ExecutionContext.Implicits.global
/*
 *  Created by neluma001c on 8/7/2018
 */

class JWTCredentialsDispatcher extends Actor  with DiagnosticActorLogging {

  def receive = {
    case consumerName : String =>
      val origin = sender()
      implicit val timeout = Timeout(20, TimeUnit.SECONDS)
      println("consumerName:"+consumerName)

      val response = Http(kongAdminURL +"consumers/" + consumerName + "/jwt").header("Content-Type", "application/json").postData("{}").asString
      /*val response = " {\n            \"created_at\": 1545067504069,\n            \"id\": \"253c64c4-9133-4bac-bdb5-08e6fa2b2d40\",\n            \"algorithm\": \"HS256\",\n            \"secret\": \"S8vpUIGMA7RVpjsnWZPVP1Vj8wwrd30p\",\n            \"key\": \"SDZ0YQzA0wrLzJRmifZOgFY1atV0c8FV\",\n            \"consumer_id\": \"b14aee55-cedc-44db-97b7-4c682d4f8795\"\n        }"
      origin ! HttpResponse(response, 200, Map.empty)*/
      println("response:"+response)
      origin ! response
  }
}
