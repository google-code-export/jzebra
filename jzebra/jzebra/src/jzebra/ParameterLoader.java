/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jzebra;

/**
 * Takes command line or web param values matching the enumerated name and tries
 * to set the value within the applet, desktop application or console
 * application, respectively.
 * @author tfino
 */
public enum ParameterLoader {
    CONFIGTYPE(EnumType.GLOBAL),
    CONFIGPATH(EnumType.GLOBAL),
    LINEFEED(EnumType.GLOBAL),
    AUTOPRINT(EnumType.GLOBAL),
    PRINTERNAME(EnumType.GLOBAL),
    ERRORMESSAGE(EnumType.GLOBAL),
    SUCCESSMESSAGE(EnumType.GLOBAL),
    MESSAGETITLE(EnumType.GLOBAL),
    BUTTONTEXT(EnumType.GLOBAL),
    
    SHOWMESSAGES(EnumType.APPLET),
    SHOWICON(EnumType.APPLET);

    public enum EnumType {

        GLOBAL,
        APPLET,
        DESKTOP,
        CONSOLE
    }

    ParameterLoader(EnumType enumType) {
        this.enumType = enumType;
    }
    private EnumType enumType;
    // GLOBAL DEFAULTS
    private String defaultConfType = ConfigLoader.ConfigType.URL.toString();
    private String defaultConfPath = "/jzebra/resources/commands.txt";
    private String defaultLineFeed = "\r";
    private String defaultAutoPrint = "true";
    // APPLET DEFAULTS
    private String defaultShowIcon = "true";
    private String defaultShowMessage = "false";
    private String defaultSuccessMessage = "jZebra successfully sent your information to the printer.";
    private String defaultErrorMessage = "jZebra encountered an error while printing.";
    private String defaultMessageTitle = "jZebra";
    private String defaultButtonText = "Print";
    private String defaultPrinterName = "zebra";

    /**
     * Gets a parameter from applet or command line and sets it withing the
     * paramable object by setting its ConfigLoader property, or setting its
     * internal value.
     * @param j
     * @return
     * @throws javax.print.PrintException
     */
    public boolean setParameter(Paramable j) throws javax.print.PrintException {
        String paramVal = j.getParameter(this.toString());
        ConfigLoader cl = j.getConfigLoader();
        
        /**
         * Global parameters
         */
  
        switch (this) {
            case CONFIGTYPE:
                if (j instanceof JZebraView) {
                     setDefaultConfType(ConfigLoader.ConfigType.FILE);
                }
                return cl.setConfigType(paramVal, defaultConfType);
            case CONFIGPATH:
                return cl.setConfigPath(paramVal, defaultConfPath);
            case LINEFEED:
                return cl.setEscapedLineFeed(paramVal, defaultLineFeed);
            case AUTOPRINT:
                return j.setAutoPrint(paramVal, defaultAutoPrint);
            case PRINTERNAME:
                return j.setPrinterName(paramVal, defaultPrinterName);
            case BUTTONTEXT:
                return j.setButtonText(paramVal, defaultButtonText);
            case ERRORMESSAGE:
                return j.setErrorMessage(paramVal, defaultErrorMessage);
            case SUCCESSMESSAGE:
                return j.setSuccessMessage(paramVal, defaultSuccessMessage);
            case MESSAGETITLE:
                return j.setMessageTitle(paramVal, defaultMessageTitle);
        }
        /**
         * JZebraApplet parameters
         */
        if (j instanceof JZebraApplet) {
            switch (this) {
                // APPLET ONLY PARAMS
                case SHOWMESSAGES:
                    return ((JZebraApplet)j).setMessagesShown(paramVal, defaultShowMessage);
                case SHOWICON:
                    return ((JZebraApplet)j).setIconShown(paramVal, defaultShowIcon);
            }

        }
        if (j instanceof JZebraView) {
            // NOT IMPLIMENTED
        }
        else if (j instanceof JZebraApp) {
            // NOT IMPLIMENTED
        }

        return false;
        //throw new jzebra.exception.ParameterMismatchException("Error setting param: " +
        //            this.toString() + " for: " + j.getClass().getCanonicalName() +
        //            " Reason:  Invalid parameter name for given type.");

    }
    
