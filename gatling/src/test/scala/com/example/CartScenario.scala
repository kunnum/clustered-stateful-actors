package com.mread.gatling

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CartScenario extends Simulation {

  private val config =  ConfigFactory.load()

  val baseUrl = config.getString("loadtest.baseUrl")

  val cartIds = Iterator.continually(
    Map("cartId" -> Random.nextInt(10))
  )

  val ids = Iterator.continually(
    Map("id" -> Random.nextInt(100))
  )

  val httpConf = http
    .baseUrl(s"${baseUrl}/cart")

  val scn = scenario("CartScenario")
    .feed(cartIds)
    .feed(ids)
    .exec(
      http("post_item")
      .get("/${cartId}?id=${id}&quantity=10")
      .check(status.is(200))
    )

  setUp(
    scn.inject(rampUsers(100) during (1 minutes))
    .protocols(httpConf)
  )
}
