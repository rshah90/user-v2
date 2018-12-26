package com.mj.users.mongo

import java.util.concurrent.Executors

import com.mj.users.config.Application._
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Neo4jConnector {

  implicit val ec: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(50))

  val driver = GraphDatabase.driver(neo4jUrl, AuthTokens.basic(neo4jUsername, neo4jPassword))

  //insert single document into collection
  /**
    * @param futureCollection : Future[BSONCollection], collection to insert
    * @param record           : T, record is BaseMongoObj
    * @return Future[(id: String, errmsg: String)], inserted id string and errmsg
    */
  def connectNeo4j(script: String): Future[Int] = {

    val insertResult = for {
      session <- Future {
        driver.session()
      }
      result <- Future {
        session.run(script)
      }
    } yield {
      println("result"+result.consume().counters().nodesCreated()  )
      session.close()
      result.consume().counters().nodesCreated()

    }
    insertResult.recover {
      case e: Throwable =>
        println("e"+e.getMessage)
        throw new Exception("Neo4j DB Error")
    }
  }


  def updateNeo4j(script: String): Future[Int] = {

    val insertResult = for {
      session <- Future {
        driver.session()
      }
      result <- Future {
        session.run(script)
      }
    } yield {
      println("result"+result.consume().counters().propertiesSet()  )
      session.close()
      result.consume().counters().propertiesSet()

    }
    insertResult.recover {
      case e: Throwable =>
        println("e"+e.getMessage)
        throw new Exception("Neo4j DB Error")
    }
  }

  def getNeo4j(script: String) = {

    val insertResult = for {
      session <- Future {
        driver.session()
      }
      result <- Future {
        session.run(script)
      }
    } yield {
      session.close()
      result

    }
    insertResult.recover {
      case e: Throwable =>
        println("e"+e.getMessage)
        throw new Exception("Neo4j DB Error")
    }
  }

}