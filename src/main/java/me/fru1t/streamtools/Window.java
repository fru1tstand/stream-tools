package me.fru1t.streamtools;

/**
 * Simple java object containing data for windows within the main menu. This is used to store
 * settings in a Json format.
 */
public class Window {
    public String controllerClass;
    public String settingsJson;

    public double stageWidth;
    public double stageHeight;
    public double stageX;
    public double stageY;
    public String title;
    public boolean isVisible;
}
