package com.mj.users.mongo

import java.io.File
import java.util.Date

import com.mj.users.model._
import com.mj.users.mongo.MongoConnector._
import com.mj.users.tools.CommonUtils._
import org.apache.commons.io.FileUtils
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.Future

object UserDao {

  val usersCollection: Future[BSONCollection] = db.map(_.collection[BSONCollection]("users"))
  val onlinesCollection: Future[BSONCollection] = db.map(_.collection[BSONCollection]("onlines"))
  val experienceCollection: Future[BSONCollection] = db1.map(_.collection[BSONCollection]("experience"))
  val eductionCollection: Future[BSONCollection] = db1.map(_.collection[BSONCollection]("education"))
  val loginHistoryCollection: Future[BSONCollection] = db.map(_.collection[BSONCollection]("loginHistory"))

  implicit def sessionStatusHandler = Macros.handler[SessionStatus]

  implicit def onlineHandler = Macros.handler[Online]

  implicit def locationHandler = Macros.handler[Location]

  implicit def contactStatusHandler = Macros.handler[ContactInfo]

  implicit def userExperienceHandler = Macros.handler[userExperience]

  implicit def userEducationHandler = Macros.handler[userEducation]

  implicit def registerHandler = Macros.handler[RegisterDto]

  implicit def experienceHandler = Macros.handler[Experience]

  implicit def educationeHandler = Macros.handler[Education]

  implicit def dbRegisterHandler = Macros.handler[DBRegisterDto]

  implicit def loginHistoryRegisterHandler = Macros.handler[loginHistory]

  val defaultAvatar = getDefaultAvatar

  //insert user Details
  def insertUserDetails(userRequest: RegisterDto): Future[RegisterDtoResponse] = {
    for {
      avatar <- defaultAvatar.map { avatarMap =>
        userRequest.gender match {
          case 1 => avatarMap("boy")
          case 2 => avatarMap("girl")
          case _ => avatarMap("unknown")
        }
      }
      prepareUseRequest <- Future {
        userRequest.copy(password = sha1(userRequest.password), repassword = sha1(userRequest.password))
      }
      userData <- Future {
        DBRegisterDto(BSONObjectID.generate().stringify, avatar, prepareUseRequest,None , None, None, None, None, None, None)
      }
      response <- insert[DBRegisterDto](usersCollection, userData).map {
        resp => RegisterDtoResponse(resp._id, resp.registerDto.firstname, resp.registerDto.lastname, resp.registerDto.email)
      }

      
    }
      yield (response)
  }

  def getUserDetails(userRequest: RegisterDto): Future[Option[DBRegisterDto]] = {
    search[DBRegisterDto](usersCollection,
      document("registerDto.email" -> userRequest.email))
  }

  def getUserDetailsById(id: String): Future[Option[DBRegisterDto]] = {
    search[DBRegisterDto](usersCollection,
      document("_id" -> id))

  }


    def updateUserDetails(secondStepRequest: SecondSignupStep): Future[String] = {
    val selector: BSONDocument = if (secondStepRequest.employmentStatus.toInt > 5) {
      BSONDocument("$set" -> BSONDocument("education" -> userEducation(secondStepRequest.school_name,
        secondStepRequest.field_of_study, secondStepRequest.degree,secondStepRequest.start_year, secondStepRequest.end_year,secondStepRequest.activities),
        "interest_on_colony" -> secondStepRequest.interest_on_colony,
        "country" -> secondStepRequest.country,
        "userIP" -> secondStepRequest.userIP,
        "employmentStatus" -> secondStepRequest.employmentStatus,
        "secondSignup_flag" -> true
      ))
    } else {
       BSONDocument("$set" -> BSONDocument("experience" -> userExperience(secondStepRequest.position,secondStepRequest.career_level,
         secondStepRequest.description,secondStepRequest.employer,secondStepRequest.start_month,secondStepRequest.start_year,
         secondStepRequest.end_month,secondStepRequest.end_year,Some(secondStepRequest.current),secondStepRequest.industry),
        "interest_on_colony" -> secondStepRequest.interest_on_colony,
        "country" -> secondStepRequest.country,
        "userIP" -> secondStepRequest.userIP,
        "employmentStatus" -> secondStepRequest.employmentStatus,
        "secondSignup_flag" -> true
      ))

    }

    update(usersCollection, {
      BSONDocument("_id" -> secondStepRequest.memberID)
    }, selector).map(resp => resp)

  }

  def insertLoginHistory(memberId : String , user_Agent : Option[String] , location : Option[Location]) = {
    for {

      userData <- Future {
        loginHistory(memberId ,user_Agent,location)
      }
      response <- insert[loginHistory](loginHistoryCollection, userData)


    }
      yield (response)
  }

