package jzebra;

import java.applet.Applet;
import java.awt.print.PrinterException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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

    public static final String VERSION = "1.3.0";
    private static final long serialVersionUID = 2787955484074291340L;
    public static final int APPEND_XML = 1;
    public static final int APPEND_RAW = 2;
    public static final int APPEND_IMAGE = 3;
    public static final int APPEND_IMAGE_PS = 4;
    public static final int APPEND_PDF = 8;
    public static final int APPEND_HTML = 16;
    private LanguageType lang;
    private int appendType;
    private long sleep;
    private PrintService ps;
    private PrintRaw pr;
    private PrintPostScript printPS;
    private PrintHTML printHTML;
    private Throwable t;
    private boolean startFinding;
    private boolean doneFinding;
    private boolean startPrinting;
    private boolean donePrinting;
    private boolean startAppending;
    private boolean doneAppending;
    private boolean running;
    private boolean reprint;
    private boolean psPrint;
    private boolean htmlPrint;
    //private double[] psMargin;
    private String file;
    private String xmlTag;
    private Thread thisThread;
    private String printer;
    private Charset charset = Charset.defaultCharset();
    //private String pageBreak; // For spooling pages one at a time
    private int documentsPerSpool = 0;
    private String endOfDocument;
//    private String manualBreak = "%/SPOOL/%";

    /**
     * Create a privileged thread that will listen for JavaScript events
     * @since 1.1.7
     */
    //@Override
    public void run() {
        logStart();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                //@Override

                public Object run() throws Exception {

                    startJavaScriptListener();
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            LogIt.log("Error starting main JavaScript thread.  All else will fail.", e);
            set(e);
        } finally {
            logStop();
        }
    }

    /**
     * TODO - Implement ThreadPoolExecutor.  Sample provided:
     */
    /*
     * 
    public void listen() {
    BlockingQueue queue = new SynchronousQueue();
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
    1, Session.getMaxSessionCount(), 100, TimeUnit.SECONDS, queue);
    while (true) {
    try {
    queue.submit(new Session(database, serverSocket.accept()));
    } catch (IOException ex) { 
    ex.printStackTrace();
    }
    }
    }
     */
    /**
     * Starts the Applet and runs the JavaScript listener thread
     */
    //@Override //JDK 1.6
    private void startJavaScriptListener() {
        processParameters();
        pr = new PrintRaw();
        pr.setCharset(charset);
        pr.clear();     // Initialize our print commands

        //logStart();
        while (running) {
            try {
                Thread.sleep(sleep);  // Wait 100 milli before running again
                /*if (xmlFile != null && xmlTag != null) {
                append64(readXMLFile());
                xmlFile = null; xmlTag = null;
                }*/
                if (startAppending) {
                    try {
                        switch (appendType) {
                            case APPEND_HTML:
                                appendHTML(readRawFile());
                            case APPEND_XML:
                                append64(readXMLFile());
                                break;
                            case APPEND_RAW:
                                append(readRawFile());
                                break;
                            case APPEND_IMAGE_PS:
                                readImage();
                                break;
                            case APPEND_IMAGE:
                                if (isBase64Image(file)) {
                                    append(ImageWrapper.getImage(Base64.decode(file.split(",")[1]), lang, charset));
                                } else {
                                    append(ImageWrapper.getImage(file, lang, charset));
                                }
                                break;
                            case APPEND_PDF:
                                readBinaryFile();
                                break;
                            default: // Do nothing
                        }
                    } catch (Throwable t) {
                        LogIt.log("Error appending " + appendType + " data", t);
                        set(t);
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

                        // PostScript style printing feature for images or PDF files
                        if (htmlPrint) {
                            logAndPrint(getPrintHTML());
                        } else if (psPrint) {
                            logAndPrint(getPrintPS());
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

                        } // Page Spooling Feature #2 -- Added 11/19/2010 -- Removed 2/28/2012 due to no use
/*                        else if (manualBreak != null && !pr.isClear() && pr.get().contains(manualBreak)) {
                            String[] split = pr.get().split(manualBreak);
                            for (String s : split) {
                                logAndPrint(s);
                            }
                        }*/ else {
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
        //logStop();
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
        appendType = 0;
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
            try {
                String retVal = super.getParameter(name);
                retVal = isBlank(retVal) ? super.getParameter(name.toUpperCase()) : retVal;
                return isBlank(retVal) ? defaultVal : retVal;
            } catch (NullPointerException e) {
                return defaultVal;
            }
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
    
    public void appendHTMLFile(String htmlFile) {
        this.appendType = APPEND_HTML;
        this.appendFromThread(htmlFile, appendType);
        //throw new UnsupportedOperationException("Sorry, not yet supported.");
    }
    
    public void appendHtmlFile(String htmlFile) {
        this.appendHTMLFile(htmlFile);
    }
    
    public void appendHtml(String html) {
        this.appendHTML(html);
    }
    
    public void appendHTML(String html) {
        getPrintHTML().append(html);
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
        appendFromThread(xmlFile, APPEND_XML);
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
        appendFromThread(rawDataFile, APPEND_RAW);
    }

    /**
     * 
     * @param imageFile
     */
    public void appendImage(String imageFile) {
        appendFromThread(imageFile, APPEND_IMAGE_PS);
    }

    public void appendPDF(String pdfFile) {
        appendFromThread(pdfFile, APPEND_PDF);
    }

    /**
     *
     * @param imageFile
     * @param appendType Use "ZPLII"
     */
    public void appendImage(String imageFile, String appendType) {
        lang = LanguageType.getType(appendType);
        switch (lang) {
            case CPCL:
            case ESCP:
            case ESCP2:
            case ZPLII:
                appendFromThread(imageFile, APPEND_IMAGE);
                break;
            default:
                LogIt.log(new UnsupportedOperationException("Image conversion to "
                        + "format \"" + appendType + "\" not yet supported."));
        }
    }

    /**
     * Appends a file of the specified type
     * @param url
     * @param appendType
     */
    private void appendFromThread(String file, int appendType) {
        this.startAppending = true;
        this.doneAppending = false;
        this.appendType = appendType;
        this.file = file;
    }

    /**
     * Can't seem to get this to work, removed from sample.html
     * @param orientation
     */
    @Deprecated
    public void setImageOrientation(String orientation) {
        getPrintPS().setOrientation(orientation);
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

    public void printToFile() {
        printToFile(null);
    }

    public void printToFile(String outputPath) {
        if (outputPath != null && !outputPath.equals("")) {
            this.pr.setOutputPath(outputPath);
        }
        this.print();
    }

    // Due to applet security, can only be invoked by run() thread
    private void readImage() {
        try {
            // Use the in-line base64 content as our image
            if (isBase64Image(file)) {
                getPrintPS().setImage(Base64.decode(file.split(",")[1]));
            } else {
                getPrintPS().setImage(ImageIO.read(new URL(file)));
            }
        } catch (IOException ex) {
            LogIt.log(Level.WARNING, "Error reading specified image", ex);
        }
    }

    private boolean isBase64Image(String path) {
        return path.startsWith("data:image/") && path.contains(";base64,");
    }

    // Use this instead of calling p2d directly.  This will allow 2d graphics
    // to only be used when absolutely needed
    private PrintPostScript getPrintPS() {
        if (this.printPS == null) {
            this.printPS = new PrintPostScript();
            this.printPS.setPrintParameters(this);
        }
        return printPS;
    }
    
    private PrintHTML getPrintHTML() {
        if (this.printHTML == null) {
            this.printHTML = new PrintHTML();
            this.printHTML.setPrintParameters(this);
        }
        return printHTML;
    }

    /*
    public double[] getPSMargin() {
    return psMargin;
    }
    
    public void setPSMargin(int psMargin) {
    this.psMargin = new double[]{psMargin};
    }
    
    public void setPSMargin(double psMargin) {
    this.psMargin = new double[]{psMargin};
    }
    
    public void setPSMargin(int top, int left, int bottom, int right) {
    this.psMargin = new double[]{top, left, bottom, right};
    }
    
    public void setPSMargin(double top, double left, double bottom, double right) {
    this.psMargin = new double[]{top, left, bottom, right};
    }*/
    /**
     * Reads a binary file (i.e. PDF) from URL to a ByteBuffer.  This is later appended
     * to the applet, but needs a renderer capable of printing it to PostScript
     * @return
     * @throws IOException 
     */
    public void readBinaryFile() {
        ByteBuffer data = null;
        try {
            URLConnection con = new URL(file).openConnection();
            InputStream in = con.getInputStream();
            int size = con.getContentLength();

            ByteArrayOutputStream out;
            if (size != -1) {
                out = new ByteArrayOutputStream(size);
            } else {
                out = new ByteArrayOutputStream(20480); // Pick some appropriate size
            }

            byte[] buffer = new byte[512];
            while (true) {
                int len = in.read(buffer);
                if (len == -1) {
                    break;
                }
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();

            byte[] array = out.toByteArray();

            //Lines below used to test if file is corrupt
            //FileOutputStream fos = new FileOutputStream("C:\\abc.pdf");
            //fos.write(array);
            //fos.close();

            data = ByteBuffer.wrap(array);

        } catch (Exception e) {
            LogIt.log(Level.WARNING, "Error reading/parsing specified PDF file", e);
        }

        getPrintPS().setPDF(data);

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
                    + charset + ". Using default.", t);
            pr.append(s);
        }
    }

    /**
     * For very special scenarios where the print listener isn't running yet.  This
     * is usually caused by calling from unorthodox means, such as a third party
     * applet or API
     *
    private void checkListener() {
    if (thisThread == null) {
    LogIt.log(Level.WARNING, "It appears the applet was not loaded "
    + "in a web browser.  Starting \"init();\" automatically.");
    this.init();
    }
    /*if (pr == null) {
    LogIt.log(Level.WARNING, "It appears the applet was not started "
    + "through a web browser.  Starting \"run();\" automatically.");
    this.run();
    }*
    }*/
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
    
    public void printHTML() {
        htmlPrint = true;
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

    /**
     * Returns the PrintService's name (the printer name) associated with this
     * applet, if any.  Returns null if none is set.
     * @return
     */
    public String getPrinter() {
        return ps == null ? null : ps.getName();
        //return ps.getName();
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
    @Deprecated
    public String getPrinterName() {
        LogIt.log(Level.WARNING, "Function \"getPrinterName()\" has been deprecated since v. 1.2.3."
                + "  Please use \"getPrinter()\" instead.");
        return getPrinter();
    }

    public Throwable getError() {
        return getException();
    }

    public Throwable getException() {
        return t;
    }

    public void clearException() {
        this.t = null;
    }

    public String getExceptionMessage() {
        return t.getLocalizedMessage();
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
    
    public void setPrinter(int index) {
        this.ps = PrintServiceMatcher.getPrinterList()[index];
        LogIt.log("Printer set to index: " + index + ",  Name: " + ps.getName());
    }

/*    public String getManualBreak() {
        return manualBreak;
    }*/

/*    public void setManualBreak(String manualBreak) {
        this.manualBreak = manualBreak;
    }*/

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

    private void set(Throwable t) {
        this.t = t;
        LogIt.log(t);
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
    
    private void logCommands(PrintHTML ph) {
        logCommands(ph.get());
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

    private void logAndPrint(PrintPostScript printPS) throws PrinterException {
        logCommands("    <<" + file + ">>");
        printPS.print();
        psPrint = false;
    }

    private void logAndPrint(PrintHTML printHTML) throws PrinterException {
        if (file != null) {
            logCommands("    <<" + file + ">>");
        }
        logCommands(printHTML);

        printHTML.print();
        htmlPrint = false;
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
            this.pr.setCharset(Charset.forName(charset));
            LogIt.log("Current applet charset encoding: " + this.charset.name());
        } catch (IllegalCharsetNameException e) {
            LogIt.log(Level.WARNING, "Could not find specified charset encoding: "
                    + charset + ". Using default.", e);
        }

    }

    public String getEncoding() {
        return this.charset.displayName();
    }
}
