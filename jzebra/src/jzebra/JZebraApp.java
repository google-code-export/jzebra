/*
 * WhiteCardApp.java
 * Liscence GPL 3.0+
 */

package jzebra;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class JZebraApp extends SingleFrameApplication  {
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new JZebraView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of WhiteCardApp
     */
    public static JZebraApp getApplication() {
        return Application.getInstance(JZebraApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        JZebra.setCommands(JZebra.readFile("/jzebra/resources/eltron_p310.cfg"));
        JZebra.processArgs(args);
        if (!JZebra.isHeadless())
            launch(JZebraApp.class, args);
    }
}
