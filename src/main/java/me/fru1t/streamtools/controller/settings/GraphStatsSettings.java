package me.fru1t.streamtools.controller.settings;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.fru1t.javafx.SerializableColor;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.javafx.DefaultSettings;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

import javax.annotation.Nonnull;


/**
 * Settings for {@link me.fru1t.streamtools.controller.GraphStatsController}.
 */
@Data
@Builder
@DefaultSettings("DEFAULT")
@EqualsAndHashCode(callSuper = false)
public class GraphStatsSettings extends Settings<GraphStatsSettings> {
    public enum Statistic {
        APM("Action per Minute"),
        PPM("Mouse Movement per Minute");

        public final String name;
        Statistic(String name) {
            this.name = name;
        }
    }

    /**
     * Represents an axis with markers at a fixed constant.
     */
    @Data
    @Builder
    public static class FixedAxis {
        private @Default boolean enabled = false;

        private @Default boolean firstMajorIncluded = false;
        private @Default long majorEvery = 0;
        private @Default int majorWidth = 1;
        private @Default @Nonnull SerializableColor majorColor = new SerializableColor(Color.BLACK);

        private @Default boolean firstMinorIncluded = false;
        private @Default long minorEvery = 0;
        private @Default int minorWidth = 1;
        private @Default @Nonnull SerializableColor minorColor = new SerializableColor(Color.BLACK);

        private @Default boolean firstTextIncluded = false;
        private @Default long textEvery = 0;
        private @Default @Nonnull SerializableColor textColor = new SerializableColor(Color.BLACK);
        private @Default @Nonnull String textValue = "";
        private @Default @Nonnull String textFontFamily = Font.getDefault().getFamily();
        private @Default int textSize = 16;
        private @Default boolean textBold = false;
        private @Default boolean textItalic = false;
        private @Default double textXOffset = 0;
        private @Default double textYOffset = 0;
    }

    /**
     * A placeholder for the number of milliseconds since the start of the graph.
     */
    public static final String TIME_AXIS_VALUE_MS = "{ms}";
    /**
     * A placeholder for the number of seconds since the start of the graph, rounded.
     */
    public static final String TIME_AXIS_VALUE_S = "{s}";
    /**
     * A placeholder for the value shown in the graph.
     */
    public static final String VALUE_AXIS_VALUE = "{v}";


    // Lombok automatically fills in default field values via the @Default annotation.
    private static final GraphStatsSettings DEFAULT = GraphStatsSettings.builder().build();


    // ** General
    private @Default int statsBufferSize = KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE;
    private @Default long graphHistoryTimeMS = 1000 * 15; // 15 seconds
    private @Default int graphPoints = 200;
    private @Default @Nonnull SerializableColor backgroundColor = new SerializableColor("#0f0");

    private @Default long minValue = 230;
    private @Default long maxValue = Long.MAX_VALUE;

    private @Default int paddingTop = 5;
    private @Default int paddingRight = 5;
    private @Default int paddingBottom = 5;
    private @Default int paddingLeft = 5;

    // ** Time Axis
    private @Default FixedAxis timeAxis = FixedAxis.builder()
            .enabled(true)
            .majorEvery(5000)
            .majorWidth(1)
            .majorColor(new SerializableColor("#ccc"))

            .minorEvery(1000)
            .minorWidth(1)
            .minorColor(new SerializableColor("#eee"))

            .textEvery(5000)
            .textColor(new SerializableColor("#000"))
            .textValue("+{s}s")
            .textFontFamily(Font.getDefault().getFamily())
            .textSize(10)
            .textBold(false)
            .textItalic(false)
            .textXOffset(3)
            .textYOffset(0)
            .build();

    // ** Fixed value axis
    private @Default FixedAxis fixedValueAxis = FixedAxis.builder()
            .enabled(true)
            .majorEvery(50)
            .majorWidth(1)
            .majorColor(new SerializableColor("#ccc"))

            .minorEvery(25)
            .minorWidth(1)
            .minorColor(new SerializableColor("#eee"))

            .textEvery(50)
            .textColor(new SerializableColor("#000"))
            .textValue("{v}apm")
            .textFontFamily(Font.getDefault().getFamily())
            .textSize(10)
            .textBold(false)
            .textItalic(false)
            .textXOffset(3)
            .textYOffset(-3)
            .build();

    // ** Visuals
    private @Default Statistic statistic = Statistic.APM;

    // ** Dots
    private @Default boolean enableDots = true;
    private @Default int dotSize = 8;
    private @Default @Nonnull SerializableColor dotColor = new SerializableColor(Color.BLACK);

    // ** Line
    private @Default boolean enableLine = true;
    private @Default int lineWidth = 2;
    private @Default @Nonnull SerializableColor lineColor = new SerializableColor(Color.AQUA);

    // ** Bar
    private @Default boolean enableBars = false;
    private @Default int barWidth = 10;
    private @Default @Nonnull SerializableColor barColor = new SerializableColor(Color.WHEAT);
}
