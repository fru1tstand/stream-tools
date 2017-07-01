package me.fru1t.streamtools.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Data;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;

import java.awt.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks mouse and keyboard movements. Provides an interface to hook into receiving updates to
 * these values.
 */
public class KeyboardAndMouseStatistics implements NativeKeyListener, NativeMouseMotionListener {
    private static class DataPoint {
        int timeDeltaMS;
        int totalKeyboardActions;
        long totalMouseMovePixels;
    }

    @Data
    public static class CurrentData {
        public final int keyboardAPM;
        public final long mousePPM;
        public final long totalActions;
        public final long totalPixels;
    }

    private static final Logger LOGGER =
            Logger.getLogger(KeyboardAndMouseStatistics.class.getName());
    private static final String JNATIVEHOOK_NO_START_ALERT_BODY =
            "Couldn't start JNativeHook due to the following error: ";
    private static final String JNATIVEHOOK_NO_END_ALERT_BODY =
            "Couldn't shut down JNativeHook due to the following error: ";
    public static final int DEFAULT_BUFFER_SIZE = 60 * 5; // frames

    private static final Object jNativeHookInstancesSynchronizer = new Object();
    private static int jNativeHookInstances = 0;

    private final DataPoint currentData;
    private DataPoint[] data;
    private long totalActions;
    private long totalPixels;

    private final Point lastMousePosition;
    private int dataPointer = 0;
    private long lastTime = 0;
    private long thisTime = 0;

    /**
     * Creates a new KeyboardAndMouseStatistics object with the default buffer size of
     * {@value DEFAULT_BUFFER_SIZE} frames.
     */
    public KeyboardAndMouseStatistics() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new KeyboardAndMouseStatistics object with the given buffer size for calculating
     * averages. One buffer slot is used every {@link #getCurrentData()} call which,
     * theoretically, should be every animation timer tick. The tick rate is different for each
     * machine and should match the user's monitor's refresh rate.
     * @param bufferSize
     */
    public KeyboardAndMouseStatistics(int bufferSize) {
        // Initializing
        data = new DataPoint[bufferSize];
        currentData = new DataPoint();
        lastTime = (new Date()).getTime();
        lastMousePosition = new Point(0, 0);
        totalActions = 0;
        totalPixels = 0;

        // Disable JNativeHook's very verbose logging
        Logger globalScreenLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        globalScreenLogger.setLevel(Level.WARNING);
        globalScreenLogger.setUseParentHandlers(false);

        // Add hooks to JNativeHook
        synchronized (jNativeHookInstancesSynchronizer) {
            if (jNativeHookInstances == 0) {
                try {
                    GlobalScreen.registerNativeHook();
                } catch (NativeHookException e) {
                    LOGGER.log(Level.SEVERE, "Couldn't start JNativeHook.", e);
                    (new Alert(Alert.AlertType.WARNING,
                            JNATIVEHOOK_NO_START_ALERT_BODY + e.getMessage(),
                            ButtonType.OK))
                            .showAndWait();
                }
            }
            ++jNativeHookInstances;
        }

        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    /**
     * @return The current data metrics for the most recent time slice.
     */
    public CurrentData getCurrentData() {
        // Calculate results
        double delta = 0;
        long actions = 0;
        long movement = 0;
        synchronized (currentData) {
            for (DataPoint data : data) {
                if (data == null) {
                    continue;
                }
                delta += data.timeDeltaMS;
                actions += data.totalKeyboardActions;
                movement += data.totalMouseMovePixels;
            }
        }
        delta /= 60.0 * 1000; // 1 minute

        return new CurrentData(
                (int) (actions / delta), // APM
                (long) (movement / delta), // PPM
                totalActions,
                totalPixels);
    }

    /**
     * Sets buffer size to determine averages for the statistics. Each {@link #getCurrentData()}
     * call consumes a single frame in the buffer. Theoretically, #getCurrentData is called every
     * animation frame redraw, which is equivalent to the user's monitor's refresh rate. Hence,
     * on a 60Hz monitor, a buffer size of 60 would be a 1 second window.
     * Ignores any invalid buffer sizes (ie. < 1).
     * @param bufferSize The size of the buffer.
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            return;
        }
        synchronized (currentData) {
            data = new DataPoint[bufferSize];
            dataPointer = 0;
        }
    }

    public int getBufferSize() {
        return data.length;
    }

    /**
     * Shuts down the global hook and unregisters JNativeHook.
     */
    public void shutdown() {
        GlobalScreen.removeNativeKeyListener(this);

        synchronized (jNativeHookInstancesSynchronizer) {
            if (jNativeHookInstances == 1) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException e) {
                    LOGGER.log(Level.SEVERE, "Couldn't stop JNativeHook.", e);
                    (new Alert(Alert.AlertType.WARNING,
                            JNATIVEHOOK_NO_END_ALERT_BODY + e.getMessage(),
                            ButtonType.OK))
                            .showAndWait();
                }
            }
            --jNativeHookInstances;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) { }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        synchronized (currentData) {
            currentData.totalMouseMovePixels
                    += lastMousePosition.distance(nativeMouseEvent.getPoint());
            lastMousePosition.setLocation(nativeMouseEvent.getPoint());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        synchronized (currentData) {
            currentData.totalKeyboardActions++;
        }
    }

    public void tick() {
        // Store current data into data
        synchronized (currentData) {
            if (data[dataPointer] == null) {
                data[dataPointer] = new DataPoint();
            }

            thisTime = (new Date()).getTime();
            data[dataPointer].totalKeyboardActions = currentData.totalKeyboardActions;
            data[dataPointer].totalMouseMovePixels = currentData.totalMouseMovePixels;
            totalActions += currentData.totalKeyboardActions;
            totalPixels += currentData.totalMouseMovePixels;
            currentData.totalKeyboardActions = 0;
            currentData.totalMouseMovePixels = 0;

            data[dataPointer].timeDeltaMS = (int) (thisTime - lastTime);
        }

        lastTime = thisTime;

        if (dataPointer + 1 >= data.length) {
            dataPointer = 0;
        } else {
            ++dataPointer;
        }
    }
}
