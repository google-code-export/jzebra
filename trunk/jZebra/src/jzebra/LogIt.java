package jzebra;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.event.PrintJobEvent;

/**
 * @author A. Tres Finocchiaro
 */
public class LogIt {
    public static void log(String className, Level lvl, String msg, Throwable t) {
        Logger.getLogger(className).log(lvl, msg, t);
    }

    public static void log(Level lvl, String msg, Throwable t) {
        log(LogIt.class.getName(), lvl, msg, t);
    }

    public static void log(String msg, Throwable t) {
        log(Level.SEVERE, msg, t);
    }
    
    public static void log(Throwable t) {
        log("Error", t);
    }

    public static void log(String className, Level lvl, String msg) {
        Logger.getLogger(className).log(lvl, msg);
    }

    public static void log(Level lvl, String msg) {
        log(LogIt.class.getName(), lvl, msg);
    }

    public static void log(String msg) {
        log(Level.INFO, msg);
    }

    public static void log(PrintJobEvent pje) {
        Level lvl;
        String msg = "Print job ";
        switch (pje.getPrintEventType()) {
            case PrintJobEvent.DATA_TRANSFER_COMPLETE:
                lvl = Level.INFO;
                msg += "data transfer complete.";
                break;
            case PrintJobEvent.NO_MORE_EVENTS:
                lvl = Level.INFO;
                msg += "has no more events.";
                break;
            case PrintJobEvent.JOB_COMPLETE:
                lvl = Level.INFO;
                msg += "job complete.";
                break;
            case PrintJobEvent.REQUIRES_ATTENTION:
                lvl = Level.WARNING;
                msg += "requires attention.";
                break;
            case PrintJobEvent.JOB_CANCELED:
                lvl = Level.WARNING;
                msg += "job canceled.";
                break;
            case PrintJobEvent.JOB_FAILED:
                lvl = Level.SEVERE;
                msg += "job failed.";
                break;
            default:
                return;
        }
        LogIt.log(lvl, msg);
    }

}
