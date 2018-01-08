package me.fru1t.javafx

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import javafx.scene.paint.Color
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SerializableColorTest {
  private lateinit var serializableColor: SerializableColor

  @BeforeEach
  fun setUp() {
    serializableColor = SerializableColor(INITIAL_COLOR)
  }

  @Test
  fun setColor_color() {
    serializableColor.setColor(Color(0.0, 0.0, 1.0, 1.0))
    assertThat(serializableColor.colorHex).isEqualTo("#0000FF")
  }

  @Test
  fun setColor_string() {
    serializableColor.setColor("#0000FF")
    assertThat(serializableColor.colorHex).isEqualTo("#0000FF")
  }

  @Test
  fun getColor() {
    val serializableColorByString = SerializableColor("#FF0000")
    val serializableColorByColor = SerializableColor(Color(1.0, 0.0, 0.0, 1.0))
    assertThat(serializableColorByString.getColor()).isEqualTo(Color(1.0, 0.0, 0.0, 1.0))
    assertThat(serializableColorByColor.getColor()).isEqualTo(Color(1.0, 0.0, 0.0, 1.0))
  }

  @Test
  fun getColor_afterSerialization() {
    serializableColor.setColor(Color.ALICEBLUE)
    val gson = Gson()
    val serializedText = gson.toJson(serializableColor)
    val result = gson.fromJson(serializedText, SerializableColor::class.java)
    assertThat(result.getColor()).isEqualTo(Color.ALICEBLUE)
  }

  @Test
  fun testToString() {
    val serializableColor = SerializableColor(Color(0.0, 1.0, 0.0, 1.0))
    assertThat(serializableColor.toString()).isEqualTo("#00FF00")
  }

  companion object {
    private val INITIAL_COLOR = Color.BLACK
  }
}
