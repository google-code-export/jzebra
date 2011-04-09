/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jzebra;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.print.PrintService;

/**
 *
 * @author tfino
 */
public class PrintPostScript implements Printable {

    private final AtomicReference<BufferedImage> bufferedImage = new AtomicReference<BufferedImage>(null);
    //private final AtomicReference<PageFormat> pageFormat = new AtomicReference<PageFormat>();
    //private final AtomicReference<Graphics> graphics = new AtomicReference<Graphics>(null);
    private final AtomicReference<PrintService> ps = new AtomicReference<PrintService>(null);
    private final AtomicReference<String> jobName = new AtomicReference<String>("jZebra 2D Printing");

    private final AtomicInteger orientation = new AtomicInteger(PageFormat.PORTRAIT);

    public PrintPostScript() {
        
    }
    /**
     * Can be called directly
     * @throws PrinterException
     */
    public void print() throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        //PageFormat pf = job.defaultPage();
        //pf.setOrientation(orientation.get());
        job.setPrintService(ps.get());
        job.setPrintable(this);
        job.setJobName(jobName.get());
        job.print();
    }

    /*public int print(Applet applet, BufferedImage bufferedImage) throws PrinterException {
        this.bufferedImage.set(bufferedImage);
        return print(applet.getGraphics(), this.pageFormat.get(), 0);
    }*/

    /**
     * Should not be called directly, see print() instead
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     * @throws PrinterException
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (this.bufferedImage.get() == null) {
            throw new PrinterException("No image data specified");
        }

        if (graphics == null) {
            throw new PrinterException("No graphics specified");
        }
        
        if (pageFormat == null) {
            throw new PrinterException("No page format specified");
        }

        if (pageIndex > 0) { /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        pageFormat.setOrientation(orientation.get());

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        /* Now we perform our rendering */
        graphics.drawImage(this.bufferedImage.get(), 0, 0, null);

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }

    public void setImage(BufferedImage bufferedImage) {
        this.bufferedImage.set(bufferedImage);
    }
    
    public BufferedImage getImage() {
        return this.bufferedImage.get();
    }

    public void setOrientation(String orientation) {
        if (orientation.equalsIgnoreCase("landscape")) {
            this.orientation.set(PageFormat.LANDSCAPE);
        } else if (orientation.equalsIgnoreCase("portrait")) {
            this.orientation.set(PageFormat.PORTRAIT);
        } else if (orientation.equalsIgnoreCase("reverse_landscape") || orientation.equalsIgnoreCase("reverse landscape")) {
            this.orientation.set(PageFormat.REVERSE_LANDSCAPE);
        }
    }

    public void setPrintParameters(PrintApplet rpa) {
        setPrintService(rpa.getPrintService());
        setJobName(rpa.getJobName().replace(" Raw ", " 2D "));
    }

    public void setJobName(String jobName) {
        this.jobName.set(jobName);
    }

    public void setPrintService(PrintService ps) {
        this.ps.set(ps);
    }

    public String getJobName() {
        return jobName.get();
    }

}
