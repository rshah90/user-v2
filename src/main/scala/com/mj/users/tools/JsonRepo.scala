package com.mj.users.tools

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.mj.users.model._
import spray.json._

object JsonRepo extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val loginDtoFormats: RootJsonFormat[LoginDto] = jsonFormat2(LoginDto)
  implicit val uidDtoFormats: RootJsonFormat[UidDto] = jsonFormat1(UidDto)
  implicit val registerDtoFormats: RootJsonFormat[RegisterDto] = jsonFormat5(
    RegisterDto)
}
