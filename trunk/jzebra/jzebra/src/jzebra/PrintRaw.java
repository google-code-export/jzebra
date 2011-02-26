/*
 * Main printing class
 */

package jzebra;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import jzebra.exception.NullCommandException;
import jzebra.exception.NullPrintServiceException;

/**
 * Sends raw data to the printer, overriding your operating system's print
 * driver.  Most usefull for printers such as zebra card or barcode printers.
 * 
 * @author A. Tres Finocchiaro
 */
public class PrintRaw {
    String err = "PrintRaw.print() failed.";
    DocFlavor docFlavor;
    DocAttributeSet docAttr;
    PrintRequestAttributeSet reqAttr;

    PrintService ps;
    String rawCmds;
    
    public PrintRaw() {
        this.ps = null;
        this.rawCmds = null;
        docFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        docAttr = null;
        reqAttr = null; 
    }
    
    public PrintRaw(PrintService ps, String rawCmds) {
        this.ps = ps;
        this.rawCmds = rawCmds;
        docFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        docAttr = null;
        reqAttr = null;
    }

    public PrintRaw(PrintService ps, String rawCmds, DocFlavor docFlavor,
            DocAttributeSet docAttr, PrintRequestAttributeSet reqAttr) {
        this.ps = ps;
        this.rawCmds = rawCmds;
        this.docFlavor = docFlavor;
        this.docAttr = docAttr;
        this.reqAttr = reqAttr;
    }
    /**
     * Generates the raw printjob that gets sent to the printer.  
     * 
     * @param ps The PrintService object
     * @param commands The RAW commands to be sent directly to the printer
     * @return True if print job created successfull
     * @throws javax.print.PrintException
     */
    public boolean print() throws PrintException {
        if (ps == null) {
            throw new NullPrintServiceException(err);
        } else if (rawCmds == null) {
            throw new NullCommandException(err);
        }
        SimpleDoc doc = new SimpleDoc(rawCmds.getBytes(), docFlavor, docAttr);
        DocPrintJob pj = ps.createPrintJob();
        pj.addPrintJobListener(new PrintJobListener() {
            public void printDataTransferCompleted(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            public void printJobCompleted(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            public void printJobFailed(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            public void printJobCanceled(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            public void printJobNoMoreEvents(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            public void printJobRequiresAttention(PrintJobEvent pje) {
                JZebraTools.log(pje);
            }
            
        });
         
        JZebraTools.log("Sending print job to printer: \"" + ps.getName() + "\"");
        pj.print(doc, reqAttr);
        return true;
    }
    
    
    
    /**
     * Convenience method for RawPrint constructor and print method
     *
     * @param ps The PrintService object
     * @param commands The RAW commands to be sent directly to the printer
     * @return True if print job created successfull
     * @throws javax.print.PrintException
     */
    public static boolean print(PrintService ps, String rawCmds) throws PrintException {
        PrintRaw p = new PrintRaw(ps, rawCmds);
        return p.print();
    }
    
    
    public DocAttributeSet getDocAttributeSet() {
        return docAttr;
    }

    public void setDocAttributeSet(DocAttributeSet docAttr) {
        this.docAttr = docAttr;
    }

    public DocFlavor getDocFlavor() {
        return docFlavor;
    }

    public void setDocFlavor(DocFlavor docFlavor) {
        this.docFlavor = docFlavor;
    }

    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        return reqAttr;
    }

    public void setPrintRequestAttributeSet(PrintRequestAttributeSet reqAttr) {
        this.reqAttr = reqAttr;
    }
    
    public PrintService getPrintService() {
        return ps;
    }

    public void setPrintService(PrintService ps) {
        this.ps = ps;
    }

    public String getRawCmds() {
        return rawCmds;
    }

    public void setRawCmds(String rawCmds) {
        this.rawCmds = rawCmds;
    }
}
