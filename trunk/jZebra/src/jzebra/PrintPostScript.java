/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jzebra;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;

/**
 *
 * @author tfino
 */
public class PrintPostScript implements Printable {

    private final AtomicReference<BufferedImage> bufferedImage = new AtomicReference<BufferedImage>(null);
    private final AtomicReference<ByteBuffer> bufferedPDF = new AtomicReference<ByteBuffer>(null);
    //private final AtomicReference<PageFormat> pageFormat = new AtomicReference<PageFormat>();
    //private final AtomicReference<Graphics> graphics = new AtomicReference<Graphics>(null);
    private final AtomicReference<PrintService> ps = new AtomicReference<PrintService>(null);
    private final AtomicReference<String> jobName = new AtomicReference<String>("jZebra 2D Printing");
    private final AtomicInteger orientation = new AtomicInteger(PageFormat.PORTRAIT);
    private final AtomicReference<Paper> paper = new AtomicReference<Paper>(null);
    private String pdfClass;

    public PrintPostScript() {
    }

    /**
     * Can be called directly
     * @throws PrinterException
     */
    public void print() throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        
        // Fixes 1" white border problem - May need tweaking
        int w = bufferedImage.get().getWidth();
        int h = bufferedImage.get().getHeight();
        HashPrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();              
        attr.add(new MediaPrintableArea(0f, 0f, w/72f, h/72f, MediaPrintableArea.INCH));
        
        job.setPrintService(ps.get());
        job.setPrintable(this);
        job.setJobName(jobName.get());
        job.print(attr);

        bufferedImage.set(null);
        bufferedPDF.set(null);
    }

    public void setPaper(Paper paper) {
        this.paper.set(paper);
    }
/*
    public void setMargin(double[] margin) {
        if (margin != null) {
            switch (margin.length) {
                case 1:
                    setMargin(margin[0]);
                    return;
                case 4:
                    setMargin(margin[0], margin[1], margin[2], margin[3]);
                    return;
                default:
                    return;
            }
        }
    }

    public void setMargin(int margin) {
        setMargin((double) margin);
    }

    public void setMargin(double margin) {
        setMargin(margin, margin, margin, margin);
    }

    public void setMargin(double top, double left, double bottom, double right) {
        if (this.paper.get() == null) {
            this.paper.set(new Paper());
        }
        this.paper.get().setImageableArea(left, top, paper.get().getWidth() - (left + right), paper.get().getHeight() - (top + bottom));
    }*/

    /*public void setMargin(Rectangle r) {
    if (this.paper.get() == null) {
    this.paper.set(new Paper());
    }
    this.paper.get().setImageableArea(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }*/

    /*public int print(Applet applet, BufferedImage bufferedImage) throws PrinterException {
    this.bufferedImage.set(bufferedImage);
    return print(applet.getGraphics(), this.pageFormat.get(), 0);
    }*/
    /**
     * Implemented by Printable interface.  Should not be called directly, see print() instead
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     * @throws PrinterException
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (paper.get() != null) {
            pageFormat.setPaper(paper.get());
        }

        if (this.bufferedImage.get() != null) {
            return printImage(graphics, pageFormat, pageIndex);
        } else if (this.bufferedPDF.get() != null) {
            // PDF-Renderer plugin
            if (isClass("com.sun.pdfview.PDFFile")) {
                return printPDFRenderer(graphics, pageFormat, pageIndex);

                /**
                // Paid/Licensed version detector
                else if (isClass("org.jpedal.utils.PdfBook")  {
                return printJPedalNonFree(graphics, pageFormat, pageIndex);
                
                }* 
                }  else if (isClass("org.jpedal.utils.PdfBook") || isClass("org.jpedal.PdfDecoder")) {
                boolean hiRes =  isClass("org.jpedal.utils.PdfBook");
                return printJPedal(graphics, pageFormat, pageIndex, hiRes);
                 * 
                 * */
            } else {
                throw new PrinterException("No suitable PDF render was found in the 'lib' directory.");
            }
        } else {
            throw new PrinterException("Unupported file/data type was supplied");
        }
