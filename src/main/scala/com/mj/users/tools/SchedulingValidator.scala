package com.mj.users.tools

import com.mj.users.model.{ LoginDto, RegisterDto, responseMessage}
import com.mj.users.tools.CommonUtils.isEmail
import play.api.libs.json.Json

import scala.concurrent.Future


/**
  * Created by rushabh on 12/11/2018.
  */
object SchedulingValidator {


  /**
    * This method is validate registerUser api
    *
    * @param request RegisterDto request
    * @return ErrorMessage
    */
  def validateRegisterUserRequest(request: RegisterDto): Option[responseMessage] = {
    request match {
      case req if (request.password != request.repassword) => Some(responseMessage("", "password and repassword must be same", ""))
      case req if (!isEmail(request.email)) => Some(responseMessage("", "login must be email", ""))
      case req if (request.nickname.getBytes.length < 4) => Some(responseMessage("", "nickname must at least 4 charactors", ""))
      case req if (request.password.length < 6) => Some(responseMessage("", "password must at least 6 charactors", ""))
      case req if (!(request.gender == 1 || request.gender == 2)) => Some(responseMessage("", "gender must be boy or girl", ""))
      case _ => None
    }
  }

  def validateLoginUserRequest(request: LoginDto): Option[responseMessage] = {
    request match {
      case req if (request.password.length < 6) => Some(responseMessage("", "password must at least 6 charactors", ""))
      case _ => None
    }
  }
}
