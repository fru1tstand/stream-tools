package me.fru1t.streamtools.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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

    private int[] data;
    private int dataPointer;

    private long lastTime;
    private long msSinceLastPoint;

    // Values set by settings or canvas size
    private transient double pixelsPerMillisecond;
    private transient double spaceBetweenPoints;
    private transient long msBetweenPoints;
    private transient Color dotColor;
    private transient Color lineColor;
    private transient Color barColor;

    /**
     * Use {@link me.fru1t.javafx.Controller#create(Class)} or any #create derivative to
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
        scene.widthProperty().addListener((observable, oldValue, newValue) -> updateCanvasSize());
        scene.heightProperty().addListener((observable, oldValue, newValue) -> updateCanvasSize());

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
        int largestData = 1;
        for (int d : data) {
            if (d > largestData) {
                largestData = d;
            }
        }

        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(Color.BLACK);

        boolean hasInitialPoint = false;
        if (settings.isEnableLine()) {
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
            if (settings.isEnableDots()) {
                ctx.setFill(dotColor);
                ctx.fillOval(x - settings.getDotSize() / 2,
                        y - settings.getDotSize() / 2,
                        settings.getDotSize(),
                        settings.getDotSize());
            }

            // lines
            if (settings.isEnableLine()) {
                if (hasInitialPoint) {
                    ctx.lineTo(x, y);
                } else {
                    ctx.moveTo(x, y);
                    hasInitialPoint = true;
                }
            }

            // Bars
            if (settings.isEnableBars()) {
                ctx.setFill(barColor);
                ctx.fillRect(
                        x - settings.getBarWidth() / 2,
                        y,
                        settings.getBarWidth(),
                        canvas.getHeight() - y - settings.getDotSize() / 2);
            }
        }

        if (settings.isEnableLine()) {
            ctx.setStroke(lineColor);
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
        if (!settings.isEnableDots()) {
            settings.setDotSize(0);
        }

        data = new int[settings.getGraphPoints()];
        msBetweenPoints = settings.getGraphHistoryTimeMS() / settings.getGraphPoints();
        dotColor = Color.web(settings.getDotColor());
        lineColor = Color.web(settings.getLineColor());
        barColor = Color.web(settings.getBarColor());

        scene.getRoot().setStyle(String.format(ROOT_STYLE, settings.getBackgroundColor()));

        updateCanvasSize();
    }

    private void updateCanvasSize() {
        canvas.setWidth(scene.getWidth()
                - settings.getPaddingLeft() - settings.getPaddingRight());
        canvas.setHeight(scene.getHeight()
                - settings.getPaddingTop() - settings.getPaddingBottom());

        // Set padding
        AnchorPane.setTopAnchor(canvas, (double) settings.getPaddingTop());
        AnchorPane.setRightAnchor(canvas, (double) settings.getPaddingRight());
        AnchorPane.setBottomAnchor(canvas, (double) settings.getPaddingTop());
        AnchorPane.setLeftAnchor(canvas, (double) settings.getPaddingLeft());

        spaceBetweenPoints = (canvas.getWidth() + settings.getDotSize()) / data.length;
        pixelsPerMillisecond =
                (canvas.getWidth() + settings.getDotSize()) / settings.getGraphHistoryTimeMS();
    }
}
