package io.simplematter.smartapartment

import java.time.LocalDateTime
import java.time.LocalTime

data class Event(
  val time: LocalTime,
  val sensorId: String,
  val value: String,
  val residentId: String,
  val taskId: String,
  val apartment: String
) {
  companion object {

    fun from(string: String, apartment: String): Event {
      // Example of parsed string: 2008-11-10	14:28:17.986759	M22	ON 2 2
      val tokens = string.split("\t")
      val subtokens = tokens[3].split(" ")
      val time = LocalDateTime.parse("${tokens[0]}T${tokens[1]}").toLocalTime()

      return Event(
        time = time,
        sensorId = tokens[2],
        value = subtokens[0],
        residentId = subtokens.getOrElse(1) { "" },
        taskId = subtokens.getOrElse(2) { "" },
        apartment = apartment
      )
    }
  }
}