//}
    }

    private boolean isClass(String className) {
        if (className != null && className.equals(pdfClass)) {
            return true;
        }
        try {
            Class.forName(className);
            LogIt.log("Using PDF renderer: " + className);
            pdfClass = className;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*private int printJPedalNonFree(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    PdfDecoder pdf = getPDFDecoder();
    
    pdf.setPrintAutoRotateAndCenter(false);
    pdf.setPrintPageScalingMode(PrinterOptions.PAGE_SCALING_NONE);
    PdfBook pdfBook = new PdfBook(pdf, , attributes);
    pdfBook.setChooseSourceByPdfPageSize(false);
    
    return 0;
    }*/
    /**
     * JPedal renders a very poor quality image representation of the 
     * PDF document.  Paid version seems to support hiRes
     *
    private int printJPedal(Graphics graphics, PageFormat pageFormat, int pageIndex, boolean hiRes) throws PrinterException {
    PdfDecoder pdf = getPDFDecoder();
    try {
    Map mapValues = new HashMap();
    mapValues.put(JPedalSettings.IMAGE_HIRES, true);
    mapValues.put(JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING, new Integer(1));
    bufferedImage.set(hiRes ? pdf.getPageAsHiRes(pageIndex + 1, mapValues) :  
    pdf.getPageAsImage(pageIndex + 1));
    return printImage(graphics, pageFormat, pageIndex);
    } catch (Exception e) {
    throw new PrinterException(e.getMessage());
    }
     * 
     * 
     */
    /*int pg = pageIndex + 1;
    
    if (pdf == null) {
    throw new PrinterException("No PDF data specified");
    }
    
    if (pg < 1 || pg > pdf.getNumberOfPages()) {
    return NO_SUCH_PAGE;
    }; 
    
    pdf.setPrintAutoRotateAndCenter(false);
    try {
    
    printImage(graphics, pageFormat, pageIndex);
    }
    
    }*/
    private int printPDFRenderer(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        PDFFile pdf = getPDFFile();

        int pg = pageIndex + 1;

        if (pdf == null) {
            throw new PrinterException("No PDF data specified");
        }

        if (pg < 1 || pg > pdf.getNumPages()) {
            return NO_SUCH_PAGE;
        };

        // fit the PDFPage into the printing area
        Graphics2D g2 = (Graphics2D) graphics;
        PDFPage page = pdf.getPage(pg);


        //double pwidth = page.getWidth(); //pdf.getImageableWidth();
        //double pheight = page.getHeight(); //pdf.getImageableHeight();

        //double aspect = page.getAspectRatio();
        //double paperaspect = pwidth / pheight;

        /*
        Rectangle imgbounds;
        
        if (aspect > paperaspect) {
        // paper is too tall / pdfpage is too wide
        int height = (int) (pwidth / aspect);
        imgbounds = new Rectangle(
        (int) pageFormat.getImageableX(),
        (int) (pageFormat.getImageableY() + ((pheight - height) / 2)),
        (int) pwidth,
        height);
        } else {
        // paper is too wide / pdfpage is too tall
        int width = (int) (pheight * aspect);
        imgbounds = new Rectangle(
        (int) (pageFormat.getImageableX() + ((pwidth - width) / 2)),
        (int) pageFormat.getImageableY(),
        width,
        (int) pheight);
        }*/

        // render the page
        //Rectangle imgbounds = new Rectangle(pg, pg)
        PDFRenderer pgs = new PDFRenderer(page, g2, page.getPageBox().getBounds(), page.getBBox(), null);
        //PDFRenderer pgs = new PDFRenderer(page, g2, getImageableRectangle(pageFormat), page.getBBox(), null);
         try {
            page.waitForFinish();

            pgs.run();
        } catch (InterruptedException ie) {
        }

        return PAGE_EXISTS;
    }

    private Rectangle getImageableRectangle(PageFormat format) {
        return new Rectangle(
                (int) format.getImageableX(), (int) format.getImageableY(),
                (int) format.getImageableWidth(), (int) format.getImageableHeight());
    }

    private int printImage(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        /* Graphics and pageFormat are required.  Page index is zero-based */
        if (graphics == null) {
            throw new PrinterException("No graphics specified");
        }
        if (pageFormat == null) {
            throw new PrinterException("No page format specified");
        }
        if (pageIndex > 0) {
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

    /*private PdfDecoder getPDFDecoder() throws PrinterException {
    try {
    Map mapValues = new HashMap();
    //do not scale above this figure
    PdfDecoder pdf = new PdfDecoder(true);
    pdf.openPdfArray(this.bufferedPDF.get().array());
    return pdf;
    } catch (Exception e) {
    throw new PrinterException(e.getMessage());
    }
    }*/
    private PDFFile getPDFFile() throws PrinterException {
        try {
            return new PDFFile(this.bufferedPDF.get());
        } catch (Exception e) {
            throw new PrinterException(e.getMessage());
        }
    }
    
    public void setImage(byte[] imgData) throws IOException {
        InputStream in = new ByteArrayInputStream(imgData);
        this.bufferedImage.set(ImageIO.read(in));
    }

    public void setImage(BufferedImage bufferedImage) {
        this.bufferedImage.set(bufferedImage);
    }

    public void setPDF(ByteBuffer bufferedPDF) {
        this.bufferedPDF.set(bufferedPDF);
    }

    public ByteBuffer getPDF() {
        return this.bufferedPDF.get();
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
//        setMargin(rpa.getPSMargin());
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
