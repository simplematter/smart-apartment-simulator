package io.simplematter.smartapartment

import io.simplematter.smartapartment.verticles.ApartmentVerticle
import io.simplematter.smartapartment.verticles.SensorVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import kotlin.text.padStart

object Service {

  private val log = LoggerFactory.getLogger(Service::class.java)
  private val serviceConfig = SmartApartmentConfig.load()
  private val vertx = Vertx.vertx()

  private val motionSensors = (1..51).toList().map { "M" + it.toString().padStart(2, '0') }
  private val itemSensors = (1..8).toList().map { "I" + it.toString().padStart(2, '0') }
  private val cabinetSensors = (1..12).toList().map { "D" + it.toString().padStart(2, '0') }
  private val temperatureSensors = (1..3).toList().map { "T" + it.toString().padStart(2, '0') }
  private val waterSensors = listOf("AD1-A", "AD1-B")
  private val burnerSensors = listOf("AD1-C")
  private val phoneSensors = listOf("P01")
  private val sensors = motionSensors + itemSensors + cabinetSensors + temperatureSensors + waterSensors + burnerSensors + phoneSensors

  private val apartments = (1..26).toList().map { it.toString().padStart(2, '0') }

  @JvmStatic
  fun main(args: Array<String>) {
    deployApartments()
  }

  private fun deployApartments() {
    for (apartmentId in apartments) {
      deployApartment(apartmentId, serviceConfig)
      deploySensors(apartmentId)
    }
  }

  private fun deployApartment(id: String, serviceConfig: SmartApartmentConfig) {
    val apartmentVerticleConfig = JsonObject()
      .put(ApartmentVerticle.Companion.Config.apartmentId, id)
      .put(ApartmentVerticle.Companion.Config.simulationStart, serviceConfig.simulation.start)
    val deployOptions = DeploymentOptions()
    deployOptions.setConfig(apartmentVerticleConfig)
    deployOptions.isWorker = true
    deployOptions.workerPoolSize = 15

    vertx.deployVerticle(ApartmentVerticle::class.qualifiedName, deployOptions)
  }

  private fun deploySensors(apartmentId: String) {
    for (sensor in sensors) {
      deploySensor(sensor, apartmentId, serviceConfig)
    }
  }

  private fun deploySensor(id: String, apartmentId: String, serviceConfig: SmartApartmentConfig) {
    val sensorVerticleConfig = JsonObject()
      .put(SensorVerticle.Companion.Config.SensorId, id)
      .put(SensorVerticle.Companion.Config.ApartmentId, apartmentId)
      .put(SensorVerticle.Companion.Config.MqttPort, serviceConfig.mqtt.port)
      .put(SensorVerticle.Companion.Config.MqttUrl, serviceConfig.mqtt.host)
      .put(SensorVerticle.Companion.Config.MqttTopicPrefix, serviceConfig.mqtt.topicPrefix)
      .put(SensorVerticle.Companion.Config.QoS, serviceConfig.mqtt.qos)

    val deployOptions = DeploymentOptions()
    deployOptions.config = sensorVerticleConfig
    deployOptions.isWorker = true
    deployOptions.workerPoolSize = 50

    vertx.deployVerticle(SensorVerticle::class.qualifiedName, deployOptions)
  }
}
