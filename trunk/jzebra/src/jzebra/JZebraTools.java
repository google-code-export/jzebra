/*
 * Works with PrintRaw, ParameterLoader, ConfigLoader and a Paramatable object
 * to send your data to the printer
 */
package jzebra;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.print.PrintException;
import javax.print.event.PrintJobEvent;
import javax.swing.JApplet;
import javax.swing.JOptionPane;

/**
 * Common methods between console, view and applet modes.
 * <p>Since JZebraConosole and JZebraView both take parameters in the same fashion,
 * a lot of the code would be duplicated if it were to reside in both.  Additionally
 * most gui funcitons, such as message boxes, etc would have to be written twice
 * once for the JZebraView and again for JZebraApplet.  This class JZebraTools 
 * does the majority of the work for all three.</p>
 * @author A. Tres Finocchiaro
 */
public class JZebraTools {

    public JZebraTools(String[] args) {
        super();
    }

    /**
     * Turn an array of arguments into a HashMap of arguments.  This quickly
     * strips duplicates and allows direct access for use later.  Args have to
     * have an "=" between the arg_name and the arg_value.
     *
     * @param args The array containing your main method's args[]
     * @return HashMap<String,String> containing <arg_name, arg_value>
     */
    public static HashMap<String, String> mapArgs(String args[]) {
        HashMap<String, String> argMap = new HashMap<String, String>();
        for (String s : Arrays.asList(args)) {
            String a[] = s.split("=");
            if (a.length >= 2 && a[1] != null) {
                argMap.put(a[0].toLowerCase(), a[1]);
                Logger.getLogger(JZebraTools.class.getName()).log(
                        Level.INFO, "Cmd arg specified: " +
                        a[0].toLowerCase() + " value: " + a[1]);
            }
        }
        return argMap;
    }

    public static Exception doPrint(Paramable p, PrintRaw pr, ConfigLoader cl) {
        try {
            String debug = "Raw output:\n";
            for (String s : pr.getRawCmds().split(cl.getEscapedLineFeed())) {
                debug += "> " + s + "\n";
            }
            log(p, debug);
            pr.print();
        } catch (PrintException e) {
            log(p, p.getErrorMessage(), e);
            return e;
        }
        log(p, p.getSuccessMessage());
        return null;
    }

    /*public static HashMap<String, String> mapArgs(JApplet j) {
    HashMap<String, String> argMap = new HashMap<String, String>();
    for (String s : j.get)
    }*/
    /**
     * Reads in a bunch of commands and looks for a "$" dollar sign infront of
     * an alpha-numeric word value.  Ex:  "$FIRSTNAME".  Returns all values in
     * a HashSet to avoid duplicates.
     * @param configCmds Config file contents
     * @return A unique set of variable names
     */
    public static HashSet<String> hashVars(String configCmds) {
        HashSet<String> varNames = new HashSet<String>();
        Matcher m = Pattern.compile("\\$(\\w+)").matcher(configCmds);
        while (m.find()) {
            String s = m.group(1);
            if (s != null) {
                varNames.add(s);
            }
        }
        return varNames;
    }

    /**
     * Cycles through all variable names in your config file denotes by a dollar
     * sign "$" Ex: "$FIRSTNAME" and compares them against the arguments supplied
     * on command line. If a match is found, it replaces the variable with the
     * supplied value.  If not, it leaves it verbatim.
     * @param argMap The command line arguments
     * @param varNames The alpha-numeric variable names found with hashVars
     * @param configCmds Config file contents to do replacement on
     * @return
     */
    public static String replaceWildCards(HashMap<String, String> argMap,
            HashSet<String> varNames,
            String configCmds) {

        String parsedCmds = new String(configCmds);

        log("Replacing wildcard variables in config.");
        for (String s : varNames) {
            if (argMap.containsKey(s.toLowerCase())) {
                parsedCmds = parsedCmds.replaceAll("\\$" + s, argMap.get(s.toLowerCase()));
            } else {
                log(Level.WARNING, "Application arg name=\"" + s + "\" not " +
                        "specified on command line.  Leaving verbatim in raw cmds.");
            }
        }
        log("Finished replacing wildcard variables in config.");
        return parsedCmds;
    }

