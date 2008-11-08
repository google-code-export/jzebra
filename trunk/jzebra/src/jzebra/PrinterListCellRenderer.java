/*
 * Liscence GPL 3.0+
 */
package jzebra;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A simple ListCellRenderer that draws a printer icon next to the cell/label.
 * 
 * @author A. Tres Finocchiaro
 */
public class PrinterListCellRenderer extends JLabel implements ListCellRenderer {
    private Image i;
    
    /**
     * Default constructor
     */
    public PrinterListCellRenderer() {
        setOpaque(true);
        i = JZebra.loadImage("/jzebra/resources/printer.png");
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
        
        setIcon(new ImageIcon(i));
        setText(value.toString());
        return this;
    }
}
