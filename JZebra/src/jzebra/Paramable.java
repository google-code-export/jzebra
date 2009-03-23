/*
 * Requirement for anything that uses the ParameterLoader to dynamically load
 * applet or command line printing parameters to the PrintRaw class.
 * Optionally, you can construct your own PrintRaw, and read your own config file.
 */
package jzebra;

import java.awt.Component;
import javax.print.PrintService;

/**
 *
 * @author tfino
 */
public interface Paramable {
    
    public boolean setAutoPrint(boolean autoPrint);

    public boolean setAutoPrint(String paramVal);

    public boolean setAutoPrint(String paramVal, String defaultAutoPrint);

    public boolean setPrinterName(String paramVal);

    public boolean setPrinterName(String paramVal, String defaultPrinterName);

    public boolean setMessageTitle(String paramVal);

    public boolean setMessageTitle(String paramVal, String defaultPrinterName);

    public boolean setButtonText(String paramVal);

    public boolean setButtonText(String paramVal, String defaultButtonText);

    public boolean setErrorMessage(String paramVal);

    public boolean setErrorMessage(String paramVal, String defaultErrorMessage);

    public boolean setSuccessMessage(String paramVal);

    public boolean setSuccessMessage(String paramVal, String defaultSuccessMessage);
    
    public boolean isAutoPrint();
    
    public String getButtonText();

    public String getErrorMessage();

    public String getSuccessMessage();

    public String getMessageTitle();

    public String getPrinterName();

    public String getParameter(String paramName) throws javax.print.PrintException;

    public ConfigLoader getConfigLoader();

    public PrintService getPrintService();

    public PrintRaw getPrintRaw();

    public Component getComponent();
}
