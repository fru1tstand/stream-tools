package me.fru1t.streamtools.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.streamtools.controller.settings.GraphStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

@FXMLResource("/FXML/GraphStats.fxml")
public class GraphStatsController
        extends WindowWithSettingsController<GraphStatsSettings, GraphStatsSettingsController> {

    private @FXML Canvas canvas;
    private GraphicsContext ctx;
    private final KeyboardAndMouseStatistics stats;

    // Settings
    private long graphHistoryTimeMS;
    private int pointSize;

    private int[] data;
    private int dataPointer;

    private long lastTime;
    private long msSinceLastPoint;

    private transient double pixelsPerMillisecond;
    private transient double spaceBetweenPoints;
    private transient long msBetweenPoints;

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
        // Render, starting from tail to front
        for (int i = 0; i < data.length; i++) {
            double y = canvas.getHeight() -
                    ((canvas.getHeight() - pointSize) *
                            (1.0 * data[(i + dataPointer) % data.length] / largestData))
                    - pointSize;
            double x = -(pixelsPerMillisecond * msSinceLastPoint) + i * spaceBetweenPoints
                    + spaceBetweenPoints - pointSize;

            ctx.setFill(Color.BLACK);
            ctx.fillOval(x, y, pointSize, pointSize);
        }
    }

    @Override
    public void onSettingsChange(GraphStatsSettings settings) {
        stats.setBufferSize(settings.getStatsBufferSize());

        graphHistoryTimeMS = settings.getGraphHistoryTimeMS();
        pointSize = settings.getPointSize();

        data = new int[settings.getGraphPoints()];
        msBetweenPoints = settings.getGraphHistoryTimeMS() / settings.getGraphPoints();

        updateCanvasSize();
    }

    private void updateCanvasSize() {
        canvas.setWidth(scene.getWidth());
        canvas.setHeight(scene.getHeight());
        spaceBetweenPoints = (canvas.getWidth() + pointSize) / data.length;
        pixelsPerMillisecond = (canvas.getWidth() + pointSize) / graphHistoryTimeMS;
    }
}
