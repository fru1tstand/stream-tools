package me.fru1t.streamtools

import javafx.application.Application
import javafx.stage.Stage
import me.fru1t.javafx.Controller
import me.fru1t.streamtools.controller.MainMenuController

fun main(args: Array<String>) {
  Application.launch(StreamToolsApplication::class.java, *args)
}

/** Entry point for the StreamTools application. */
class StreamToolsApplication : Application() {
  override fun start(primaryStage: Stage) {
    Controller.create(MainMenuController::class.java, primaryStage).show()
  }
}
