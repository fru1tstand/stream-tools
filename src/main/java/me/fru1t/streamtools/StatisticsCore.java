package me.fru1t.streamtools;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;

import java.awt.*;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks mouse and keyboard movements. Provides an interface to hook into receiving updates to
 * these values.
 */
public class StatisticsCore implements NativeKeyListener, NativeMouseMotionListener {
    /**
     * Events to be handled. Calls to these events are done through the JavaFX thread.
     */
    public interface Events {
        /**
         * Fired when the JavaFX animation timer is ready.
         * @param keyboardAPM
         */
        void onStatsUpdate(int keyboardAPM, long mouseMPM);
    }

    private static class DataPoint {
        int timeDeltaMS;
        int totalKeyboardActions;
        long totalMouseMoveDistance;
    }

    private static final int BUFFER_SIZE = 60 * 5; // seconds

    private final HashSet<Events> eventHandlers;
    private final DataPoint currentData;
    private final DataPoint[] data;

    private Point lastMousePosition;
    private int dataPointer = 0;
    private long lastTime = 0;
    private long thisTime = 0;

    public StatisticsCore() {
        // Initializing
        eventHandlers = new HashSet<>();
        data = new DataPoint[BUFFER_SIZE];
        currentData = new DataPoint();
        lastTime = (new Date()).getTime();
        lastMousePosition = new Point(0, 0);

        // Add hooks to JNativeHook
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);

        // Disable JNativeHook's very verbose logging
        Logger globalScreenLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        globalScreenLogger.setLevel(Level.WARNING);
        globalScreenLogger.setUseParentHandlers(false);
    }

    /**
     * Adds an event handler
     */
    public void addEventHandler(Events handler) {
        eventHandlers.add(handler);
    }

    /**
     * Removes an event handler.
     */
    public void removeEventHandler(Events handler) {
        eventHandlers.remove(handler);
    }

    /**
     * This method must be called on the JavaFX application thread.
     */
    public void notifyHandlers() {
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
            movement += data.totalMouseMoveDistance;
        }
        delta /= 60.0 * 1000; // 1 minute

        for (Events eventHandler : eventHandlers) {
            eventHandler.onStatsUpdate((int) (actions / delta), (long) (movement / delta));
        }
    }

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
            currentData.totalMouseMoveDistance
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
            data[dataPointer].totalMouseMoveDistance = currentData.totalMouseMoveDistance;
            currentData.totalKeyboardActions = 0;
            currentData.totalMouseMoveDistance = 0;
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
