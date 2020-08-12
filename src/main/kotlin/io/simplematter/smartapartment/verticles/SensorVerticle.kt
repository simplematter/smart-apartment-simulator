package io.simplematter.smartapartment.verticles

import io.netty.handler.codec.mqtt.MqttQoS
import io.simplematter.smartapartment.MqttConnect
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class SensorVerticle : CoroutineVerticle() {

  private val sensorId by lazy { config.getString(Config.SensorId) }
  private val apartmentId by lazy { config.getString(Config.ApartmentId) }
  private val qos by lazy { MqttQoS.valueOf(config.getInteger(Config.QoS)) }

  companion object {
    private val log = LoggerFactory.getLogger(SensorVerticle::class.java.name)

    object Config {
      const val MqttUrl = "mqtt.url"
      const val MqttPort = "mqtt.port"
      const val MqttTopicPrefix = "mqtt.topic"
      const val SensorId = "sensor-id"
      const val ApartmentId = "apartment-id"
      const val QoS = "qos"
    }
  }

  private val mqttConnect by lazy {
    MqttConnect(
      vertx = vertx,
      mqttUrl = config.getString(Config.MqttUrl),
      mqttPort = config.getInteger(Config.MqttPort),
      clientId = "$deploymentID-apartment-$apartmentId-sensor-$sensorId}"
    )
  }

  override suspend fun start() {
    try {
      log.info("Apartment $apartmentId, sensor $sensorId starts")
      val mqttTopicPrefix = config.getString(Config.MqttTopicPrefix)
      vertx.eventBus().consumer(
        "/sensors/$apartmentId/$sensorId",
        Handler<Message<String>> { message ->
          log.info("Sending event ${message.body()}")
          launch {
            mqttConnect.getConnectedMqttClient().publish(
              "$mqttTopicPrefix/apt1/$sensorId",
              Buffer.buffer(message.body()),
              qos,
              false,
              false
            )
          }
        }
      )
    } catch (e: Exception) {
      e.printStackTrace()
      log.info("$sensorId will be removed because of initialization error")
      undeployVerticle()
    }
  }

  override suspend fun stop() {
    log.info("Sensor $sensorId stops")
    mqttConnect.disconnectMqtt()
  }

  private fun undeployVerticle() {
    vertx.undeploy(deploymentID)
  }
}
