package com.mj.users.model

import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

//login api user request
case class LoginDto(email: String, password: String ,  user_agent : Option[String], location: Option[Location])

//logout api user request
case class UidDto(uid: String)

//RegisterDto api user request
case class RegisterDto(email: String, nickname: String, password: String, repassword: String,
                       gender: Int, firstname: String, lastname: String, contact_info: Option[ContactInfo],
                       location:Option[Location], connections: Option[List[String]],
                       connection_requests: Option[List[String]],
                       friends_with_post:Option[List[String]],
                       user_agent : Option[String])

case class loginHistory(memberID : String ,user_agent : Option[String],location:Option[Location])
//RegisterDto api user response
case class RegisterDtoResponse(memberID: String, firstname: String, lastname: String, email: String ,  avatar: String)

//RegisterDto DB user case class
case class DBRegisterDto(var _id : String , avatar: String,
                         registerDto :RegisterDto,
                         experience : Option[userExperience] , /*experience collection*/
                         education: Option[userEducation] , /*education collection*/
                         Interest : Option[List[String]] ,   /*interest details*/
                         userIP : Option[String] ,country : Option[String] ,interest_on_colony : Option[String] , employmentStatus : Option[String]  /*extra fields from second step page*/
                         ,interest_flag: Option[Boolean]= Some(false), secondSignup_flag : Option[Boolean]= Some(false), email_verification_flag : Option[Boolean]= Some(false), /*user prfile flags*/
                         lastLogin: Long = 0, loginCount: Int = 0, sessionsStatus: List[SessionStatus] = List(), dateline: Long = System.currentTimeMillis()
                        ) /*default value*/

case class Location(city: Option[String], state:Option[String], country: Option[String], countryCode: Option[String], lat: Option[Double], lon: Option[Double], ip: Option[String], region: Option[String], regionName: Option[String], timezone: Option[String], zip: Option[String])

case class ContactInfo(address: String, city: String, state: String, country: String, email:Option[String], mobile_phone: Option[String], birth_day:Option[Int], birth_month:Option[Int], birth_year:Option[Int], twitter_profile:Option[String], facebook_profile:Option[String])

//SecondSignupStep api user response
case class SecondSignupStep(memberID: String, country:String, employmentStatus:String,
                            employer:Option[String], position:Option[String],career_level:Option[String], description:Option[String],industry:Option[String], degree:Option[String],
                            school_name:Option[String], field_of_study:Option[String],activities: Option[String], current: Boolean , interest_on_colony: Option[String], userIP: Option[String],
                            updated_date: Option[String] ,start_month:Option[String], start_year:Option[String], end_month:Option[String], end_year:Option[String])

//Experience Collection

case class Experience(expID: String, memberID: String, position:Option[String], career_level:Option[String], description:Option[String], employer: Option[String],
                      start_month:Option[String],
                      start_year:Option[String], end_month:Option[String], end_year:Option[String], created_date: Option[String], updated_date: Option[String],
                      current:Option[Boolean], industry: Option[String]
                     )
case class userExperience(position:Option[String], career_level:Option[String], description:Option[String], employer: Option[String], start_month:Option[String],
                          start_year:Option[String], end_month:Option[String], end_year:Option[String], current:Option[Boolean],
                          industry: Option[String])

//Education Collection
case class Education(eduID: String, memberID: String, school_name:  Option[String], field_of_study: Option[String], degree:  Option[String],
                     start_year: Option[String], end_year: Option[String],activities:  Option[String], created_date:  Option[String], updated_date:Option[String])

case class userEducation(  school_name: Option[String], field_of_study:Option[String], degree: Option[String],
                           start_year:Option[String], end_year:Option[String],activities: Option[String])

case class SessionStatus(sessionid: String, newCount: Int)

//Online Collection
case class Online(var _id: String, uid: String, dateline: Date = new Date())

//Response format for all apis
case class responseMessage(uid: String, errmsg: String , successmsg : String)

//Interest collection for users
case class Interest(memberID: String, interest: Option[String])

//Update Personal Info
case class PersonalInfo(memberID: String, contact_info : ContactInfo)


case class TokenDetails(consumer_id : String , created_at : Long , id : String , key : String , secret : String ,algorithm : Option[String])

case class ConsumerDetails(userName : String , created_at : Long , id : String )

case class listConsumerDetails(total : Long , data : List[TokenDetails])

case class Consumer(username : String)
/*case class GetJobTitle(position: String)

case class GetCompanies(employer: String)*/

object JsonRepo extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val locationFormats: RootJsonFormat[Location] = jsonFormat11(Location)
  implicit val loginDtoFormats: RootJsonFormat[LoginDto] = jsonFormat4(LoginDto)
  implicit val uidDtoFormats: RootJsonFormat[UidDto] = jsonFormat1(UidDto)

  implicit val contactInfoFormats: RootJsonFormat[ContactInfo] = jsonFormat11(ContactInfo)
  implicit val registerDtoFormats: RootJsonFormat[RegisterDto] = jsonFormat13(RegisterDto)
  implicit val errorMessageDtoFormats: RootJsonFormat[responseMessage] = jsonFormat3(responseMessage)
  implicit val registerDtoResponseDtoFormats: RootJsonFormat[RegisterDtoResponse] = jsonFormat5(RegisterDtoResponse)
  implicit val secondSignupStepsFormats: RootJsonFormat[SecondSignupStep] = jsonFormat20(SecondSignupStep)
  implicit val interestFormats: RootJsonFormat[Interest] = jsonFormat2(Interest)
  implicit val personalInfoFormats: RootJsonFormat[PersonalInfo] = jsonFormat2(PersonalInfo)
  implicit val tokenDetailsFormats: RootJsonFormat[TokenDetails] = jsonFormat6(TokenDetails)
  implicit val consumerFormats: RootJsonFormat[Consumer] = jsonFormat1(Consumer)
  implicit val consumerDetailsFormats: RootJsonFormat[ConsumerDetails] = jsonFormat3(ConsumerDetails)
  implicit val consumerDetailsListFormats: RootJsonFormat[listConsumerDetails] = jsonFormat2(listConsumerDetails)

}
