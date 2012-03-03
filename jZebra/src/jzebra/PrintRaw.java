/*
 * Main printing class
 */
package jzebra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
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

    private final static String ERR = "jzebra.PrintRaw.print() failed.";
    private final AtomicReference<DocFlavor> docFlavor = new AtomicReference<DocFlavor>(DocFlavor.BYTE_ARRAY.AUTOSENSE);
    private final AtomicReference<DocAttributeSet> docAttr = new AtomicReference<DocAttributeSet>(null);
    private final AtomicReference<PrintRequestAttributeSet> reqAttr = new AtomicReference<PrintRequestAttributeSet>(new HashPrintRequestAttributeSet());
    private final AtomicReference<PrintService> ps = new AtomicReference<PrintService>(null);
    private final AtomicReference<String> rawCmds = new AtomicReference<String>(null);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);
    private final AtomicReference<Charset> charset = new AtomicReference<Charset>(Charset.defaultCharset());
    private final AtomicReference<String> jobName = new AtomicReference<String>("jZebra Raw Printing");
    private final AtomicReference<String> outputPath = new AtomicReference<String>(null);

    public PrintRaw() {
    }

    public PrintRaw(PrintService ps, String rawCmds) {
        this.ps.set(ps);
        this.rawCmds.set(rawCmds);
    }

    public PrintRaw(PrintService ps, String rawCmds, DocFlavor docFlavor,
            DocAttributeSet docAttr, PrintRequestAttributeSet reqAttr, Charset charset) {
        this.ps.set(ps);
        this.rawCmds.set(rawCmds);
        this.docFlavor.set(docFlavor);
        this.docAttr.set(docAttr);
        this.reqAttr.set(reqAttr);
        this.charset.set(charset);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath.set(outputPath);
    }

    public boolean print(String rawCmds) throws PrintException, InterruptedException, UnsupportedEncodingException {
        this.set(rawCmds);
        return print();
    }

    public boolean printToFile() throws PrintException {
        LogIt.log("Printing to file (placeholder)");

        //File f = new File(outputPath.get());
        /*if (!f.exists()) {
            throw new PrintException(ERR + ": File does not exist " + outputPath.get());
        }
        else
        if (!f.canWrite()) {
            throw new PrintException(ERR + ": Error writing to " + outputPath.get());
        }*/
        
        try { 
            OutputStream out = new FileOutputStream(outputPath.get());
            out.write(rawCmds.get().getBytes(charset.get().name()));
            out.close();

        } catch (Exception e) {
            throw new PrintException(ERR + ": " + e.getLocalizedMessage());
        }
        
        //isFinished.set(true);
        return true;
    }

    /**
     * Generates the raw printjob that gets sent to the printer.  
     * 
     * @param ps The PrintService object
     * @param commands The RAW commands to be sent directly to the printer
     * @return True if print job created successfull
     * @throws javax.print.PrintException
     */
    public boolean print() throws PrintException, InterruptedException, UnsupportedEncodingException {
        if (ps.get() == null) {
            throw new NullPrintServiceException(ERR);
        } else if (rawCmds.get() == null) {
            throw new NullCommandException(ERR);
        } else if (outputPath.get() != null) {
            return printToFile();
        }
        SimpleDoc doc = new SimpleDoc(rawCmds.get().getBytes(charset.get().name()), docFlavor.get(), docAttr.get());

        reqAttr.get().add(new JobName(jobName.get(), Locale.getDefault()));
        DocPrintJob pj = ps.get().createPrintJob();
        pj.addPrintJobListener(new PrintJobListener() {

            //@Override //JDK 1.6
            public void printDataTransferCompleted(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished.set(true);
            }

            //@Override //JDK 1.6
            public void printJobCompleted(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished.set(true);
            }

            //@Override //JDK 1.6
            public void printJobFailed(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished.set(true);
            }

            //@Override //JDK 1.6
            public void printJobCanceled(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished.set(true);
            }

            //@Override //JDK 1.6
            public void printJobNoMoreEvents(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished.set(true);
            }

            //@Override //JDK 1.6
            public void printJobRequiresAttention(PrintJobEvent pje) {
                LogIt.log(pje);
            }
        });

        LogIt.log("Sending print job to printer: \"" + ps.get().getName() + "\"");
        pj.print(doc, reqAttr.get());

        while (!isFinished.get()) {
            Thread.sleep(100);
        }

        LogIt.log("Print job received by printer: \"" + ps.get().getName() + "\"");

        //clear(); - Ver 1.0.8+ : Should be done from Applet instead now
        return true;
    }

    // public void reprint() throws PrintException, InterruptedException {
    //     print(reprint.get() == null ? rawCmds.get() : reprint.get());
    // }
    /**
     * Convenience method for RawPrint constructor and print method
     *
     * @param ps The PrintService object
     * @param commands The RAW commands to be sent directly to the printer
     * @return True if print job created successfull
     * @throws javax.print.PrintException
     */
    public static boolean print(PrintService ps, String rawCmds) throws PrintException, InterruptedException, UnsupportedEncodingException {
        PrintRaw p = new PrintRaw(ps, rawCmds);
        return p.print();
    }

    public DocAttributeSet getDocAttributeSet() {
        return docAttr.get();
    }

    public void setDocAttributeSet(DocAttributeSet docAttr) {
        this.docAttr.set(docAttr);
    }

    public DocFlavor getDocFlavor() {
        return docFlavor.get();
    }

    public void setDocFlavor(DocFlavor docFlavor) {
        this.docFlavor.set(docFlavor);
    }

    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        return reqAttr.get();
    }

    public void setPrintRequestAttributeSet(PrintRequestAttributeSet reqAttr) {
        this.reqAttr.set(reqAttr);
    }

    /**
     * Returns the <code>PrintService</code> used internally to the <code>PrintRaw</code>
     * object.
     * @return
     */
    public PrintService getPrintService() {
        return ps.get();
    }

    /**
     * Sets the <code>PrintService</code> used internally to the <code>PrintRaw</code>
     * object.
     * @param ps
     */
    public void setPrintService(PrintService ps) {
        this.ps.set(ps);
    }

    /**
     * Sets the raw print data, overriding any existing data
     * @param s
     */
    public void set(String s) {
        this.rawCmds.set(s);
    }

    /**
     * Returns the raw print data as a <code>String</code>
     * @return
     */
    public String get() {
        return this.rawCmds.get();
    }

    /**
     * Sets the raw print data (or print commands) to blank <scode>String</code>
     */
    public void clear() {
        try {
            this.rawCmds.set(new String("".getBytes(charset.get().name())));
        } catch (UnsupportedEncodingException e) {
            this.rawCmds.set("");
        }
    }

    /**
     * Append the specified <code>String</code> data to the raw stream of data
     * @param s
     */
    public void append(String s) {
        this.rawCmds.set(this.rawCmds.get().concat(s));
    }

    /**
     * Append the <code>byte</code> array data to the raw stream of data
     * @param b
     */
    public void append(byte[] b) throws UnsupportedEncodingException {
        append(new String(b, charset.get().name()));
    }

    /**
     * Sets the <code>Charset</code> (character set) to use, example "US-ASCII" for use when decoding
     * byte arrays.  TODO:  Test this parameter.
     * @param charset
     */
    public void setCharset(Charset charset) {
        this.charset.set(charset);
        LogIt.log("Current printer charset encoding: " + charset.name());
    }

    /**
     * Return the character set, example "US-ASCII" for use when decoding byte
     * arrays.  TODO:  Test this parameter.
     * @return
     */
    public Charset getCharset() {
        return this.charset.get();
    }

    /**
     * Returns whether or not the print data is clear.  This usually happens
     * shortly after a print, or when <code>clear()</code> is explicitely called.
     * @return
     */
    public boolean isClear() {
        try {
            return this.rawCmds.get().getBytes(charset.get().name()).length == 0;
        } catch (UnsupportedEncodingException e) {
            return this.rawCmds.get().length() == 0;
        }
    }

    public void setJobName(String jobName) {
        this.jobName.set(jobName);
    }

    public String getJobName() {
        return this.jobName.get();
    }
}