    public String getParameter(Paramable j) {
        ConfigLoader cl = j.getConfigLoader();
        switch (this) {
                case CONFIGTYPE:
                    return cl.getConfigType().toString();
                case CONFIGPATH:
                    return cl.getConfigPath();
                case LINEFEED:
                    return cl.getEscapedLineFeed();
                case AUTOPRINT:
                    return "" + j.isAutoPrint();
                case PRINTERNAME:
                    return j.getPrinterName();
                case BUTTONTEXT:
                    return j.getButtonText();
                case ERRORMESSAGE:
                    return j.getErrorMessage();
                case SUCCESSMESSAGE:
                    return j.getSuccessMessage();
                case MESSAGETITLE:
                    return j.getMessageTitle();
        }
        
        if (j instanceof JZebraApplet) {
            switch (this) {
                // APPLET ONLY PARAMS
                case SHOWMESSAGES:
                    return "" + ((JZebraApplet)j).isMessagesShown();
                case SHOWICON:
                    return "" + ((JZebraApplet)j).isIconShown();
            }

        }
        
        return "some_value";
    }

    public EnumType getEnumType() {
        return enumType;
    }

    public String getDefaultConfigType() {
        return defaultConfType;
    }

    public void setDefaultConfType(String defaultConfType) {
        this.defaultConfType = defaultConfType;
    }
    
    public void setDefaultConfType(ConfigLoader.ConfigType defaultConfType) {
        this.defaultConfType = defaultConfType.toString();
    }

    public String getDefaultAutoPrint() {
        return defaultAutoPrint;
    }

    public void setDefaultAutoPrint(String defaultAutoPrint) {
        this.defaultAutoPrint = defaultAutoPrint;
    }

    public String getDefaultButtonText() {
        return defaultButtonText;
    }

    public void setDefaultButtonText(String defaultButtonText) {
        this.defaultButtonText = defaultButtonText;
    }

    public String getDefaultConfPath() {
        return defaultConfPath;
    }

    public void setDefaultConfPath(String defaultConfPath) {
        this.defaultConfPath = defaultConfPath;
    }

    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }

    public void setDefaultErrorMessage(String defaultErrorMessage) {
        this.defaultErrorMessage = defaultErrorMessage;
    }

    public String getDefaultLineFeed() {
        return defaultLineFeed;
    }

    public void setDefaultLineFeed(String defaultLineFeed) {
        this.defaultLineFeed = defaultLineFeed;
    }

    public String getDefaultMessageTitle() {
        return defaultMessageTitle;
    }

    public void setDefaultMessageTitle(String defaultMessageTitle) {
        this.defaultMessageTitle = defaultMessageTitle;
    }

    public String getDefaultPrinterName() {
        return defaultPrinterName;
    }

    public void setDefaultPrinterName(String defaultPrinterName) {
        this.defaultPrinterName = defaultPrinterName;
    }

    public String getDefaultShowIcon() {
        return defaultShowIcon;
    }

    public void setDefaultShowIcon(String defaultShowIcon) {
        this.defaultShowIcon = defaultShowIcon;
    }

    public String getDefaultShowMessage() {
        return defaultShowMessage;
    }

    public void setDefaultShowMessage(String defaultShowMessage) {
        this.defaultShowMessage = defaultShowMessage;
    }

    public String getDefaultSuccessMessage() {
        return defaultSuccessMessage;
    }

    public void setDefaultSuccessMessage(String defaultSuccessMessage) {
        this.defaultSuccessMessage = defaultSuccessMessage;
    }
}
