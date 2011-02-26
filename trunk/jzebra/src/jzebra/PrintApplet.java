package jzebra;

import java.applet.Applet;
import java.awt.print.PrinterException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * An invisible web applet for use with JavaScript functions
 * @author  A. Tres Finocchiaro
 */
public class PrintApplet extends Applet implements Runnable {

    public static final String VERSION = "1.1.5";
    private static final long serialVersionUID = 2787955484074291340L;
    private static final int APPEND_XML = 1;
    private static final int APPEND_RAW = 2;
    private static final int APPEND_IMAGE = 3;
    private int appendType = 0;
    private long sleep;
    private PrintService ps;
    private PrintRaw pr;
    private Print2D p2d;
    private Exception e;
    private boolean startFinding;
    private boolean doneFinding;
    private boolean startPrinting;
    private boolean donePrinting;
    private boolean startAppending;
    private boolean doneAppending;
    private boolean running;
    private boolean reprint;
    private boolean psPrint;
    private String file;
    private String xmlTag;
    private Thread thisThread;
    private String printer;
    private Charset charset = Charset.defaultCharset();
    //private String pageBreak; // For spooling pages one at a time
    private int documentsPerSpool = 0;
    private String endOfDocument;
    private String manualBreak = "%/SPOOL/%";

    /**
     * Starts the applet and runs the JavaScript listener thread
     */
    //@Override //JDK 1.6
    public void run() {
        processParameters();

        pr = new PrintRaw();
        pr.setCharset(charset);
        pr.clear();     // Initialize our print commands

        logStart();
        while (running) {
            try {
                Thread.sleep(sleep);  // Wait 100 milli before running again
                /*if (xmlFile != null && xmlTag != null) {
                append64(readXMLFile());
                xmlFile = null; xmlTag = null;
                }*/
                if (startAppending) {
                    switch (appendType) {
                        case APPEND_XML:
                            append64(readXMLFile());
                            break;
                        case APPEND_RAW:
                            append(readRawFile());
                            break;
                        case APPEND_IMAGE:
                            readImage();
                            break;
                        default: // Do nothing
                    }
                    startAppending = false;
                    doneAppending = true;
                }
                if (startFinding) {
                    logFindPrinter();
                    startFinding = false;
                    ps = PrintServiceMatcher.findPrinter(printer);
                    doneFinding = true;
                }
                if (startPrinting) {
                    logPrint();
                    try {
                        startPrinting = false;
                        pr.setPrintService(ps);

                        // PostScript style printign feature -- Added 1/22/2011
                        if (psPrint) {
                            logAndPrint(getPrint2D());
                            //getPrint2D().print(this.getGraphics(),new PageFormat(), 0);
                        } // Page Spooling Feature #1 -- Added 11/19/2010
                        else if (documentsPerSpool > 0 && endOfDocument != null && !pr.isClear() && pr.get().contains(endOfDocument)) {
                            String[] split = pr.get().split(endOfDocument);
                            int currentPage = 1;
                            pr.clear();
                            for (String s : split) {
                                pr.append(s + endOfDocument);
                                if (currentPage < documentsPerSpool) {
                                    currentPage++;
                                } else {
                                    logAndPrint(pr);
                                    currentPage = 1;
                                }
                            }

                            if (!pr.isClear()) {
                                logAndPrint(pr);
                            }

                        } // Page Spooling Feature #2 -- Added 11/19/2010
                        else if (manualBreak != null && !pr.isClear() && pr.get().contains(manualBreak)) {
                            String[] split = pr.get().split(manualBreak);
                            for (String s : split) {
                                logAndPrint(s);
                            }
                        } else {
                            logAndPrint(pr);
                        }
                    } catch (PrintException e) {
                        set(e);
                    } catch (PrinterException e) {
                        set(e);
                    } catch (UnsupportedEncodingException e) {
                        set(e);
                    } finally {
                        donePrinting = true;
                        pr.clear();
                    }
                }
            } catch (InterruptedException e) {
                set(e);
            }
        }
        logStop();
    }

    private void processParameters() {
        running = true;
        startPrinting = false;
        donePrinting = true;
        startFinding = false;
        doneFinding = true;
        startAppending = false;
        doneAppending = true;
        sleep = getParameter("sleep", 100);
        psPrint = false;
        String printer = getParameter("printer", null);
        if (printer != null) {
            findPrinter(printer);
        }
    }

    /**
     * Overrides getParameter() to allow all upper or all lowercase parameter names
     * @param name
     * @return
     */
    private String getParameter(String name, String defaultVal) {
        if (name != null) {
            String retVal = super.getParameter(name);
            retVal = isBlank(retVal) ? super.getParameter(name.toUpperCase()) : retVal;
            return isBlank(retVal) ? defaultVal : retVal;
        }
        return defaultVal;
    }

