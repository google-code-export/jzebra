/*
 * The main printing class.  Does the printer connection, command parsing
 * etc.
 * 
 * Liscence GPL 3.0+
 * 
 */


package jzebra;

import java.awt.HeadlessException;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.standard.PrinterName;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * 
 * Takes string commands intended for Zebra or Eltron type printers
 * and sends them directly to the printer in RAW mode.<p>
 * 
 * Using in your application is as easy as:
 * <p><blockquote><pre>
 *  PrinterService ps = JZebra.getBestPrinter("Eltron");
 *  Zebra.print(ps, "A\r");
 * </pre></blockquote><p>
  
 * @author A. Tres Finocchiaro
 */
public class JZebra {
    /**
     * Reads a file line by line and returns the entire file as a String.
     * Each line is separated by "\r\n" linebreaks.
     * 
     * Used for two purposes, reading a printer config file and reading the 
     * manpage file for when the "--help" options is specified.
     * 
     * @param s The file to read from
     * @return A string containg the file's contents
     */
    
    private static boolean isHeadless = false;
    private static PrintService ps = null;
    private static String commands = null;

    public static String readFile(String file) {
        InputStream is = null;
        BufferedReader br = null;
        String line;
        String data = "";
        
        try { 
            is = JZebraView.class.getResourceAsStream(file);
            if (isResource(file))
                br = new BufferedReader(new InputStreamReader(is));
            else if (isFile(file))
                br = new BufferedReader(new FileReader(file));
            else
                return file;
            while (null != (line = br.readLine())) {
                data += line + "\r\n";
                //if (!line.trim().equals("")) {
                //data += line + "\r";
                //}
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null) br.close();
                if (is != null) is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }


    /**
     * Deterimine if a string value is a file or not
     * 
     * @param path The file path to test
     * @return True if file, False if not
     */
    private static boolean isFile(String path) {
        File f = new File(path);
        return f != null && f.isFile() && f.exists();
    }
    
    /**
     * Determine if a string value is a resource or not
     * 
     * @param resource The resource location to test
     * @return True if resource, False if not
     */
    private static boolean isResource(String resource) {
        return resource != null && resource.startsWith("/jzebra/resources/");
    }
    

    /**
     * The meat and potatoes that actually generates the printjob that gets sent
     * to the printer.  <p>If you view the source, you'll notice this method
     * is very simple.  Fortunately, RAW printing is pretty simple to code, so 
     * view the source if you'd like to impliment directly.
     * 
     * @param ps The PrintService object
     * @param commands The RAW commands to be sent directly to the printer
     * @return True if print job created successfull
     * @throws javax.print.PrintException
     */
    public static boolean print(PrintService ps, String commands) throws PrintException {
        if (ps == null)
            return false;
        System.out.println("Using printer: " + ps.getName());
        
        // Replace newline chars with carriage returns + strip comments
        commands = stripDown(commands);
        
        // Dump the printer commands  to console
        System.out.println("JZebra Print Command:\n    > " + commands.replaceAll("\\r", "\n    > "));
        
        // Create our print job, document, document type and print that sucker
        ps.createPrintJob().print(new SimpleDoc(commands.getBytes(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
        return true;
    }
    
    /**
     * Convenience method for print(PrintService, String)
     * Loads the default eltron config if none is specified
     * 
     * @return
     */
    public static boolean print() {
        if (ps == null)
           ps = getBestPrinter("eltron");
        if (ps == null)
            ps = getBestPrinter("zebra");
        if (commands == null)
            commands = readFile("/jzebra/resources/eltron_p310.cfg");
        if (commands == null || commands.trim().equals(""))
            commands = "A\r";
        
        try {
            return print(ps, commands);
        }
        catch (Exception e) {
            showError(e);
        }
        return false;
    }
    
    /**
     * Strips out bad or unwanted characters from the printer command string.
     * 
     * @param s Printer command string
     * @return Command string that has been stripped
     */
    private static String stripDown(String s) {
        //return s.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "").replace('\n', '\r');
        return s.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "").replaceAll("\\n+", "\r").trim() + "\r";
    }
    
    /**
     * Converts an image of type "ImageIcon" to a string of hexadecimal data,<p>
     * Usefull for printing an image on a card, as most Zebra type printers only
     * allow binary data, which usually can only be written RAW as hex
     * characters.
     * 
     * @return A hex string composed of the image data.
     */
    private String getImageString(boolean printLogo, ImageIcon logo) {
        if (printLogo && logo != null) {
            //String logoString = logo.
            ImageIcon i = new ImageIcon();
            //BufferedImage b = new BufferedImage();
           // b.
        }
        return "";
    }
    
    /**
     * Loads a picture of type "Image" from a resource URL (A file embedded in
     * the jar)
     * 
     * @param src The resource URL
     */
    public static Image loadImage(String src) {
        try {
            return ImageIO.read(JZebra.class.getResource(src));
        }
        catch (IOException e)        {
            return null;
        }
    }
    

    /**
     * Does command replacement for the printer RAW commands in the format
     * "1=COMMAND".  <p>For example, the RAW commands to be sent to the printer
     * may be something like follows: <code>R\r&CDEW 0 0\r</code>.  The line 
     * breaks, or in this case carriage returns would be new lines in the 
     * config file.  If we skip the comments, each command is a new line.<p>
     * The command <code>2=&CDEW 1 1</code> will change the second command.  This
     * is usefull for keeping config files when changing only a few values, such
     * as name or account number.
     *
     * @param commands The RAW printer commands
     * @param replacements The array of replacement commands
     * @return
     */
    public static String replaceCommands(String commands, String replacements[]) {
        if (commands == null || commands.trim().equals(""))
            return commands;
        
        String[] expCommands = stripDown(commands).split("\\r+");
        
        // Split up the 1="&CDER 0 0 ", etc.
        List<String> l = Arrays.asList(replacements); //replacements.split("\\s=\\d?\\\""));
        ArrayList<String> expReplacements = new ArrayList<String>(l);
        
        // Replace with new values
        int count = 0;
        int i;
        int index;
        String newVal;
        for (String s : expReplacements) {
            i = s.indexOf("=");
            if (i < 0)
                continue;
            try {
                index = Integer.parseInt(s.substring(0, i));
                if (index < 0)
                    continue;
            }
            catch (NumberFormatException e) {
                continue;
            }
            
            
            newVal = s.substring(i+1);
            System.out.println("Replacing value " + i + ": \""
                    + expCommands[index] + "\" with " + i + ": \"" + newVal + "\"");
            
            expCommands[index] = newVal;
            count++;
        }
        if (count > 0) 
            return rebuildCommands(expCommands);
        return commands;
    }
    
    /**
     * Rebuilds the command string from array, usefull after manipulating
     * contents, such as using replaceCommands.
     * 
     * @param s
     * @return
     */
    private static String rebuildCommands(String[] s) {
        String retVal = "";
        for (String t : s) {
            retVal += t + "\r";
        }
        return retVal;
    }
    
    /**
     * Gets the best "PrintService" based on the given printer name "o" which
     * should be of type String.<p>  If not, it will do o.toString() and 
     * continue. <p>
     * 
     * Given name does not need to be case sensitive, and can be any part of the
     * printer name.  For example: "eltron" can return "Eltron P310 Card Printer"
     * etc.
     * 
     * @param o
     * @return
     */
    public static PrintService getBestPrinter(Object o) {
       String printerName;
       if (o instanceof String)
           printerName = (String)o;
       else if (o instanceof PrintService)
           return (PrintService)o;
       else
           printerName = o.toString();
          
        System.out.println("Printer specified: " + printerName);
        
        // If exact match, return it immediately
        PrintService[] s = PrintServiceLookup.lookupPrintServices(null, null);
        int i = Arrays.asList(s).indexOf(printerName);
        if (i > -1)
            return s[i];
        // Else, do a "best match" search
        String sysPrinter;
        String typedPrinter;
        for (PrintService ps : s) {
            sysPrinter = ((PrinterName)ps.getAttribute(PrinterName.class)).getValue().toLowerCase();
            typedPrinter = printerName.toString().trim().toLowerCase();
            if (sysPrinter.indexOf(typedPrinter) > -1) {
                System.out.println("Printer found: " + sysPrinter);
                return ps;
            }
        }
        System.err.println("Printer not found: " + printerName);
        return null;
    }
    
    /**
     * Displays a generic error dialog.
     * 
     * @param msg
     * @return
     */
    public static boolean showError(String msg) {
        System.err.println("[JZEBRA] Error: " + msg);
        if (!isHeadless) {
            try {
                JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (HeadlessException e) {}
        }
        return false; 
    }
    
    /**
     * Displays a generic success dialog.
     * @param msg
     * @return
     */
    public static boolean showSuccess(String msg) {
        System.out.println("[JZEBRA] Success: " + msg);
        if (!isHeadless) {
            try {
                JOptionPane.showMessageDialog(null, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (HeadlessException e) {}
        }
        return true; 
    }
    
    /**
     * Processes command line arguments.<p>
     * 
     * If "--help", "-h", or "/?" was specified, prints help information and
     * exits.<p>
     * 
     * For more information on what arguments are available try the following in
     * a command window:
     * <p><blockquote><pre>
     *  java -jar JZebra.jar --help
     * </pre></blockquote><p>
     * @param args The args passes from the main method.
     * @return Printer name if found in arguments, else null
     */
    public static void processArgs(String[] args) {
        boolean eltronOverride = false;
        String firstName = "FIRST";
        String middleName = "";
        String lastName = "LAST";
        String accountNo = "000000000";
        String companyNo = "000000000";
        
        for (String s : args) {
            if (s == null || s.trim().equals(""))
                continue;
            
            String var = s.substring(s.indexOf("=")+1).trim().toUpperCase();
            
            if (isHelp(s))
                displayHelpAndQuit();
                
            if (s.toLowerCase().startsWith("config="))
                commands = readFile(var);
            
            if (s.toLowerCase().startsWith("printer="))
                ps = getBestPrinter(var);
            
            if (s.toLowerCase().equals("nogui"))
                isHeadless = true;
            
            if (s.toLowerCase().startsWith("firstname=")) {
                eltronOverride = true;
                firstName = var;
            }
            
            if (s.toLowerCase().startsWith("middlename=")) {
                eltronOverride = true;
                middleName = var;
            }
            
            if (s.toLowerCase().startsWith("lastname=")) {
                eltronOverride = true;
                lastName = var;
            }
            
            if (s.toLowerCase().startsWith("accountno=")) {
                eltronOverride = true;
                accountNo = var;
            }
            
             if (s.toLowerCase().startsWith("companyno=")) {
                eltronOverride = true;
                companyNo = var;
             }
            
        }
        
        commands = replaceCommands(commands, args);
        
        // Proprietary - Replace the standard eltron variables
        // (Double-escape for special regex such as dollar sign)
        if (eltronOverride) {
            // Accomodate for no middle name
            if (middleName == null || middleName.equals("")) {
                commands = commands.replaceAll("\\$FIRSTNAME \\$MIDDLENAME", firstName);
            }
            else {
                commands = commands.replaceAll("\\$FIRSTNAME", firstName);
                commands = commands.replaceAll("\\$MIDDLENAME", middleName);
            }
            commands = commands.replaceAll("\\$LASTNAME", lastName);
        }
        
        // Proprietary - Replace the numbers, since card won't encode with non-numeric
        commands = commands.replaceAll("\\$ACCOUNTNO", accountNo);
        commands = commands.replaceAll("\\$COMPANYNO", companyNo);
 
        if (isHeadless) {
           print();
        }
    }

    /*private static boolean isCommandReplacement(String s) {
        int i = s.indexOf("=");
        if (i < 0)
            return false;
        try {
            if (Integer.parseInt(s.substring(0, i)) < 0 )
                return false;
        }
        catch (NumberFormatException e) {}
        return true;
    }*/
    
    
    /**
     * 
     * @param s
     * @return
     */
    private static boolean isHelp(String s) {
        return s.equals("-h") || s.equals("-help") || s.equals("--help") || s.equals("/?");
    }
    
    /**
     * 
     */
    private static void displayHelpAndQuit() {
        System.out.println("\n" + JZebra.readFile("/jzebra/resources/manpage.txt"));
        System.exit(0);
    }
    
    /**
     * Displays a generic error dialog based on the exception's message,
     * also does a PrintStackTrace on the exception.
     * 
     * @param e The exception to display
     * @return False
     */
    public static boolean showError(Exception e) {
        e.printStackTrace();
        return showError(e.getMessage());
    }
    
    public static String getCommands() {
        return commands;
    }

    public static void setCommands(String commands) {
        JZebra.commands = commands.replaceAll("\\r\\n", "\n").replaceAll("\\r+", "\n");
    }

    public static boolean isHeadless() {
        return isHeadless;
    }

    
    /**
     * Tells JZebra to operate in "headless" mode, where no GUI is desired.
     * This is assumed in "nogui" mode.
     * 
     * @param isHeadless 
     */
    public static void setHeadless(boolean isHeadless) {
        JZebra.isHeadless = isHeadless;
    }

    public static PrintService getPrintService() {
        return ps;
    }

    public static void setPrintService(PrintService ps) {
        JZebra.ps = ps;
    }
}
