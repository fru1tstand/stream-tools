package me.fru1t.streamtools.util;

import lombok.Data;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;

import java.awt.Point;
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

    private static final int BUFFER_SIZE = 60 * 5; // seconds

    private final DataPoint currentData;
    private final DataPoint[] data;
    private long totalActions;
    private long totalPixels;

    private final Point lastMousePosition;
    private int dataPointer = 0;
    private long lastTime = 0;
    private long thisTime = 0;

    public KeyboardAndMouseStatistics() {
        // Initializing
        data = new DataPoint[BUFFER_SIZE];
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
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    /**
     * @return The current data metrics for the most recent time slice.
     */
    public CurrentData getCurrentData() {
        tick();

        // Calculate results
        double delta = 0;
        long actions = 0;
        long movement = 0;
        for (DataPoint data : data) {
            if (data == null) {
                continue;
            }
            delta += data.timeDeltaMS;
            actions += data.totalKeyboardActions;
            movement += data.totalMouseMovePixels;
        }
        delta /= 60.0 * 1000; // 1 minute

        return new CurrentData(
                (int) (actions / delta), // APM
                (long) (movement / delta), // PPM
                totalActions,
                totalPixels);
    }

    /**
     * Shuts down the global hook and unregisters JNativeHook.
     */
    public void shutdown() {
        GlobalScreen.removeNativeKeyListener(this);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
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

    private void tick() {
        if (data[dataPointer] == null) {
            data[dataPointer] = new DataPoint();
        }

        // Store current data into data
        synchronized (currentData) {
            thisTime = (new Date()).getTime();
            data[dataPointer].totalKeyboardActions = currentData.totalKeyboardActions;
            data[dataPointer].totalMouseMovePixels = currentData.totalMouseMovePixels;
            totalActions += currentData.totalKeyboardActions;
            totalPixels += currentData.totalMouseMovePixels;
            currentData.totalKeyboardActions = 0;
            currentData.totalMouseMovePixels = 0;
        }

        data[dataPointer].timeDeltaMS = (int) (thisTime - lastTime);
        lastTime = thisTime;

        if (dataPointer + 1 >= data.length) {
            dataPointer = 0;
        } else {
            ++dataPointer;
        }
    }
}
