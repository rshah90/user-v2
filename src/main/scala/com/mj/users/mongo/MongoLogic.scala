package com.mj.users.mongo

import java.io.File
import java.util.Date

import com.mj.users.model.{UpdateResult, User, _}
import com.mj.users.mongo.MongoOps._
import com.mj.users.tools.CommonUtils._
import org.apache.commons.io.FileUtils
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.Future

object MongoLogic {
  val colUsersName = "users"
  val colOnlinesName = "onlines"

  val usersCollection: Future[BSONCollection] =
    db.map(_.collection[BSONCollection](colUsersName))
  val onlinesCollection: Future[BSONCollection] =
    db.map(_.collection[BSONCollection](colOnlinesName))


  implicit def sessionStatusHandler = Macros.handler[SessionStatus]
  implicit def userHandler = Macros.handler[User]
  implicit def userStatusHandler = Macros.handler[UserStatus]
  implicit def onlineHandler = Macros.handler[Online]


  val defaultAvatar = getDefaultAvatar

  //create users collection and index
  def createUsersCollection(): Future[String] = {
    val indexSettings = Array(
      //colName, sort, unique, expire
      ("login", 1, true, 0),
      ("nickname", 1, false, 0)
    )
    createIndex(colUsersName, indexSettings)
  }

 //create onlines collection and index
  def createOnlinesCollection(): Future[String] = {
    val indexSettings = Array(
      //colName, sort, unique, expire
      ("uid", 1, true, 0),
      ("dateline", -1, false, 15 * 60)
    )
    createIndex(colOnlinesName, indexSettings)
  }

  //register new user
  def registerUser(login: String,
                   nickname: String,
                   password: String,
                   gender: Int): Future[(String, String)] = {
    var errmsg: String = ""
    if (!isEmail(login)) {
      errmsg = "login must be email"
    } else if (nickname.getBytes.length < 4) {
      errmsg = "nickname must at least 4 charactors"
    } else if (password.length < 6) {
      errmsg = "password must at least 6 charactors"
    } else if (!(gender == 1 || gender == 2)) {
      errmsg = "gender must be boy or girl"
    }
    if (errmsg != "") {
      Future(("", errmsg))
    } else {
      for {
        avatar <- defaultAvatar.map { avatarMap =>
          gender match {
            case 1 => avatarMap("boy")
            case 2 => avatarMap("girl")
            case _ => avatarMap("unknown")
          }
        }
        user <- findCollectionOne[User](usersCollection,
                                        document("login" -> login))
        (uid, errmsg) <- {
          if (user != null) {
            errmsg = "user already exist"
            Future((user._id, errmsg))
          } else {
            val newUser =
              User("", login, nickname, sha1(password), gender, avatar)

            insertCollection[User](usersCollection, newUser)
              .map {
                case (iuid, ierrmsg) =>
                  if (iuid != "") {
                    loginUpdate(iuid)
                  }
                  (iuid, ierrmsg)
              }
          }
        }
      } yield {
        (uid, errmsg)
      }
    }
  }



  def loginAction(login: String, pwd: String): Future[Option[String]] = {
    for {
      user <- findCollectionOne[User](usersCollection,
                                      document("login" -> login))
      uid <- Future {
        Option(user)
          .filter(_.password == sha1(pwd))
          .map(_._id)
          .map(uid => {
            loginUpdate(uid)
            uid
          })
      }
    } yield uid
  }

  def logoutAction(uid: String): Future[UpdateResult] = {
    removeCollection(onlinesCollection, document("uid" -> uid))
  }

  //update user online status
  def updateOnline(uid: String): Future[String] = {
    val selector = document("uid" -> uid)
    for {
      online <- findCollectionOne[Online](onlinesCollection, selector)
      errmsg <- {
        if (online == null) {
          // time expire after 15 minutes
          val onlineNew = Online("", uid, new Date())
          insertCollection[Online](onlinesCollection, onlineNew).map {
            case (id, errmsg) =>
              errmsg
          }
        } else {
          val update = document("$set" -> document("dateline" -> new Date()))
          updateCollection(onlinesCollection, selector, update).map { ur =>
            ur.errmsg
          }
        }
      }
    } yield {
      errmsg
    }
  }

  //when user login, update the loginCount and online info
  def loginUpdate(uid: String): Future[UpdateResult] = {
    for {
      onlineResult <- updateOnline(uid)
      loginResult <- {
        val selector = document("_id" -> uid)
        val update = document(
          "$inc" -> document("loginCount" -> 1)
        )
        updateCollection(usersCollection, selector, update)
      }
    } yield {
      loginResult
    }
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
