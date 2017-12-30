package me.fru1t.streamtools.controller.settings.graph

/** An identifier for a type of statistic used in Graph Stats. */
enum class Statistic(val displayString: String) {
  APM("Action per Minute"),
  PPM("Mouse Movement per Minute")
}