  def insertExperienceDetails(secondStepRequest: SecondSignupStep) = {
    if (secondStepRequest.employmentStatus.toInt > 5) {
      Future {
        Education(
          BSONObjectID.generate().stringify,
          secondStepRequest.memberID,
          secondStepRequest.school_name,
          secondStepRequest.field_of_study,
          secondStepRequest.degree,
          secondStepRequest.start_year,
          secondStepRequest.end_year,
          secondStepRequest.activities,
          None,
          None
        )
      }.flatMap(eductionData => insert[Education](eductionCollection, eductionData).map(response => response))

    } else {
      Future {
        Experience(
          BSONObjectID.generate().stringify,
          secondStepRequest.memberID,
          secondStepRequest.position,
          secondStepRequest.career_level,
          secondStepRequest.description,
          secondStepRequest.employer,
          secondStepRequest.start_month,
          secondStepRequest.start_year
          ,secondStepRequest.end_month,secondStepRequest.end_year,None , None ,Some(secondStepRequest.current),secondStepRequest.industry
        )
      }.flatMap(expirenceData => insert[Experience](experienceCollection, expirenceData).map(response => response))
    }
  }


  def updateUserInterestDetails(interestReq: Interest): Future[String] = {

    update(usersCollection, {
      BSONDocument("_id" -> interestReq.memberID)
    }, {
      BSONDocument("$set" -> BSONDocument("interest_flag" -> true),
        "$set" -> BSONDocument("interest" -> interestReq.interest.get.split(",")))
    }).map(resp => resp)

  }


  def updateUserInfoDetails(personalInfo: PersonalInfo): Future[String] = {

    update(usersCollection, {
      BSONDocument("_id" -> personalInfo.memberID)
    }, {
      BSONDocument(
        "$set" -> BSONDocument("registerDto.contact_info" -> personalInfo.contact_info))
    }).map(resp => resp)

  }

  //update user online status
  def updateOnline(uid: String) = {
    val selector = document("uid" -> uid)
    for {
      online <- search[Online](onlinesCollection, selector)
      details <- {
        online match {
          case None => insert[Online](onlinesCollection, Online(BSONObjectID.generate().stringify, uid, new Date()))
          case _ => update(onlinesCollection, selector, document("$set" -> document("dateline" -> new Date())))
        }
      }
    } yield ()
  }

  //when user login, update the loginCount and online info
  def loginUpdate(uid: String, login : LoginDto): Future[String] = {
    for {
      onlineResult <- updateOnline(uid)
      loginResult <- {
        val selector = document("_id" -> uid)
        val updateDoc =  document("$set" -> document(
          "user_agent" -> login.user_agent,
          "registerDto.location" -> login.location) ,
          "$inc" -> document("loginCount" -> 1)
          )
        update(usersCollection, selector, updateDoc)
      }
    } yield {
      loginResult
    }
  }

  def emailVerification(memberID : String): Future[String] ={
    update(usersCollection, {
      BSONDocument("_id" -> memberID)
    }, {
      BSONDocument(
        "$set" -> BSONDocument("email_verification_flag" -> true))
    }).map(resp => resp)
  }

  def getDefaultAvatar: Future[Map[String, String]] = {
    for {
      (idBoy, fileNameBoy, fileTypeBoy, fileSizeBoy, fileMetaDataBoy, errmsgBoy) <- getGridFileMeta(
        document("metadata" -> document("avatar" -> "boy")))
      bsidBoy <- {
        if (fileNameBoy == "") {
          val bytes =
            FileUtils.readFileToByteArray(
              new File("src/main/resources/avatar/boy.jpg"))
          saveGridFile(bytes,
            fileName = "boy.jpg",
            contentType = "image/jpeg",
            metaData = document("avatar" -> "boy")).map(_._1)
        } else {
          Future(idBoy)
        }
      }

      (idGirl,
      fileNameGirl,
      fileTypeGirl,
      fileSizeGirl,
      fileMetaDataGirl,
      errmsgGirl) <- getGridFileMeta(
        document("metadata" -> document("avatar" -> "girl")))
      bsidGirl <- {
        if (fileNameGirl == "") {
          val bytes = FileUtils.readFileToByteArray(
            new File("src/main/resources/avatar/girl.jpg"))
          saveGridFile(bytes,
            fileName = "girl.jpg",
            contentType = "image/jpeg",
            metaData = document("avatar" -> "girl")).map(_._1)
        } else {
          Future(idGirl)
        }
      }

      (idUnknown,
      fileNameUnknown,
      fileTypeUnknown,
      fileSizeUnknown,
      fileMetaDataUnknown,
      errmsgUnknown) <- getGridFileMeta(
        document("metadata" -> document("avatar" -> "unknown")))
      bsidUnknown <- {
        if (fileNameUnknown == "") {
          val bytes = FileUtils.readFileToByteArray(
            new File("src/main/resources/avatar/unknown.jpg"))
          saveGridFile(bytes,
            fileName = "unknown.jpg",
            contentType = "image/jpeg",
            metaData = document("avatar" -> "unknown")).map(_._1)
        } else {
          Future(idUnknown)
        }
      }
    } yield {
      var idBoyStr = ""
      var idGirlStr = ""
      var idUnknownStr = ""
      bsidBoy match {
        case bsid: BSONObjectID =>
          idBoyStr = bsid.stringify
        case _ =>
      }
      bsidGirl match {
        case bsid: BSONObjectID =>
          idGirlStr = bsid.stringify
        case _ =>
      }
      bsidUnknown match {
        case bsid: BSONObjectID =>
          idUnknownStr = bsid.stringify
        case _ =>
      }
      Map(
        "boy" -> idBoyStr,
        "girl" -> idGirlStr,
        "unknow" -> idUnknownStr
      )
    }
  }

}