    /**
     * Same as <code>getParameter(String, String)</code> except for a <code>long</code>
     * type.
     * @param name
     * @param defaultVal
     * @return
     */
    private long getParameter(String name, long defaultVal) {
        return Long.parseLong(getParameter(name, "" + defaultVal));
    }

    /**
     * Returns true if given String is empty or null
     * @param s
     * @return
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().equals("");
    }

    public String getPrinters() {
        return PrintServiceMatcher.getPrinterListing();
    }

    /**
     * Tells jZebra to spool a new document when the raw data matches
     * <code>pageBreak</code>
     * @param pageBreak
     */
    //   @Deprecated
    //   public void setPageBreak(String pageBreak) {
    //       this.pageBreak = pageBreak;
    //   }
    public void append64(String base64) {
        try {
            pr.append(Base64.decode(base64));
        } catch (IOException e) {
            set(e);
        }
    }

    /**
     * Gets the first xml node identified by <code>tagName</code>, reads its
     * contents and appends it to the buffer.  Assumes XML content is base64
     * formatted.
     * 
     * @param xmlFile
     * @param tagName
     */
    public void appendXML(String xmlFile, String xmlTag) {
        appendFile(xmlFile, APPEND_XML);
        //this.startAppending = true;
        //this.doneAppending = false;
        //this.appendType = APPEND_XML;
        //this.file = xmlFile;
        this.xmlTag = xmlTag;
    }

    /**
     * Appends the entire contents of the specified file to the buffer
     * @param rawDataFile
     */
    public void appendFile(String rawDataFile) {
        appendFile(rawDataFile, APPEND_RAW);
        //this.startAppending = true;
        //this.doneAppending = false;
        //this.appendType = APPEND_RAW;
        //this.file = rawDataFile;
    }

    /**
     * 
     * @param imageFile
     */
    public void appendImage(String imageFile) {
        appendFile(imageFile, APPEND_IMAGE);
        
        //this.startAppending = true;
        //this.doneAppending = false;
        //this.appendType = APPEND_IMAGE;
        //this.file = imageFile;
    }

    /**
     * Appends a file of the specified type
     * @param url
     * @param appendType
     */
    private void appendFile(String file, int appendType) {
        this.startAppending = true;
        this.doneAppending = false;
        this.appendType = appendType;
        this.file = file;
    }

    /**
     * Can't seem to get this to work, remvoed from sample.html
     * @param orientation
     */
    public void setImageOrientation(String orientation) {
        getPrint2D().setOrientation(orientation);
    }

