package io.simplematter.smartapartment.verticles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.simplematter.smartapartment.Event
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import java.io.File
import java.net.URL
import java.time.LocalTime

class ApartmentVerticle : CoroutineVerticle() {

  companion object {
    private val log = LoggerFactory.getLogger(ApartmentVerticle::class.java.name)

    object Config {
      const val apartmentId = "apartment-id"
      const val simulationStart = "simulation-start"
    }

    private val jsonMapper = ObjectMapper().
    registerModule(JavaTimeModule()).
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  private val millisecondsADay = 24 * 60 * 60 * 1000

  private val events: List<Event> by lazy {
    val apartmentId = config.getString(Config.apartmentId)
    val resource: URL = object {}.javaClass.getResource("/adlmr/P$apartmentId.txt")
    File(resource.file).readLines().map { raw ->
      Event.from(raw, apartmentId)
    }
  }

  private fun scheduleEvent(diff: Long, handler: (Event) -> Unit) {
    val simulationTime = LocalTime.now().minusNanos(diff)
    val event = nextEvent(simulationTime)
    val simulationNanos = simulationTime.toNanoOfDay() / 1000_000L
    val eventNanos = event.time.toNanoOfDay() / 1000_000L
    val delay = eventNanos - simulationNanos
    val finalDelay = if (delay < 0) {
      delay + millisecondsADay
    } else {
      delay + 1
    }

    if (finalDelay <= 0) {
      log.warn("Negative delay $finalDelay $delay $simulationTime $simulationNanos ${event.time} $eventNanos")
    }

    vertx.setTimer(finalDelay) { _ ->
      handler(event)
      scheduleEvent(diff, handler)
    }
  }

  override suspend fun start() {
    val simulationStart = config.getString(Config.simulationStart)
    val diff = LocalTime.now().toNanoOfDay() - LocalTime.parse(simulationStart).toNanoOfDay()
    val apartmentId = config.getString(Config.apartmentId)
    try {
      scheduleEvent(diff) { e ->
        vertx.eventBus().publish("/sensors/$apartmentId/${e.sensorId}", jsonMapper.writeValueAsString(e))
      }
    } catch (e: Exception) {
      e.printStackTrace()
      log.info("Apartment $apartmentId will be removed because of initialization error")
      undeployVerticle()
    }
  }

  override suspend fun stop() {
  }

  private fun undeployVerticle() {
    vertx.undeploy(deploymentID)
  }

  private fun nextEvent(time: LocalTime): Event {
    val e: Event? = events.find { event -> event.time > time }
    return e ?: events.first()
  }
}
