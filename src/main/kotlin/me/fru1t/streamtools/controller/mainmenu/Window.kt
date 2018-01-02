package me.fru1t.streamtools.controller.mainmenu

/** Used to store window data for saving and loading in JSON. */
data class Window(
  var controllerClass: String? = null,
  var settingsJson: String? = null,

  var stageWidth: Double = 0.toDouble(),
  var stageHeight: Double = 0.toDouble(),
  var stageX: Double = 0.toDouble(),
  var stageY: Double = 0.toDouble(),
  var title: String? = null,
  var isVisible: Boolean = false
)