    // Due to applet security, can only be invoked by run() thread
    private String readXMLFile() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            Document doc;
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);
            doc.getDocumentElement().normalize();
            LogIt.log("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName(xmlTag);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            } else {
                LogIt.log("Node \"" + xmlTag + "\" could not be found in XML file specified");
            }
        } catch (Exception e) {
            LogIt.log(Level.WARNING, "Error reading/parsing specified XML file", e);
        }
        return "";
    }

    // Due to applet security, can only be invoked by run() thread
    private void readImage() {
        try {
            getPrint2D().setImage(ImageIO.read(new URL(file)));
        } catch (IOException ex) {
            LogIt.log(Level.WARNING, "Error reading specified image", ex);
        }
    }

    // Use this instead of calling p2d directly.  This will allow 2d graphics
    // to only be used when absolutely needed
    private Print2D getPrint2D() {
        if (this.p2d == null) {
            this.p2d = new Print2D();
            this.p2d.setPrintParameters(this);
        }
        return p2d;
    }

    // Due to applet security, can only be invoked by run() thread
    private String readRawFile() {
        String rawData = "";
        try {
            byte[] buffer = new byte[512];
            DataInputStream in = new DataInputStream(new URL(file).openStream());
            //String inputLine;
            while (true) {
                int len = in.read(buffer);
                if (len == -1) {
                    break;
                }
                rawData += new String(buffer, 0, len, charset.name());
            }
            in.close();
        } catch (Exception e) {
            LogIt.log(Level.WARNING, "Error reading/parsing specified RAW file", e);
        }
        return rawData;
    }

    /**
     * Prints the appended data without clearing the print buffer afterward.
     */
    public void printPersistent() {
        startPrinting = true;
        donePrinting = false;
        reprint = true;
    }

    public void append(String s) {
        try {
            pr.append(s.getBytes(charset.name()));
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(Level.WARNING, "Could not append using specified charset encoding: "
                    + charset + ". Using default.", e);
            pr.append(s);
        }
    }
    
    public void replace(String tag, String value) {
        replaceAll(tag, value);
    }
    
    public void replaceAll(String tag, String value) {
        pr.set(pr.get().replaceAll(tag, value));
    }
    
    public void replaceFirst(String tag, String value) {
        pr.set(pr.get().replaceFirst(tag, value));
    }

    public void set(String s) {
        pr.set(s);
    }

    public void clear() {
        pr.clear();
    }

    /**
     * Perform PrintRaw.print() and handles the output of exceptions and
     * debugging.  Important:  print() clears the buffer after printing.  Use
     * printPersistent() to save the buffer to be used/appended to later.
     */
    public void print() {
        startPrinting = true;
        donePrinting = false;
        reprint = false;
    }

    public void printPS() {
        psPrint = true;
        startPrinting = true;
        donePrinting = false;
        reprint = false;
    }

    /** Initializes the applet JZebraApplet */
    @Override
    public void init() {
        try {
            thisThread = new Thread(this);
            thisThread.start();
        } catch (Exception e) {
            set(e);
        }
    }

    @Override
    public void destroy() {
        running = false;
        this.stop();
        super.destroy();
    }

    /**
     * Creates the print service by iterating through printers until finding
     * matching printer containing "printerName" in its description
     * @param printerName
     * @return
     */
    public void findPrinter(String printer) {
        this.startFinding = true;
        this.doneFinding = false;
        this.printer = printer;
    }

    public boolean isDoneFinding() {
        return doneFinding;
    }

    public boolean isDonePrinting() {
        return donePrinting;
    }

    public boolean isDoneAppending() {
        return doneAppending;
    }

    public String getPrinter() {
        return ps.getName();
    }

    /**
     * Returns the PrintRaw object associated with this applet, if any.  Returns
     * null if none is set.
     * @return
     */
    public PrintRaw getPrintRaw() {
        return pr;
    }

    /**
     * Returns the PrintService object associated with this applet, if any.  Returns
     * null if none is set.
     * @return
     */
    public PrintService getPrintService() {
        return ps;
    }

    /**
     * Returns the PrintService's name (the printer name) associated with this
     * applet, if any.  Returns null if none is set.
     * @return
     */
    public String getPrinterName() {
        return ps == null ? null : ps.getName();
    }

    public Exception getException() {
        return e;
    }

    public void clearException() {
        this.e = null;
    }

    public String getExceptionMessage() {
        return e.getLocalizedMessage();
    }

    public long getSleepTime() {
        return sleep;
    }

    public String getVersion() {
        return VERSION;
    }

    /**
     * Sets the time the listener thread will wait between actions
     * @param sleep
     */
    public void setSleepTime(long sleep) {
        this.sleep = sleep;
    }

    public String getEndOfDocument() {
        return endOfDocument;
    }

    public void setEndOfDocument(String endOfPage) {
        this.endOfDocument = endOfPage;
    }

    public String getManualBreak() {
        return manualBreak;
    }

    public void setManualBreak(String manualBreak) {
        this.manualBreak = manualBreak;
    }

    public int getDocumentsPerSpool() {
        return documentsPerSpool;
    }

    public void setDocumentsPerSpool(int pagesPer) {
        this.documentsPerSpool = pagesPer;
    }

    public void setJobName(String jobName) {
        pr.setJobName(jobName);
    }

    public String getJobName() {
        return pr.getJobName();
    }

    private void set(Exception e) {
        this.e = e;
        LogIt.log(e);
    }

    private void logStart() {
        LogIt.log("jZebra " + VERSION);
        LogIt.log("===== JAVASCRIPT LISTENER THREAD STARTED =====");
    }

    private void logStop() {
        LogIt.log("===== JAVASCRIPT LISTENER THREAD STOPPED =====");
    }

    private void logPrint() {
        LogIt.log("===== SENDING DATA TO THE PRINTER =====");
    }

    private void logFindPrinter() {
        LogIt.log("===== SEARCHING FOR PRINTER =====");
    }

    private void logCommands(PrintRaw pr) {
        logCommands(pr.get());
    }

    private void logCommands(String commands) {
        LogIt.log("\r\n\r\n" + commands + "\r\n\r\n");
    }

    private void logAndPrint(PrintRaw pr) throws PrintException, InterruptedException, UnsupportedEncodingException {
        logCommands(pr);
        if (reprint) {
            pr.print();
        } else {
            pr.print();
            pr.clear();
        }
    }

    private void logAndPrint(Print2D p2d) throws PrinterException {
        logCommands("    <" + file + ">");
        p2d.print();
        psPrint = false;
    }

    private void logAndPrint(String commands) throws PrintException, InterruptedException, UnsupportedEncodingException {
        logCommands(commands);
        pr.print(commands);

    }

    public void setEncoding(String charset) {
        // Example:  Charset.forName("US-ASCII");
        System.out.println("Default charset encoding: " + Charset.defaultCharset().name());
        try {
            this.charset = Charset.forName(charset);
            LogIt.log("Current charset encoding: " + this.charset.name());
        } catch (IllegalCharsetNameException e) {
            LogIt.log(Level.WARNING, "Could not find specified charset encoding: "
                    + charset + ". Using default.", e);
        }

    }

    public String getEncoding() {
        return this.charset.displayName();
    }
}
