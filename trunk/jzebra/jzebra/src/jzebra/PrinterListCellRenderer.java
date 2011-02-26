/*
 * Liscence GPL 3.0+
 */
package jzebra;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;



/**
 * A simple ListCellRenderer that draws a printer icon next to the cell/label.
 * 
 * @author A. Tres Finocchiaro
 */
public class PrinterListCellRenderer extends JLabel implements ListCellRenderer {
    private java.awt.Image i;
    private String printerIcon = "/jzebra/resources/printer.png";
    
    /**
     * Default constructor
     */
    public PrinterListCellRenderer() {
        setOpaque(true);
        i = loadImage(printerIcon);
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
        setIcon(new javax.swing.ImageIcon(i));
        setText(value.toString());
        return this;
    }
    
    /**
     * Loads a picture of type "Image" from a resource URL (A file embedded in
     * the jar)
     * 
     * @param src The resource URL
     */
    private java.awt.Image loadImage(String src) {
        try {
            return javax.imageio.ImageIO.read(this.getClass().getResource(src));
        }
        catch (java.io.IOException e) {
            return null;
        }
    }
}
