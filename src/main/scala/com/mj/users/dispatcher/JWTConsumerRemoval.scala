package com.mj.users.dispatcher

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, DiagnosticActorLogging}
import akka.util.Timeout
import com.mj.users.config.Application._
import scalaj.http.{Http, HttpResponse}
import akka.util.Timeout
import com.mj.users.model._
import com.mj.users.tools.{CommonUtils, SchedulingValidator}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import com.mj.users.mongo.UserDao.getUserDetailsById

import scala.util.{Failure, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/*
 *  Created by neluma001c on 8/7/2018
 */

class JWTConsumerRemoval extends Actor  with DiagnosticActorLogging {

  def receive = {
    case (uid : String , consumerId : String) =>
      val origin = sender()
      implicit val timeout = Timeout(20, TimeUnit.SECONDS)

      val userDetails = for{userDbDetails <- getUserDetailsById(uid)}yield(userDbDetails)

      implicit val formats = DefaultFormats
      val result = for{
        user <- userDetails
        response <- Future{Http(kongAdminURL +"consumers/" + user.get.registerDto.email + "/jwt").header("Content-Type", "application/json").asString}
        parsedResp <- Future{parse(response.body.toString).extract[listConsumerDetails]}
        id <- Future{parsedResp.data.filter(details => details.consumer_id == consumerId).head.id}
        resp <- Future{Http(kongAdminURL +"consumers/" + user.get.registerDto.email + "/jwt/" + id).method("DELETE").header("Content-Type", "application/json").asString}
        _ = origin ! resp
      } yield()


      result.recover {
        case e: Throwable =>
          origin !  HttpResponse(e.getMessage, 400, Map.empty)
       }
    /*  val response = Http(kongAdminURL +"consumers/" + consumerName + "/jwt").header("Content-Type", "application/json").asString


      val parsedResp: ConsumerDetails = parse(response.body.toString).extract[ConsumerDetails]

      val resp = Http(kongAdminURL +"consumers/" + consumerName + "/jwt" + parsedResp.id).header("Content-Type", "application/json").postData("").option(HttpOptions.method("DELETE"))
*/
      /*val response = " {\n            \"created_at\": 1545067504069,\n            \"id\": \"253c64c4-9133-4bac-bdb5-08e6fa2b2d40\",\n            \"algorithm\": \"HS256\",\n            \"secret\": \"S8vpUIGMA7RVpjsnWZPVP1Vj8wwrd30p\",\n            \"key\": \"SDZ0YQzA0wrLzJRmifZOgFY1atV0c8FV\",\n            \"consumer_id\": \"b14aee55-cedc-44db-97b7-4c682d4f8795\"\n        }"
      origin ! HttpResponse(response, 200, Map.empty)*/
  /*    println("response:"+resp)
      origin ! resp*/
  }
}
