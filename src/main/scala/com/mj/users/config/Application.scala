package com.mj.users.config

import com.typesafe.config.{Config, ConfigFactory}
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.concurrent.Future

object Application {
  val config: Config = ConfigFactory.load("application.conf")

  val configServer: Config = config.getConfig("server")
  val hostName: String = configServer.getString("hostName")
  val port: Int = configServer.getString("port").toInt
  val akkaPort: Int = configServer.getString("akkaPort").toInt
  val seedNodes: String = configServer.getString("seedNodes")

  val configMongo: Config = config.getConfig("mongodb")
  val configMongoDbname: String = configMongo.getString("dbname")
  var configMongoUri: String = configMongo.getString("uri")

  val configProfileDbName: String = configMongo.getString("configProfileDbName")

  val kongAdminURL = config.getString("kong.admin.url")

  //Mongo configuration
  val dbName = configMongoDbname
  val database_profile = configProfileDbName
  val mongoUri = Application.configMongoUri
  val driver = MongoDriver()
  val parsedUri = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(driver.connection)
  val futureConnection = Future.fromTry(connection)

}
