package com.mj.users.dispatcher

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, DiagnosticActorLogging}
import akka.util.Timeout
import com.mj.users.config.Application.kongAdminURL
import com.mj.users.model.Consumer
import com.mj.users.model.JsonRepo._
import scalaj.http.{Http, HttpResponse}
import spray.json._

/*
 *  Created by neluma001c on 8/7/2018
 */

class KongoConsumerDispatcher extends Actor with DiagnosticActorLogging {

  def receive = {
    case consumerName: Consumer =>
      val origin = sender()
      implicit val timeout = Timeout(20, TimeUnit.SECONDS)

      val response = Http(kongAdminURL + "consumers/").header("Content-Type", "application/json").postData(consumerName.toJson.toString).asString
      //val response = "{\n    \"consumer_id\": \"7bce93e1-0a90-489c-c887-d385545f8f4b\",\n    \"created_at\": 1442426001000,\n    \"id\": \"bcbfb45d-e391-42bf-c2ed-94e32946753a\",\n    \"key\": \"a36c3049b36249a3c9f8891cb127243c\",\n    \"secret\": \"e71829c351aa4242c2719cbfbe671c09\"\n}"

      origin ! HttpResponse(response, 200, Map.empty)

  }
}
