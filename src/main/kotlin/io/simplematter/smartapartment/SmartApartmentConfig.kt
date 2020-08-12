package io.simplematter.smartapartment

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

data class SmartApartmentConfig(
  val mqtt: MqttConfig,
  val simulation: Simulation
) {
  companion object {
    fun load(): SmartApartmentConfig {
      val config = ConfigFactory.load()
      return config.extract<SmartApartmentConfig>()
    }
  }
}

data class MqttConfig(
  val host: String,
  val port: Int,
  val topicPrefix: String,
  val qos: Int
)

data class Simulation(
  val start: String
)
