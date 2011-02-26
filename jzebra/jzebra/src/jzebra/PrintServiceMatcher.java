/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jzebra;

import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.PrinterName;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 *
 * @author tfino
 */
public class PrintServiceMatcher {

    /**
     * Finds a printer in the PrintServices listing and returns it's respective
     * PrintService.<p>  If a PrintService is supplied, the same PrintService is
     * returned.  If an Object is supplied, it calls the "toString()" method and
     * then does a standard name search.</p>
     * @param o The object holding the name of the printer to search for.
     * @return PrintService ps for RawPrint(ps, cmds)
     */
    public static PrintService findPrinter(Object o) {
        String printerName;
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            printerName = (String)o;
        } else if (o instanceof PrintService) {
            return (PrintService)o;
        } else {
            printerName = o.toString();
        }

        // Get our print service list
        List<PrintService> printerList = Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));

        Logger.getLogger(PrintServiceMatcher.class.getName()).log(Level.INFO,
                "Printer specified: " + printerName);
        
        // Search for best match by name
        for (PrintService ps : printerList) {
            String sysPrinter = ((PrinterName) ps.getAttribute(PrinterName.class)).getValue();
            if (sysPrinter.toLowerCase().indexOf(printerName.trim().toLowerCase()) > -1) {
                Logger.getLogger(PrintServiceMatcher.class.getName()).log(Level.INFO,
                    "Printer found: " + sysPrinter);
                return ps;
            }
        }

        // Couldn't find printer
        Logger.getLogger(PrintServiceMatcher.class.getName()).log(Level.SEVERE,
                "Printer not found: " + printerName);
        return null;
    }
    
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
    }
}
