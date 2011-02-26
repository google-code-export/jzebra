/*
 * JZebraView.java
 * Date: 2008-07-25
 * Author: A. Tres Finocchiaro
 * Liscense: GPL 3.0+
 */
package jzebra;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import jzebra.ConfigLoader.ConfigType;
import jzebra.ConfigLoader.LineFeed;
import jzebra.exception.NullParameterException;

/**
 * The application's main frame.
 */
public class JZebraView extends FrameView implements Paramable {

    private PrintService ps;
    private ConfigLoader cl;
    private PrintRaw pr;
    boolean autoPrint;
    private String errorMessage;
    private String successMessage;
    private String messageTitle;
    private HashMap<String, String> argMap;
    private HashSet<String> varNames;

    /**
     * 
     * @param app
     */
    public JZebraView(SingleFrameApplication app, String[] args) {
        super(app);

        cl = new ConfigLoader();
        pr = new PrintRaw();

        // Initialize our GUI components
        initComponents();
        
        argMap = JZebraTools.mapArgs(args);
        JZebraTools.processParameters(this, pr, cl);
        varNames = JZebraTools.hashVars(cl.getRawCmds());

        pr.setPrintService(ps);
        pr.setRawCmds(JZebraTools.replaceWildCards(argMap, varNames, cl.getRawCmds()));

        // Initialize our custom components
        initCustomComponents();
    }

    @Action
    public void showAboutBox() {
        JZebraApp.getApplication().show(new JZebraAboutBox(this.getFrame()));
    }

    /**
     * 
     */
    private void initCustomComponents() {
        getRootPane().setDefaultButton(printButton);
        getFrame().setIconImage(loadImage("printer.png"));

        printerComboBox.setRenderer(new PrinterListCellRenderer());
        configTypeComboBox.setRenderer(new ConfigTypeCellRenderer());

        PrintServiceMatcher.populateComponent(printerComboBox);

        if (ps != null) {
            printerComboBox.setSelectedItem(ps);
        }
        else if (printerComboBox.getItemCount() == 0) {
            printerComboBox.addItem("No printers detected.");
            printerComboBox.setEnabled(false);
        }

        configTypeComboBox.removeAllItems();
        for (ConfigType c : ConfigType.values()) {
            configTypeComboBox.addItem(c);
        }

        if (cl.getConfigType() != null) {
            configTypeComboBox.setSelectedItem(cl.getConfigType());
        }

        lineFeedComboBox.removeAllItems();
        for (LineFeed l : LineFeed.values()) {
            lineFeedComboBox.addItem(l);
        }

        if (cl.getLineFeed() != null) {
            lineFeedComboBox.setSelectedItem(LineFeed.getLineFeed(cl.getLineFeed()));
        }


        fillInGui();

        new FileDropListener(Logger.getLogger(getClass().getName()), cmdsEditorPane, new FileDropListener.Listener() {
            public void filesDropped(File[] f) {
                refreshConfig(f);
            }
        });
    }

    private void fillInGui() {
        configPathTextField.setText(cl.getConfigPath());
        cmdsEditorPane.setText(JZebraTools.replaceWildCards(argMap, varNames, cl.getCommentedCmds()));
        cmdsEditorPane.setCaretPosition(0);
    }

    public void refreshConfig(URL u) {
        if (cl.setConfigPath(u)) { 
            readConfig(u.getPath());
        }
    }

    public void refreshConfig(File[] f) {
        if (f.length > 0) {
                refreshConfig(f[0]);
        }
    }

    public void refreshConfig(File f) {
        if (cl.setConfigPath(f)) {
            readConfig(f.getPath());
        }
    }

    public void refreshConfig(String s) {
        if (cl.setConfigPath(s)) {
            readConfig(s);
        }
    }

    public void readConfig(String s) {
        try {
            cl.readFile();
            fillInGui();
            //cmdsEditorPane.setText(JZebraTools.replaceWildCards(argMap, varNames, cl.getCommentedCmds()));
            //cmdsEditorPane.setCaretPosition(0);
        } catch (IOException e) {
            JZebraTools.showMessage(this, errorMessage, e);
        } finally {
            cl.closeStreams();
        }
    }

    /**
     * Replaces wildcard variables, like $FIRSTNAME$ with the variable supplied
     * in the web applet: param name="FIRSTNAME" value="Tres"
     * @param configCmds
     * @return
     */
    //private String parseVariables(String configCmds) {
//
    // }

