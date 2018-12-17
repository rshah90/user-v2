package com.mj.users

import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.mj.users.config.Application
import com.mj.users.config.Application._
import com.mj.users.tools.CommonUtils._
import com.mj.users.tools.RouteUtils
import com.typesafe.config.ConfigFactory

object Server extends App {
  val seedNodesStr = seedNodes
    .split(",")
    .map(s => s""" "akka.tcp://users-cluster@$s" """)
    .mkString(",")

  val inetAddress = InetAddress.getLocalHost
  var configCluster = Application.config.withFallback(
    ConfigFactory.parseString(s"akka.cluster.seed-nodes=[$seedNodesStr]"))

  configCluster = configCluster
    .withFallback(
      ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname=$hostName"))
    .withFallback(
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$akkaPort"))

  implicit val system: ActorSystem = ActorSystem("users-cluster", configCluster)
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  val registerProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.RegisterProcessor]), "registerProcessor")
  val loginProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.LoginProcessor]), "loginProcessor")
  val logoutProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.LogoutProcessor]), "logoutProcessor")
  val sigupStepsProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.SignupStepsProcessor]), "signupStepsProcessor")
  val updateInterestProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.UpdateInterestProcessor]), "updateInterestProcessor")
  val updateInfoProcessor = system.actorOf(RoundRobinPool(20).props(Props[processor.UpdateInfoProcessor]), "updateInfoProcessor")
  val jwtCredentialsDispatcher = system.actorOf(RoundRobinPool(20).props(Props[dispatcher.JWTCredentialsDispatcher]), "JWTCredentialsDispatcher")
  val kongoConsumerDispatcher = system.actorOf(RoundRobinPool(20).props(Props[dispatcher.KongoConsumerDispatcher]), "kongoConsumerDispatcher")


  import system.dispatcher

  Http().bindAndHandle(RouteUtils.logRoute, "0.0.0.0", port)

  consoleLog("INFO",
             s"User server started! Access url: https://$hostName:$port/")
}
