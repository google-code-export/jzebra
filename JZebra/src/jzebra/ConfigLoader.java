package jzebra;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import jzebra.exception.NullConfigPathException;
import jzebra.exception.UndefinedConfigTypeException;

/**
 * Loads the config file, or "RAW" file containing all of the print commands.
 * <p>Usage:
 *       ConfigLoader cl = new ConfigLoader(ConfigLoader.ConfigType.FILE, 
 *          "C:\\zebra.cfg");
 *       //ConfigLoader cl = new ConfigLoader(ConfigLoader.ConfigType.URL,
 *          "http://mysite.com/zebra.cfg");
 *       //ConfigLoader cl = new ConfigLoader(ConfigLoader.ConfigType.RESOURCE,
 *          "/jzebra/resources/zebra.cfg");
 *
 *       try {
 *           cl.readFile();
 *       } catch (IOException ioe) {
 *           ioe.printStackTrace(System.err);
 *       } finally {
 *           cl.closeStreams();
 *       }
 *
 *       System.out.println("RAW: \n" + cl.getRawCmds());
 *       System.out.println("COMMENTED: \n" + cl.getCommentedCmds());</p>
 * @author tfino
 */
public class ConfigLoader {

    public enum ConfigType {
        URL, FILE, RESOURCE;
        public static ConfigType getType(String namedType) {
            if (namedType == null || namedType.equals("")) {
                return null;
            }
            for (ConfigType c : values()) {
                if (namedType.equalsIgnoreCase(c.toString())) {
                    return c;
                }
            }
            return null;
        }

    }

    public enum LineFeed {
        N ("New Line"),
        R ("Carraige Return"),
        RN ("Carraige Return + New Line"),
        T ("Tab Character"),
        F ("Form Feed");
        private String title;
        LineFeed(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return getEscapedLineFeed() +  " [" + title + "]";
        }

        public String getLineFeed() {
            switch (this) {
                case N: return "\n";
                case R: return "\r";
                case T: return "\t";
                case F: return "\f";
                case RN: return "\r\n";
                default:  return "\n";
            }
        }

        public String getEscapedLineFeed() {
            return "\\" + super.toString().toLowerCase();
        }

        public String getRegexLineFeed() {
            return "\\" + getEscapedLineFeed();
        }

        public static LineFeed getLineFeed(String name) {
            for (LineFeed l : values()) {
                if (l.getLineFeed().equals(name) || l.getEscapedLineFeed().equals(name)) {
                    return l;
                }
            }
            return LineFeed.N;
        }

    }

    private String configPath;
    private ConfigType configType;
    
    private String commentedCmds;

    private String rawCmds;
    //private String lineFeed = "\r\n";
    private String lineFeed = LineFeed.N.getLineFeed();
    private InputStream in;
    private BufferedReader br;

    /**
     * Explicit constructor, when "configtype" and "configPath" are known.
     * @param configType
     * @param configPath
     */
    public ConfigLoader(ConfigType configType, String configPath) {
        this.configType = configType;
        this.configPath = configPath;
    }

    /**
     * Default constructor when "configPath" and "configType" are unknown.
     * <p>Attemps to use this constructor to print before calling setConfigType()
     * and setConfigPath() will result in a PrintException</p>
     */
    public ConfigLoader() {
        this.configPath = null;
        this.configType = null;
    }

    /**
     * Uses ConfigType enum "configType" to switch between file
     * modes, and then read the file with it's appropriate method.
     *<p>Make sure to call a finally { closeStreams(); } in your try/catch</p>
     *
     * @throws java.io.IOException
     */
    public void readFile() throws IOException {
        if (configPath == null || configPath.trim().equals("")) {
            throw new NullConfigPathException("Config is null or empty.");
        }
        switch (configType) {
            case URL:
                URLConnection urlConn = new URL(configPath).openConnection();
                urlConn.setDoInput(true);
                urlConn.setUseCaches(false);

                in = new DataInputStream(urlConn.getInputStream());
                br = new BufferedReader(new InputStreamReader(in));
                break;
            case FILE:
                br = new BufferedReader(new FileReader(configPath));
                break;
            case RESOURCE:
                in = this.getClass().getResourceAsStream(configPath);
                br = new BufferedReader(new InputStreamReader(in)); 
                break;
            default:
                throw new UndefinedConfigTypeException();
        }
        readBuffer();
    }

    /**
     * Read the config to memory and save a version that has the comments and
     * extra newlines stripped out
     * 
     * @throws java.io.IOException
     */
    private boolean readBuffer() throws IOException {
        String buffer;
        String commTemp = null;
        while (null != (buffer = br.readLine())) {
            if (commTemp == null) {
                commTemp = "";
            }
            commTemp += buffer.trim() + "\n";
        }
        return setCommentedCmds(commTemp);
    }

    /**
     * Sets the commented commands to the string specified, then strips the comments
     * and sets the raw commands
     */
    public boolean setCommentedCmds(String commentedCmds) {
        this.commentedCmds = commentedCmds;
        if (commentedCmds != null) {
            String rawTemp = commentedCmds.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");
            rawTemp = rawTemp.replaceAll("([\\r\\n\\f])+", lineFeed);
            if (rawTemp.startsWith(lineFeed)) {
                rawTemp = rawTemp.replaceFirst(getEscapedLineFeed(),"");
            }
            rawCmds = rawTemp;
            return true;
        }
        return false;
    }