    /*public void showMessage() {
    if (ex != null) {
    String msg = errorMessage + "\n" + ex.getLocalizedMessage();
    JOptionPane.showMessageDialog(this.getComponent(), msg, messageTitle,
    JOptionPane.ERROR_MESSAGE);
    } else {
    JOptionPane.showMessageDialog(this.getComponent(), successMessage,
    messageTitle, JOptionPane.INFORMATION_MESSAGE);
    }
    }*/
    /**
     * Creates the print service by iterating through printers until finding
     * matching printer containing "printerName" in its description
     * @param printerName
     * @return
     */
    public boolean setPrinterName(String printerName) {
        return ((ps = PrintServiceMatcher.findPrinter(printerName)) != null);
    }

    public boolean setPrinterName(String printerName, String defaultPrinter) {
        return setPrinterName(printerName) ? true : setPrinterName(defaultPrinter);
    }

    public String getParameter(String param) throws PrintException {
        if (argMap != null && param != null) {
            return argMap.get(param.toLowerCase());
        }
        throw new NullParameterException("argMap not defined or null.");
    }

    /**
     * Sets applet to print automatically on load, normally specified by
     * applet parameter in html.  Default is true
     * @param autoPrint
     */
    public boolean setAutoPrint(boolean autoPrint) {
        this.autoPrint = autoPrint;
        return true;
    }

    public boolean setAutoPrint(String autoPrint) {
        if (autoPrint != null) {
            this.autoPrint = autoPrint.equalsIgnoreCase("true");
        }
        return autoPrint != null;
    }

    //public boolean setArgs(String[] args) {
    //
    //}
    public boolean setAutoPrint(String autoPrint, String defaultPrint) {
        if (!setAutoPrint(autoPrint)) {
            return setAutoPrint(defaultPrint);
        }
        return true;
    }

    /**
     * Loads a picture of type "Image" from a resource URL (A file embedded in
     * the jar)
     * 
     * @param src The resource URL
     */
    private Image loadImage(String src) {
        try {
            return ImageIO.read(this.getClass().getResource("/jzebra/resources/" + src));
        } catch (IOException e) {
            return null;
        }
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        printButton = new javax.swing.JButton();
        printerLabel = new javax.swing.JLabel();
        cmdsScrollPane = new javax.swing.JScrollPane();
        cmdsEditorPane = new javax.swing.JEditorPane();
        sizerLabel = new javax.swing.JLabel();
        htmlButton = new javax.swing.JButton();
        consoleButton = new javax.swing.JButton();
        lineFeedComboBox = new javax.swing.JComboBox();
        lineFeedLabel = new javax.swing.JLabel();
        sectionSeparator = new javax.swing.JSeparator();
        configTypeLabel = new javax.swing.JLabel();
        configTypeComboBox = new javax.swing.JComboBox();
        configPathLabel = new javax.swing.JLabel();
        configPathTextField = new javax.swing.JTextField();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jzebra.JZebraApp.class).getContext().getResourceMap(JZebraView.class);
        printButton.setIcon(resourceMap.getIcon("printButton.icon")); // NOI18N
        printButton.setMnemonic('p');
        printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
        printButton.setToolTipText(resourceMap.getString("printButton.toolTipText")); // NOI18N
        printButton.setName("printButton"); // NOI18N
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        printerComboBox.setToolTipText(resourceMap.getString("printerComboBox.toolTipText")); // NOI18N
        printerComboBox.setName("printerComboBox"); // NOI18N
        //printerComboBox.setRenderer(new PrinterListCellRenderer());

        printerLabel.setDisplayedMnemonic('r');
        printerLabel.setLabelFor(printerComboBox);
        printerLabel.setText(resourceMap.getString("printerLabel.text")); // NOI18N
        printerLabel.setName("printerLabel"); // NOI18N

        cmdsScrollPane.setName("cmdsScrollPane"); // NOI18N

