package jzebra;

import javax.swing.JOptionPane;

/**
 * Cannot be run interactively.  Display error and exit.
 * @author tfino
 */
public class Main {

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "This program cannot be run interactively.  Please visit: http://code.google.com/p/jzebra", "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}
