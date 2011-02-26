/*
 * Liscence GPL 3.0+
 */
package jzebra;

import java.awt.Component;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;



/**
 * A simple ListCellRenderer that draws a printer icon next to the cell/label.
 * 
 * @author A. Tres Finocchiaro
 */
public class ConfigTypeCellRenderer extends JLabel implements ListCellRenderer {
    private javax.swing.ImageIcon u;
    private javax.swing.ImageIcon f;
    private javax.swing.ImageIcon r;
    
    private String uPath = "/jzebra/resources/url.png";
    private String fPath = "/jzebra/resources/file.png";
    private String rPath = "/jzebra/resources/resource.png";

    /**
     * Default constructor
     */
    public ConfigTypeCellRenderer() {
        setOpaque(true);
        u = loadImage(uPath);
        f = loadImage(fPath);
        r = loadImage(rPath);
    }
    
    /**
     * 
     * The overridden method that does the component rendering changes.
     * 
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        if (value instanceof ConfigLoader.ConfigType) {
           ConfigLoader.ConfigType c = (ConfigLoader.ConfigType)value;
           setText(c.toString().toLowerCase());
           switch(c) {
               case FILE: setIcon(f); break;
               case RESOURCE: setIcon(r); break;
               case URL: setIcon(u); break;
               default: setIcon(null); break;
           }
        }
        else {
            setIcon(null);
            setText("ERROR");
        }
        return this;
    }
    
    /**
     * Loads a picture of type "Image" from a resource URL (A file embedded in
     * the jar)
     * 
     * @param src The resource URL
     */
    private ImageIcon loadImage(String src) {
        try {
            return new ImageIcon(ImageIO.read(this.getClass().getResource(src)));
        }
        catch (java.io.IOException e) {
            return null;
        }
    }
}
