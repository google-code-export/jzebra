package jzebra;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tfino
 */


//test
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.datatransfer.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.Border;
import javax.swing.*;

public class FileDropListener {
    private transient Border normalBorder;
    private transient DropTargetListener dropListener;
    private static Boolean supportsDnD;
    private static Color defaultBorderColor = new Color(0f, 0f, 1f, 0.25f);

    private String junk;
    public FileDropListener(final Component c, final Listener listener) {
    	this(null,
        c,
        BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor),
        true,
        listener);
    }

    public FileDropListener(final Component c, final boolean recursive, final Listener listener) {
	this(null,
	c,
	BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor),
	recursive,
	listener);
    }

    public FileDropListener(final Logger out, final Component c, final Listener listener) {
	this(out,
	c,
	BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor),
	false,
	listener);
    }

    public FileDropListener(final Logger out, final java.awt.Component c, final boolean recursive, final Listener listener) {
	this(out,
	c,
	BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), // Drag border
	recursive,
	listener);
    }

    public FileDropListener(final Component c, final Border dragBorder, final Listener listener) {
	this(
	null,
	c,
	dragBorder,
	false,
	listener);
    }

    public FileDropListener(final Component c, final Border dragBorder, final boolean recursive, final Listener listener) {   this(
	null,
	c,
	dragBorder,
	recursive,
	listener);
    }

    public FileDropListener(final Logger out, final Component c, final Border dragBorder, final Listener listener) {
	this(
	out,
	c,
	dragBorder,
	false,
	listener);
    }
    public FileDropListener(final Logger out, final Component c, final Border dragBorder, final boolean recursive, final Listener listener) {
        if(supportsDnD()) {
            dropListener = new DropTargetListener() {
		    public void dragEnter(DropTargetDragEvent evt) {
			    log(out, "DropListener: dragEnter event.");
			    // Is this an acceptable drag event?
			    if(isDragOk(out, evt)) {
				    // If it's a Swing component, set its border
				    if(c instanceof javax.swing.JComponent) {
					    JComponent jc = (JComponent) c;
					    normalBorder = jc.getBorder();
					    //log(out, "DropListener: normal border saved.");
					    jc.setBorder(dragBorder);
					    //log(out, "DropListener: drag border set.");
				    }
				    // Acknowledge that it's okay to enter
				    //evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE);
				    evt.acceptDrag(DnDConstants.ACTION_COPY);
				    log(out, "DropListener: event accepted.");
			    }
			    else {   // Reject the drag event
				evt.rejectDrag();
				log(out, "DropListener: event rejected.");
			    }
		    }

		    public void dragOver(DropTargetDragEvent evt) {
			    // This is called continually as long as the mouse is
			    // over the drag target.
		    }

		    public void drop(DropTargetDropEvent evt) {
			    log(out, "DropListener: drop event.");
			    try {   // Get whatever was dropped
				    Transferable tr = evt.getTransferable();
				    // Is it a file list?
				    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					    // Say we'll take it.
					    //evt.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
					    evt.acceptDrop (DnDConstants.ACTION_COPY);
					    log(out, "DropListener: file list accepted.");
					    final File[] files = (File[])((List)tr.getTransferData(DataFlavor.javaFileListFlavor)).toArray();
					    if(listener != null) listener.filesDropped(files);
					    // Mark that drop is completed.
					    evt.getDropTargetContext().dropComplete(true);
					    log(out, "DropListener: drop complete.");
				    }
				    else {
					    log(out, "DropListener: not a file list - abort.");
					    evt.rejectDrop();
				    }
			    }
			    catch (IOException io) {
				    log(out, "DropListener: IOException - abort:");
				    io.printStackTrace();
				    evt.rejectDrop();
			    }
			    catch (UnsupportedFlavorException ufe) {
				    log(out, "DropListener: UnsupportedFlavorException - abort:");
				    ufe.printStackTrace();
				    evt.rejectDrop();
			    }
			    finally {
				    // If it's a Swing component, reset its border
				    if(c instanceof JComponent) {
					    JComponent jc = (JComponent)c;
					    jc.setBorder(normalBorder);
					    //log(out, "DropListener: normal border restored.");
				    }
			    }
		    }

		    public void dragExit(DropTargetEvent evt) {
			    log(out, "DropListener: dragExit event.");
			    // If it's a Swing component, reset its border
			    if(c instanceof javax.swing.JComponent) {
				    JComponent jc = (javax.swing.JComponent)c;
				    jc.setBorder(normalBorder);
				    //log(out, "DropListener: normal border restored.");
			    }
		    }

		    public void dropActionChanged(DropTargetDragEvent evt) {
			    log(out, "DropListener: dropActionChanged event.");
			    // Is this an acceptable drag event?
			    if(isDragOk(out, evt)) {
				    //evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE);
				    evt.acceptDrag(DnDConstants.ACTION_COPY);
				    log(out, "DropListener: event accepted.");
			    }
			    else {
				    evt.rejectDrag();
				    log(out, "DropListener: event rejected.");
			    }
		    }
	    };
            // Make the component (and possibly children) drop targets
            makeDropTarget(out, c, recursive);
        }
        else {
		log(out, "DropListener: Drag and drop is not supported with this JVM");
        }
    }

    private static boolean supportsDnD() {
        if(supportsDnD == null) {
            boolean support = false;
            try {
		Class arbitraryDndClass = Class.forName("java.awt.dnd.DnDConstants");
                support = true;
            }
            catch(Exception e) {
		support = false;
            }
            supportsDnD = new Boolean(support);
        }
        return supportsDnD.booleanValue();
    }

    private void makeDropTarget(final Logger out, final Component c, boolean recursive) {
        // Make drop target
        final DropTarget dt = new DropTarget();
        try {
		dt.addDropTargetListener(dropListener);
        }
        catch(TooManyListenersException e) {
		e.printStackTrace();
		log(out, "DropListener: Drop will not work due to previous error. Do you have another listener attached?");
        }
        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent evt) {
				//log(out, "DropListener: Hierarchy changed.");
				Component parent = c.getParent();
				if(parent == null){
					c.setDropTarget(null);
					//log(out, "DropListener: Drop target cleared from component.");
				}
				else {
					new DropTarget(c, dropListener);
					//log(out, "DropListener: Drop target added to component.");
				}
			}
	});
        if(c.getParent() != null) new java.awt.dnd.DropTarget(c, dropListener);
        if(recursive && (c instanceof Container)) {
            // Get the container
            Container cont = (Container) c;
            // Get it's components
            Component[] comps = cont.getComponents();
            // Set it's components as listeners also
            for(int i = 0; i < comps.length; i++)
                makeDropTarget(out, comps[i], recursive);
        }
    }

    /** Determine if the dragged data is a file list. */
    private boolean isDragOk(final Logger out, final DropTargetDragEvent evt) {
	    boolean ok = false;
	    /*// Get data flavors being dragged
	    DataFlavor[] flavors = evt.getCurrentDataFlavors();
            // See if any of the flavors are a file list
	    int i = 0;
	    while(!ok && i < flavors.length) {
		    // Is the flavor a file list?
		    if(flavors[i].equals(DataFlavor.javaFileListFlavor)) ok = true;
		    i++;
	    }
	    // If logging is enabled, show data flavors
	    if(out != null) {
		    if(flavors.length == 0) log(out, "FileDropListener: no data flavors.");
		    for(i = 0; i < flavors.length; i++) log(out, flavors[i].toString());
	    }*/
        return true;
    }

    /** Outputs message to out if it's not null. */
    private static void log(Logger out, String message) {
	    // Log message if requested
	    if(out != null) out.log(Level.INFO, message);
    }

    public static boolean remove(Component c) {
	    return remove(null, c, true);
    }

    public static boolean remove(Logger out, Component c, boolean recursive) {
	    // Make sure we support dnd.
	    if(supportsDnD()) {
		    log(out, "DropListener: Removing drag-and-drop hooks.");
		    c.setDropTarget(null);
		    if(recursive && (c instanceof Container)) {
			    Component[] comps = ((Container)c).getComponents();
			    for(int i = 0; i < comps.length; i++) remove(out, comps[i], recursive);
			    return true;
            }
        }
	return false;
    }

    /** Runs a sample program that shows dropped files */
    public static void main(String[] args) {
        JFrame frame = new JFrame("DropListener");
        //javax.swing.border.TitledBorder dragBorder = new javax.swing.border.TitledBorder("Drop 'em");
        final JTextArea text = new JTextArea();
        frame.getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);
        new FileDropListener(Logger.getLogger(FileDropListener.class.getName()), text, /*dragBorder,*/ new FileDropListener.Listener() {
			public void filesDropped(File[] files) {
				for(int i = 0; i < files.length; i++) {
					try {
						text.append(files[i].getCanonicalPath() + "\n");
					}
					catch(IOException e) {}
				}
			}
        });
        frame.setBounds(100, 100, 300, 400);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
	frame.setVisible(true);
    }

    public interface Listener {
	    public abstract void filesDropped(File[] files);
    }
}
