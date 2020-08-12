package io.simplematter.smartapartment

import io.vertx.core.Vertx
import io.vertx.mqtt.MqttClient
import io.vertx.mqtt.MqttClientOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MqttConnect(
  private val vertx: Vertx,
  private val mqttUrl: String,
  private val mqttPort: Int,
  private val clientId: String
) {

  private var client: MqttClient? = null
  private val clientInitMutex = Mutex()

  suspend fun getConnectedMqttClient(): MqttClient {
    return clientInitMutex.withLock {
      val existingClient = client
      if (existingClient != null && existingClient.isConnected) {
        existingClient
      } else {
        val c = MqttClient.create(vertx, MqttClientOptions().setClientId(clientId))
        suspendCoroutine<Unit> { continuation ->
          c.connect(mqttPort, mqttUrl) { result ->
            if (result.succeeded()) {
              continuation.resume(Unit)
            } else {
              continuation.resumeWithException(result.cause())
            }
          }
        }
        c.closeHandler {
        }
        client = c
        c
      }
    }
  }

  suspend fun disconnectMqtt() {
    clientInitMutex.withLock {
      val c = client
      if (c != null) {
        c.disconnect()
        client = null
      }
    }
  }
}
