package jzebra;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.print.PrintService;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class PrintHTML extends JLabel implements Printable {

    private final AtomicReference<PrintService> ps = new AtomicReference<PrintService>(null);
    private final AtomicReference<String> jobName = new AtomicReference<String>("jZebra 2D Printing");
    private final AtomicInteger orientation = new AtomicInteger(PageFormat.PORTRAIT);
    //private final AtomicReference<Paper> paper = new AtomicReference<Paper>(null);
    //private JLabel label;
    
    public PrintHTML() {
        super();
        //add(label = new JLabel());
        super.setOpaque(true);
        super.setBackground(Color.WHITE);
        //label.setBackground(Color.WHITE);
    }

    public void append(String html) {
        super.setText(super.getText() == null ? html : super.getText() + html);
    }

    public void clear() {
        super.setText(null);
    }
    
    public String get() {
        return super.getText();
    }
    
    public void print() throws PrinterException {
        JFrame j = new JFrame(jobName.get());
        j.setUndecorated(true);
        j.setLayout(new FlowLayout());
        j.add(this);
        this.setBorder(null);
        
        j.pack();
        j.setExtendedState(j.ICONIFIED);
        j.setVisible(true);
        
        // Elimate any margins
        HashPrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();             
        attr.add(new MediaPrintableArea(0f, 0f, getWidth()/72f, getHeight()/72f, MediaPrintableArea.INCH));               
        
        PrinterJob job = PrinterJob.getPrinterJob();    
        job.setPrintService(ps.get());
        job.setPrintable(this);
        job.setJobName(jobName.get());
        job.print(attr);
        j.setVisible(false);
        j.dispose();
        clear();
    }
    
    public void setPrintParameters(PrintApplet a) {
        this.ps.set(a.getPrintService());
        this.jobName.set(a.getJobName().replace(" Raw ", " HTML "));
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (g == null) {
            throw new PrinterException("No graphics specified");
        }
        if (pf == null) {
            throw new PrinterException("No page format specified");
        }
        if (pageIndex > 0) {
            return (NO_SUCH_PAGE);
        }
        
        boolean doubleBuffered = super.isDoubleBuffered();
        super.setDoubleBuffered(false);
        
        pf.setOrientation(orientation.get());
        
        //Paper paper = new Paper();
        //paper.setSize(8.5 * 72, 11 * 72);
        //paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        //pf.setPaper(paper);
        //Paper paper = pf.getPaper();
        //paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        //pf.getPaper().setImageableArea(0, 0, paper.getWidth() + 200, paper.getHeight() + 200);
        
        //pf.getPaper().setImageableArea(-100, -100, 200, 200);
        
        
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        //g2d.translate(paper.getImageableX(), paper.getImageableY());
        this.paint(g2d);
        super.setDoubleBuffered(doubleBuffered);
        return (PAGE_EXISTS);
    }
}
