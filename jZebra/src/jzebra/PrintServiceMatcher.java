/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jzebra;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;

/**
 *
 * @author tfino
 */
public class PrintServiceMatcher {

    private static PrintService[] printers = null;
    private static String printerListing = "";

    /**
     * Finds a printer in the PrintServices listing and returns it's respective
     * PrintService.<p>  If a PrintService is supplied, the same PrintService is
     * returned.  If an Object is supplied, it calls the "toString()" method and
     * then does a standard name search.</p>
     * @param o The object holding the name of the printer to search for.
     * @return PrintService ps for RawPrint(ps, cmds)
     */
    public static PrintService findPrinter(Object o) {
        PrintService exact = null;
        PrintService begins = null;
        PrintService partial = null;
        
        String printerName;
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            printerName = (String) o;
        } else if (o instanceof PrintService) {
            return (PrintService) o;
        } else {
            printerName = o.toString();
        }

        // Get our print service list
        getPrinterList();

        LogIt.log(Level.INFO, "Found " + printers.length + " attached printers.");
        LogIt.log(Level.INFO, "Printer specified: " + printerName);

        // Search for best match by name
        for (PrintService ps : printers) {
            String sysPrinter = ((PrinterName) ps.getAttribute(PrinterName.class)).getValue();

            // TODO:  Test logics
            Pattern p1 = Pattern.compile("\\b" + printerName + "\\b", Pattern.CASE_INSENSITIVE);
            Pattern p2 = Pattern.compile("\\b" + printerName, Pattern.CASE_INSENSITIVE);
            Pattern p3 = Pattern.compile(printerName, Pattern.CASE_INSENSITIVE);
            
            
            Matcher m1 = p1.matcher(sysPrinter);
            Matcher m2 = p2.matcher(sysPrinter);
            Matcher m3 = p3.matcher(sysPrinter);
            
            if (m1.find()) {
                exact = ps;
                LogIt.log("Printer name match: " + sysPrinter);
            } else if (m2.find()) {
                begins = ps;
                LogIt.log("Printer name beginning match: " + sysPrinter);
            } else if (m3.find()) {
                begins = ps;
                LogIt.log("Printer name partial match: " + sysPrinter);
            }
            
            /*if (sysPrinter.toLowerCase().indexOf(printerName.trim().toLowerCase()) > -1) {
                Logger.getLogger(PrintServiceMatcher.class.getName()).log(Level.INFO,
                        "Printer found: " + sysPrinter);
                partial = ps;
            }*/
        }
        
        // Return closest match
        if (exact != null) {
            LogIt.log("Using best match: " + exact.getName());
            return exact;
        } else if (begins != null) {
            LogIt.log("Using best match: " + begins.getName());
            return begins;
        } else if (partial != null) {
            LogIt.log("Using best match: " + partial.getName());
            return begins;
        }

        // Couldn't find printer
        LogIt.log(Level.WARNING, "Printer not found: " + printerName);
        return null;
    }

    public static PrintService[] getPrinterList() {
        return getPrinterArray(false);
    }

    public static PrintService[] getPrinterArray(boolean forceSearch) {
        if (forceSearch || printers == null || printers.length == 0) {
            /*printerArrayList = Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));
            
            printerListing = "";
            for (PrintService ps : printerArrayList) {
            String sysPrinter = ((PrinterName) ps.getAttribute(PrinterName.class)).getValue();
            printerListing = printerArrayList + "," + sysPrinter;
            }
            
            // Remove last comma
            if (printerListing.endsWith(",")) {
            printerListing = printerListing.substring(0, printerListing.length() - 1);
            }
            
            // Dear Win JRE, why oh why do you put brackets around the printer names?
            if (printerListing.endsWith("]")) {
            printerListing = printerListing.substring(0, printerListing.length() - 1);
            }
            if (printerListing.startsWith("[")) {
            printerListing = printerListing.substring(1, printerListing.length());
            }*/

            printerListing = "";
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                   
            for (int i = 0; i < services.length; i++) {
                PrintServiceAttributeSet psa = services[i].getAttributes();
                printerListing  = printerListing  + psa.get(PrinterName.class);
                if (i != (services.length - 1)) {
                    printerListing  = printerListing  + ",";
                }
            }

            printers = services;
        }
        
        return printers;
    }

    /**
     * Returns a CSV format of printer names, convenient for JavaScript
     * @return
     */
    public static String getPrinterListing() {
        return printerListing;
    }

    /*
    public static boolean populateComponent(Object o) {
    List<PrintService> printers = Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));
    if (o instanceof JComboBox) {
    for (PrintService p : printers) {
    ((JComboBox)o).addItem(p);
    }
    }
    else if (o instanceof JList) {
    for (PrintService p : printers) {
    ((DefaultListModel)((JList)o).getModel()).addElement(p);
    }
    }
    else {
    return false;
    }
    return true;
    }*/
    /*
    public static boolean populateComponent(Object o) {
    List<PrintService> printers = Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));
    if (o instanceof JComboBox) {
    for (PrintService p : printers) {
    ((JComboBox)o).addItem(p);
    }
    }
    else if (o instanceof JList) {
    for (PrintService p : printers) {
    ((DefaultListModel)((JList)o).getModel()).addElement(p);
    }
    }
    else {
    return false;
    }
    return true;
    }*/
}
