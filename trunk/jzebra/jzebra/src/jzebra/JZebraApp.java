/*
 * JZebraApp
 * Author:  A. Tres Finocchiaro
 * Liscence GPL 3.0+
 */

package jzebra;

import java.util.logging.Level;
import java.util.logging.Logger;
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
        //show(new JZebraView(this));
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
        boolean showGui = true;
        for (String s : args) {
           if (s.toLowerCase().contains("nogui"))
               showGui = false;
        }
        
        if (showGui) {
            JZebraView j = new JZebraView(getApplication(), args);
            getApplication().show(j);
        }
        else {
            Logger.getLogger(JZebraApp.class.getName()).log(Level.INFO,
                    "Running application without a GUI");
        }
    }
}