        cmdsEditorPane.setFont(resourceMap.getFont("cmdsEditorPane.font")); // NOI18N
        cmdsEditorPane.setText(resourceMap.getString("cmdsEditorPane.text")); // NOI18N
        cmdsEditorPane.setToolTipText(resourceMap.getString("cmdsEditorPane.toolTipText")); // NOI18N
        cmdsEditorPane.setName("cmdsEditorPane"); // NOI18N
        cmdsEditorPane.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                cmdsEditorPaneCaretUpdate(evt);
            }
        });
        cmdsScrollPane.setViewportView(cmdsEditorPane);

        sizerLabel.setIcon(resourceMap.getIcon("sizerLabel.icon")); // NOI18N
        sizerLabel.setText(resourceMap.getString("sizerLabel.text")); // NOI18N
        sizerLabel.setName("sizerLabel"); // NOI18N

        htmlButton.setIcon(resourceMap.getIcon("htmlButton.icon")); // NOI18N
        htmlButton.setMnemonic('h');
        htmlButton.setText(resourceMap.getString("htmlButton.text")); // NOI18N
        htmlButton.setToolTipText(resourceMap.getString("htmlButton.toolTipText")); // NOI18N
        htmlButton.setBorder(null);
        htmlButton.setName("htmlButton"); // NOI18N
        htmlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                htmlButtonActionPerformed(evt);
            }
        });

        consoleButton.setIcon(resourceMap.getIcon("consoleButton.icon")); // NOI18N
        consoleButton.setMnemonic('c');
        consoleButton.setText(resourceMap.getString("consoleButton.text")); // NOI18N
        consoleButton.setToolTipText(resourceMap.getString("consoleButton.toolTipText")); // NOI18N
        consoleButton.setBorder(null);
        consoleButton.setName("consoleButton"); // NOI18N
        consoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consoleButtonActionPerformed(evt);
            }
        });

        lineFeedComboBox.setName("lineFeedComboBox"); // NOI18N
        lineFeedComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineFeedComboBoxActionPerformed(evt);
            }
        });

        lineFeedLabel.setDisplayedMnemonic('F');
        lineFeedLabel.setLabelFor(lineFeedComboBox);
        lineFeedLabel.setText(resourceMap.getString("lineFeedLabel.text")); // NOI18N
        lineFeedLabel.setName("lineFeedLabel"); // NOI18N

        sectionSeparator.setName("sectionSeparator"); // NOI18N

        configTypeLabel.setDisplayedMnemonic('T');
        configTypeLabel.setLabelFor(lineFeedComboBox);
        configTypeLabel.setText(resourceMap.getString("configTypeLabel.text")); // NOI18N
        configTypeLabel.setName("configTypeLabel"); // NOI18N

        configTypeComboBox.setName("configTypeComboBox"); // NOI18N

        configPathLabel.setDisplayedMnemonic('T');
        configPathLabel.setLabelFor(lineFeedComboBox);
        configPathLabel.setText(resourceMap.getString("configPathLabel.text")); // NOI18N
        configPathLabel.setName("configPathLabel"); // NOI18N

        configPathTextField.setText(resourceMap.getString("configPathTextField.text")); // NOI18N
        configPathTextField.setName("configPathTextField"); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, sectionSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(lineFeedLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lineFeedComboBox, 0, 152, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(configPathLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configPathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(consoleButton)
                    .add(htmlButton))
                .addContainerGap())
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cmdsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(printerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(printerComboBox, 0, 365, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(printButton)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sizerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cmdsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lineFeedLabel)
                            .add(lineFeedComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(configTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(configTypeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(configPathLabel)
                            .add(configPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(htmlButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(consoleButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sectionSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(printerLabel)
                    .add(printerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(printButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sizerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        setComponent(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     * @param evt
     */
private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
    cl.setCommentedCmds(cmdsEditorPane.getText());
    ps = (PrintService) printerComboBox.getSelectedItem();
    pr.setPrintService(ps);
    pr.setRawCmds(cl.getRawCmds());

    Exception e = JZebraTools.doPrint(this, pr, cl);
    if (e == null) {
        JZebraTools.showMessage(this, successMessage);
    }
    else {
        JZebraTools.showMessage(this, successMessage, e);
    }
/* THREAD
new Thread(new Runnable() {

public void run() {
try {
String debug = "Raw output:\n";
for (String s : pr.getRawCmds().split(cl.getEscapedLineFeed())) {
debug += "> " + s + "\n";
}
Logger.getLogger(this.getClass().getName()).log(Level.INFO, debug);
pr.print();
} catch (PrintException pe) {
Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, errorMessage, pe);
ex = pe;
}
Logger.getLogger(this.getClass().getName()).log(Level.INFO, successMessage);
SwingUtilities.invokeLater(
new Runnable() {

public void run() {
showMessage();
}
});
}
}).run();
 */

}//GEN-LAST:event_printButtonActionPerformed

private void cmdsEditorPaneCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_cmdsEditorPaneCaretUpdate
}//GEN-LAST:event_cmdsEditorPaneCaretUpdate

private void htmlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_htmlButtonActionPerformed
    String msg = "<html>\n\t<applet code=\"" + JZebraApplet.class.getCanonicalName() + 
            "\" archive=\"jzebra.jar\" width=\"" + printButton.getWidth() +
            "\" height=\"" + printButton.getHeight() + "\">";
    for (ParameterLoader pl : ParameterLoader.values()) {
        msg += "\n\t\t<param name=\"" + pl.toString().toLowerCase() +
                "\" value=\"" + pl.getParameter(this) + "\">";
    }
    for (String s : varNames) {
        msg += "\n\t\t<param name=\"" + s + "\" value=\"some_value\">";
    }
    msg += "\n\t</applet>\n</html>";
    JOptionPane.showMessageDialog(this.getFrame(), new JTextArea(msg),
            messageTitle, JOptionPane.PLAIN_MESSAGE);
}//GEN-LAST:event_htmlButtonActionPerformed

private void consoleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleButtonActionPerformed
    String msg = "java -jar jzebra.jar ";
    for (ParameterLoader pl : ParameterLoader.values()) {
        msg += "\n\t" + pl.toString().toLowerCase() +
                "=\"" + pl.getParameter(this) + "\" ";
    }
    for (String s : varNames) {
        msg += "\n\t" + s + "=\"some_value\" ";
    }
    //msg += "\n\t</applet>\n</html>";
    JOptionPane.showMessageDialog(this.getFrame(), new JTextArea(msg),
            messageTitle, JOptionPane.PLAIN_MESSAGE);
}//GEN-LAST:event_consoleButtonActionPerformed

private void lineFeedComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineFeedComboBoxActionPerformed
    cl.setLineFeed(lineFeedComboBox.getSelectedItem());
}//GEN-LAST:event_lineFeedComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane cmdsEditorPane;
    private javax.swing.JScrollPane cmdsScrollPane;
    private javax.swing.JLabel configPathLabel;
    private javax.swing.JTextField configPathTextField;
    private javax.swing.JComboBox configTypeComboBox;
    private javax.swing.JLabel configTypeLabel;
    private javax.swing.JButton consoleButton;
    private javax.swing.JButton htmlButton;
    private javax.swing.JComboBox lineFeedComboBox;
    private javax.swing.JLabel lineFeedLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton printButton;
    private final javax.swing.JComboBox printerComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel printerLabel;
    private javax.swing.JSeparator sectionSeparator;
    private javax.swing.JLabel sizerLabel;
    // End of variables declaration//GEN-END:variables

    public ConfigLoader getConfigLoader() {
        return cl;
    }

    public PrintService getPrintService() {
        return ps;
    }

    public PrintRaw getPrintRaw() {
        return pr;
    }

    public boolean setMessageTitle(String paramVal) {
        this.messageTitle = paramVal;
        return messageTitle != null;
    }

    public boolean setMessageTitle(String messageTitle, String defaultTitle) {
        return setMessageTitle(messageTitle) ? true : setMessageTitle(defaultTitle);
    }

    public boolean setButtonText(String paramVal) {
        printButton.setText(paramVal);
        return paramVal != null;
    }

    public boolean setButtonText(String paramVal, String defaultButtonText) {
        return setButtonText(paramVal) ? true : setButtonText(defaultButtonText);
    }

    /**
     * Sets the message displayed when a print error has occured.  This can be
     * an empty config, unknown printer name, etc.  Default is "jZebra encountered
     * an error...".
     * <p>This message supports &lt;html&gt; tags.  See included 
     * <strong>preview-application.html</strong> for more details.</p>
     * @param successMessage
     */
    public boolean setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return errorMessage != null;
    }

    public boolean setErrorMessage(String errorMessage, String defaultMessage) {
        return setErrorMessage(errorMessage) ? true : setErrorMessage(defaultMessage);
    }

    public boolean setSuccessMessage(String paramVal) {
        this.successMessage = paramVal;
        return this.successMessage != null;
    }

    public boolean setSuccessMessage(String paramVal, String defaultSuccessMessage) {
        return setSuccessMessage(paramVal) ? true : setSuccessMessage(defaultSuccessMessage);
    }

    public boolean isAutoPrint() {
        return autoPrint;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public String getPrinterName() {
        if (ps != null) {
            return ps.getName();
        }
        return null;
    }

    public String getButtonText() {
        return printButton.getText();
    }
}