    public static String replaceWildCards(JApplet j, HashSet<String> varNames, String configCmds) {
        String parsedCmds = new String(configCmds);

        log("Replacing wildcard variables in config.");
        for (String s : varNames) {
            if (j.getParameter(s) != null && !j.getParameter(s).equals("")) {
                parsedCmds = parsedCmds.replaceAll("\\$" + s, j.getParameter(s));
            } else {
                log(Level.WARNING, "Applet <param name=\"" + s + "\" ...> " +
                        "not specified in html applet code. Leaving verbatim in" +
                        "raw cmds.");
            }
        }
        log("Finished replacing wildcard variables in config.");
        return parsedCmds;
    }

    /**
     * Cycle through parameters and load those applicable to a web applet
     * make sure to set config type to file (default is URL) before loading the
     * config.
     */
    public static void processParameters(Paramable p, PrintRaw pr, ConfigLoader cl) {
        for (ParameterLoader pl : ParameterLoader.values()) {
            try {
                pl.setParameter(p);
            } catch (PrintException e) {
                showMessage(p, "Error setting " +
                        pl.toString() + "(" + pl.getEnumType().toString() +
                        ") parameter while loading application. " +
                        "Printing functionality will fail.", e);
            }
        }

        // Read file contents to buffer
        try {
            cl.readFile();
        } catch (IOException e) {
            showMessage(p, "Error reading \"" + cl.getConfigPath() + "\"" + " (" +
                    cl.getConfigType().toString() + ")", e);
            System.exit(1);
        } finally {
            cl.closeStreams();
        }
    }

    public static void showMessage(String msg) {
        showMessage(null, msg, null);
    }

    public static void showMessage(Paramable p, String msg) {
        showMessage(p, msg, null);
    }

    public static void showMessage(Paramable p, String msg, Exception e) {
        showMessage(p.getComponent(), p.getMessageTitle(), msg, e);
    }

    public static void showMessage(Component c, String title, String msg, Exception e) {
        int msgType = JOptionPane.INFORMATION_MESSAGE;

        Level msgLevel = Level.INFO;
        if (c != null) {
            if (e != null) {
                msg = msg + "\n" + e.getLocalizedMessage();
                msgType = JOptionPane.ERROR_MESSAGE;
                msgLevel = Level.SEVERE;
            }
            JOptionPane.showMessageDialog(c, msg, title, msgType);
        }
        log(c.getClass().getName(), msgLevel, "Dialog: " + msg);
    }

    public static void log(Paramable p, Level lvl, String msg, Exception e) {
        log(p.getClass().getName(), lvl, msg, e);
    }

    public static void log(Paramable p, String msg, Exception e) {
        log(p, Level.SEVERE, msg, e);
    }

    public static void log(Paramable p, Exception e) {
        log(p, Level.SEVERE, p.getErrorMessage(), e);
    }

    public static void log(Paramable p, String msg) {
        log(p, Level.INFO, msg);
    }

    public static void log(Paramable p, Level lvl, String msg) {
        log(p.getClass().getName(), lvl, msg);
    }

    public static void log(String className, Level lvl, String msg, Exception e) {
        Logger.getLogger(className).log(lvl, msg, e);
    }

    public static void log(Level lvl, String msg, Exception e) {
        log(JZebraTools.class.getName(), lvl, msg, e);
    }

    public static void log(String msg, Exception e) {
        log(Level.SEVERE, msg, e);
    }

    public static void log(Exception e) {
        log("Error", e);
    }

    public static void log(String className, Level lvl, String msg) {
        Logger.getLogger(className).log(lvl, msg);
    }

    public static void log(Level lvl, String msg) {
        log(JZebraTools.class.getName(), lvl, msg);
    }

    public static void log(String msg) {
        log(Level.INFO, msg);
    }
    
    public static void log(PrintJobEvent pje) {
        Level lvl;
        String msg = "Print job ";
        switch (pje.getPrintEventType()) {
            case PrintJobEvent.DATA_TRANSFER_COMPLETE:
                lvl = Level.INFO; msg += "data transfer complete."; break;
            case PrintJobEvent.NO_MORE_EVENTS:
                lvl = Level.INFO; msg += "has no more events."; break;
            case PrintJobEvent.JOB_COMPLETE:
                lvl = Level.INFO; msg += "job complete."; break;
            case PrintJobEvent.REQUIRES_ATTENTION:
                lvl = Level.WARNING; msg += "requires attention."; break;
            case PrintJobEvent.JOB_CANCELED:
                lvl = Level.WARNING; msg += "job canceled."; break;
            case PrintJobEvent.JOB_FAILED:
                lvl = Level.SEVERE; msg += "job failed."; break;
            default: return;
        }
        JZebraTools.log(lvl, msg);
    }
}
