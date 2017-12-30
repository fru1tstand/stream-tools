package me.fru1t.streamtools.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.streamtools.controller.settings.GraphStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

@FXMLResource("/FXML/GraphStats.fxml")
public class GraphStatsController
        extends WindowWithSettingsController<GraphStatsSettings, GraphStatsSettingsController> {

    private static final String CANVAS_STYLE = "-fx-background-color: transparent;";
    private static final String ROOT_STYLE = "-fx-background-color: %s";

    private @FXML Canvas canvas;
    private GraphicsContext ctx;
    private final KeyboardAndMouseStatistics stats;

    // Settings
    private GraphStatsSettings settings;

    private long[] data;
    private int dataPointer;

    private long lastTime;
    private long msSinceLastPoint;

    // Values set by settings or canvas size
    private transient double pixelsPerMillisecond;
    private transient double spaceBetweenPoints;
    private transient long msBetweenPoints;

    private transient double timeAxisPxBetweenMajor;
    private transient double timeAxisPxBetweenMinor;
    private transient double timeAxisPxBetweenText;

    /**
     * Use {@link me.fru1t.javafx.Controller.Companion#create(Class)} or any #create derivative to
     * instantiate.
     */
    public GraphStatsController() {
        stats = new KeyboardAndMouseStatistics();
    }

    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();
        ctx = canvas.getGraphicsContext2D();

        // Set up canvas resizing
        getScene().widthProperty().addListener((observable, oldValue, newValue) -> updateCanvasSize());
        getScene().heightProperty().addListener((observable, oldValue, newValue) -> updateCanvasSize());

        canvas.setStyle(CANVAS_STYLE);
    }

    @Override
    public void onUpdate(long now) {
        // Calculate some time information
        msSinceLastPoint += (now - lastTime) / 1000000;
        lastTime = now;
        stats.tick();

        // Check if we should add a point
        if (msSinceLastPoint >= msBetweenPoints) {
            KeyboardAndMouseStatistics.CurrentData currentData = stats.getCurrentData();
            msSinceLastPoint = 0;
            data[dataPointer] = currentData.getKeyboardAPM();
            dataPointer = (dataPointer + 1) % data.length;
        }

        // Find largest value
        long largestData = settings.getMinValue();
        for (long d : data) {
            if (d > settings.getMaxValue()) {
                largestData = settings.getMaxValue();
                break;
            }
            if (d > largestData) {
                largestData = d;
            }
        }

        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(Color.BLACK);

        // Time Axis
        if (settings.getTimeAxis().getEnabled()) {
            double currentPosition = canvas.getWidth();

            // Major
            if (timeAxisPxBetweenMajor > 1) {
                ctx.setFill(settings.getTimeAxis().getMajorColor().getColor());
                if (!settings.getTimeAxis().getFirstMajorIncluded()) {
                    currentPosition -= timeAxisPxBetweenMajor;
                }
                while (currentPosition > 0) {
                    ctx.fillRect(currentPosition,
                            0,
                            settings.getTimeAxis().getMajorWidth(),
                            canvas.getHeight());
                    currentPosition -= timeAxisPxBetweenMajor;
                }
            }

            if (timeAxisPxBetweenMinor > 0) {
                currentPosition = canvas.getWidth();
                ctx.setFill(settings.getTimeAxis().getMinorColor().getColor());
                if (!settings.getTimeAxis().getFirstMajorIncluded()) {
                    currentPosition -= timeAxisPxBetweenMinor;
                }
                while (currentPosition > 0) {
                    ctx.fillRect(currentPosition,
                            0,
                            settings.getTimeAxis().getMinorWidth(),
                            canvas.getHeight());
                    currentPosition -= timeAxisPxBetweenMinor;
                }
            }

            if (timeAxisPxBetweenText > 0) {
                currentPosition = canvas.getWidth();
                long currentTime = 0;
                ctx.setFont(Font.font(
                        settings.getTimeAxis().getTextFontFamily(),
                        (settings.getTimeAxis().getTextBold() ? FontWeight.BOLD : FontWeight.NORMAL),
                        (settings.getTimeAxis().getTextItalic()
                                ? FontPosture.ITALIC : FontPosture.REGULAR),
                        settings.getTimeAxis().getTextSize()));
                ctx.setFill(settings.getTimeAxis().getTextColor().getColor());
                if (!settings.getTimeAxis().getFirstTextIncluded()) {
                    currentTime += settings.getTimeAxis().getTextEvery();
                    currentPosition -= timeAxisPxBetweenText;
                }
                while (currentPosition > 0) {
                    ctx.fillText(parseTimeText(currentTime, settings.getTimeAxis().getTextValue()),
                            currentPosition + settings.getTimeAxis().getTextXOffset(),
                            canvas.getHeight() - settings.getTimeAxis().getTextYOffset());
                    currentTime += settings.getTimeAxis().getTextEvery();
                    currentPosition -= timeAxisPxBetweenText;
                }
            }
        }

        // Fixed value axis
        if (settings.getFixedValueAxis().getEnabled()) {
            double currentPosition = canvas.getHeight();

            // Major
            if (settings.getFixedValueAxis().getMajorEvery() > 1) {
                ctx.setFill(settings.getFixedValueAxis().getMajorColor().getColor());
                double pxPerLine = canvas.getHeight()
                        / (1.0 * largestData / settings.getFixedValueAxis().getMajorEvery());
                if (!settings.getFixedValueAxis().getFirstMajorIncluded()) {
                    currentPosition -= pxPerLine;
                }
                while (currentPosition > 0) {
                    ctx.fillRect(0,
                            currentPosition,
                            canvas.getWidth(),
                            settings.getFixedValueAxis().getMajorWidth());
                    currentPosition -= pxPerLine;
                }
            }

            // Minor
            // TODO: Fix minor axis
            if (settings.getFixedValueAxis().getMinorEvery() > 1) {
                ctx.setFill(settings.getFixedValueAxis().getMinorColor().getColor());
                currentPosition = canvas.getHeight();
                double pxPerLine = canvas.getHeight()
                        / (1.0 * largestData / settings.getFixedValueAxis().getMinorEvery());
                if (!settings.getFixedValueAxis().getFirstMinorIncluded()) {
                    currentPosition -= pxPerLine;
                }
                while (currentPosition > 0) {
                    ctx.fillRect(0,
                            currentPosition,
                            canvas.getWidth(),
                            settings.getFixedValueAxis().getMinorWidth());
                    currentPosition -= pxPerLine;
                }
            }

            // Text
            if (settings.getFixedValueAxis().getTextEvery() > 1) {
                ctx.setFill(settings.getFixedValueAxis().getTextColor().getColor());
                currentPosition = canvas.getHeight();
                double pxPerLine = canvas.getHeight()
                        / (1.0 * largestData / settings.getFixedValueAxis().getTextEvery());
                long currentValue = 0;
                if (!settings.getFixedValueAxis().getFirstTextIncluded()) {
                    currentPosition -= pxPerLine;
                    currentValue += settings.getFixedValueAxis().getTextEvery();
                }
                while (currentPosition > 0) {
                    ctx.fillText(
                            parseValueText(
                                    currentValue, settings.getFixedValueAxis().getTextValue()),
                            settings.getFixedValueAxis().getTextXOffset(),
                            currentPosition + settings.getFixedValueAxis().getTextYOffset());
                    currentValue += settings.getFixedValueAxis().getTextEvery();
                    currentPosition -= pxPerLine;
                }
            }
        }

        boolean hasInitialPoint = false;
        if (settings.getEnableLine()) {
            ctx.beginPath();
        }

        // Render, starting from tail to front
        for (int i = 0; i < data.length; i++) {
            // Creates coordinates for right-to-left history, and bottom-to-top values)
            double y = canvas.getHeight() -
                    ((canvas.getHeight() - settings.getDotSize()) *
                            (1.0 * data[(i + dataPointer) % data.length] / largestData))
                    - settings.getDotSize() / 2;
            double x = -(pixelsPerMillisecond * msSinceLastPoint)
                    + i * spaceBetweenPoints
                    + spaceBetweenPoints
                    - settings.getDotSize() / 2;

            // Dots
            if (settings.getEnableDots()) {
                ctx.setFill(settings.getDotColor().getColor());
                ctx.fillOval(x - settings.getDotSize() / 2,
                        y - settings.getDotSize() / 2,
                        settings.getDotSize(),
                        settings.getDotSize());
            }

            // lines
            if (settings.getEnableLine()) {
                if (hasInitialPoint) {
                    ctx.lineTo(x, y);
                } else {
                    ctx.moveTo(x, y);
                    hasInitialPoint = true;
                }
            }

            // Bars
            if (settings.getEnableBars()) {
                ctx.setFill(settings.getBarColor().getColor());
                ctx.fillRect(
                        x - settings.getBarWidth() / 2,
                        y,
                        settings.getBarWidth(),
                        canvas.getHeight() - y - settings.getDotSize() / 2);
            }
        }

        if (settings.getEnableLine()) {
            ctx.setStroke(settings.getLineColor().getColor());
            ctx.setLineWidth(settings.getLineWidth());
            ctx.stroke();
        }
    }

    @Override
    public void onShutdown() {
        stats.shutdown();
        super.onShutdown();
    }

    @Override
    public void onSettingsChange(GraphStatsSettings settings) {
        stats.setBufferSize(settings.getStatsBufferSize());

        this.settings = settings.copy();
        if (!settings.getEnableDots()) {
            settings.setDotSize(0);
        }

        data = new long[settings.getGraphPoints()];
        msBetweenPoints = settings.getGraphHistoryTimeMS() / settings.getGraphPoints();

        getScene().getRoot().setStyle(
            String.format(ROOT_STYLE, settings.getBackgroundColor().getColorHex()));

        updateCanvasSize();
    }

    private void updateCanvasSize() {
        canvas.setWidth(getScene().getWidth()
                - settings.getPaddingLeft() - settings.getPaddingRight());
        canvas.setHeight(getScene().getHeight()
                - settings.getPaddingTop() - settings.getPaddingBottom());

        // Set padding
        AnchorPane.setTopAnchor(canvas, (double) settings.getPaddingTop());
        AnchorPane.setRightAnchor(canvas, (double) settings.getPaddingRight());
        AnchorPane.setBottomAnchor(canvas, (double) settings.getPaddingTop());
        AnchorPane.setLeftAnchor(canvas, (double) settings.getPaddingLeft());

        spaceBetweenPoints = (canvas.getWidth() + settings.getDotSize()) / data.length;
        pixelsPerMillisecond =
                (canvas.getWidth() + settings.getDotSize()) / settings.getGraphHistoryTimeMS();

        timeAxisPxBetweenMinor = 0;
        if (settings.getTimeAxis().getMinorEvery() > 1) {
            timeAxisPxBetweenMinor = canvas.getWidth()
                    / (1.0
                    * settings.getGraphHistoryTimeMS() / settings.getTimeAxis().getMinorEvery());
        }
        timeAxisPxBetweenMajor = 0;
        if (settings.getTimeAxis().getMajorEvery() > 1) {
            timeAxisPxBetweenMajor = canvas.getWidth()
                    / (1.0
                    * settings.getGraphHistoryTimeMS() / settings.getTimeAxis().getMajorEvery());
        }
        timeAxisPxBetweenText = 0;
        if (settings.getTimeAxis().getTextEvery() > 1) {
            timeAxisPxBetweenText = canvas.getWidth()
                    / (1.0
                    * settings.getGraphHistoryTimeMS() / settings.getTimeAxis().getTextEvery());
        }
    }

    private String parseTimeText(long ms, String value) {
        return value
                .replace(GraphStatsSettings.Companion.getTIME_AXIS_VALUE_S(), Math.round(ms / 1000.0) + "")
                .replace(GraphStatsSettings.Companion.getTIME_AXIS_VALUE_MS(), ms + "");
    }

    private String parseValueText(long value, String string) {
        return string.replace(GraphStatsSettings.Companion.getVALUE_AXIS_VALUE(), value + "");
    }
}