    public void closeStreams() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }

        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
        }
    }

    public String getConfigPath() {
        return configPath;
    }

    public ConfigType getConfigType() {
        return configType;
    }

    /**
     * Set the type of location the config file is coming from as a URL, FILE,
     * RESOURCE of type ConfigLoader.ConfigType. <p>For example:  Web Applets
     * default to "ConfigType.URL", Desktop Applications default to
     * "ConfigType.FILE", and if none is specified, we default to an embedded
     * "ConfigType.RESOURCE" file.</p>
     * @param configType
     */
    public boolean setConfigType(ConfigType configType) {
        this.configType = configType;
        return configType != null;
    }

    /**
     * Sets the configType to the String entered assuming it matches the name, such
     * as URL, FILE, RESOURCE.   Does not need to match case.  Returns the 
     * defaultType if the namedType could be matched.
     * @param namedType
     * @param defaultType
     */
    public boolean setConfigType(String namedType, ConfigType defaultType) {
        ConfigType c = ConfigType.getType(namedType);
        return setConfigType(c == null ? defaultType : c);
    }
    
    public boolean setConfigType(String namedType, String namedDefault) {
        ConfigType c = ConfigType.getType(namedDefault);
        return setConfigType(namedType, c);
    }

    /**
     * Sets the path to the config file, which can be a URL, FILE, or RESOURCE
     * @param configPath
     */
    public boolean setConfigPath(String configPath) {
        this.configPath = configPath;
        return configPath != null;
    }

    public boolean setConfigPath(File f) {
        this.configType = ConfigType.FILE;
        return setConfigPath(f.getPath());
    }
    
    public boolean setConfigPath(URL u) {
        this.configType = ConfigType.URL;
        return setConfigPath(u.getPath());
    }

    /**
     * Sets the path to the config file, unless empty, then sets it to the default
     * Its important to call this AFTER setConfigType, as the default path relies
     * on the default type.
     * @param configPath
     * @param defaultPath
     */
    public boolean setConfigPath(String configPath, String defaultPath) {
        boolean useDefault = isBlank(configPath);
        boolean success = setConfigPath(useDefault ? defaultPath : configPath);
        if (useDefault) {
            setConfigType(configType.RESOURCE);
        }
        return success;
    }
    
    /**
     * Returns true if given String is empty or null
     * @param s
     * @return
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().equals("");
    }
    
    /**
     * Returns the linefeed string
     * @return
     */
    public String getLineFeed() {
        return lineFeed;
    }

    /**
     * Sets the linefeed string to your choice (/r/n, etc) as linefeed characters
     * are stripped when reading the config file.
     * @param lineFeed
     */
    public boolean setLineFeed(String lineFeed) {
        this.lineFeed = lineFeed;
        return this.lineFeed != null;
    }

    public boolean setLineFeed(Object o) {
        if (o instanceof LineFeed) {
            return setLineFeed(((LineFeed)o).getEscapedLineFeed());
        }
        return false;
    }
   
    /**
     * Gets the string representing a linefeed and returns it in human-readable
     * format.
     *
     * @param s
     * @return
     */
    private String escapeString(String s) {
        return s.replaceAll("\\\\", "\\\\\\\\");
    }

    public String getCommentedCmds() {
        return commentedCmds;
    }
    
    public String getRawCmds() {
        return rawCmds;
    }
    
    public String getEscapedCommentedCmds() {
        return escapeString(commentedCmds);
    }
    
    public boolean setEscapedLineFeed(String lineFeed) {
        this.lineFeed = fixEscapedCharacters(lineFeed);
        return this.lineFeed != null;
    }

    public boolean setEscapedLineFeed(String lineFeed, String defaultLineFeed) {
        return isBlank(lineFeed) ? setLineFeed(defaultLineFeed) : setEscapedLineFeed(lineFeed);
    }

    /**
     * Replaced double escapes (such as reading "\r" from a config file which
     * translates to "\\r") for setting the lineFeed.  Add special characters as
     * needed.
     * @param s
     * @return
     */
    /*public String fixEscapedCharacters(String s) {
        String[] normal = {"t","n","r","f"};//,"\\\"","\\\\"};
        String[] special = {"\t","\n","\r","\f"};//,"\"","\\"};
        String retVal = new String(s);
        for (int i = 0; i < normal.length; i++) {
            retVal = retVal.replaceAll("\\\\" + normal[i], special[i]);
        }
        return retVal;
    }*/


    /**
     * Replaced double escapes (such as reading "\r" from a config file which
     * translates to "\\r") for setting the lineFeed.  Add special characters as
     * needed.
     * @param s
     * @return
     */
    public String fixEscapedCharacters(String s) {
        String retVal = new String(s);
        for (LineFeed l : LineFeed.values()) {
            retVal.replaceAll(l.getRegexLineFeed(), l.getLineFeed());
        }
        return retVal;
    }
    
    public String getEscapedRawCmds() {
        return escapeString(rawCmds);
    }
    
    public String getEscapedLineFeed() {
        return escapeString(lineFeed);
    }
}
