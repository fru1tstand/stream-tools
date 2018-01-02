package me.fru1t.javafx

import javafx.scene.control.TextInputDialog

/** Utility functions for [TextInputDialog]. */
object TextInputDialogUtils {
  fun createShowAndWait(title: String?, headerText: String?, contentText: String?): String? {
    val dialog = TextInputDialog()
    dialog.title = title
    dialog.headerText = headerText
    dialog.contentText = contentText
    val result = dialog.showAndWait()
    return result.orElse(null)
  }
}
