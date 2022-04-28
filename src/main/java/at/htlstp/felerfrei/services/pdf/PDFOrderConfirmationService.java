package at.htlstp.felerfrei.services.pdf;

import at.htlstp.felerfrei.domain.order.Order;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

// please do not judge me, I hate myself for this code :)
// https://www.mysamplecode.com/2012/10/generate-pdf-using-java-and-itext.html
@Service("pdfConfirmationService")
public class PDFOrderConfirmationService implements OrderConfirmationService{

    private static final String PRICE_FORMAT = "%.2f €";

    public static final String PATH = "orderconfirmations/";

    private BaseFont bfBold;
    private BaseFont bf;
    private int pageNumber = 0;

    public void write(Order order) {
        createPDF(order);
    }

    private void createPDF(Order order) {

        Document doc = new Document();
        PdfWriter docWriter = null;
        initializeFonts();

        try {
            String path = PATH + order.getId() + ".pdf";
            docWriter = PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.addAuthor("felerfrei");
            doc.addCreationDate();
            doc.addProducer();
            doc.addCreator("felerfrei.at");
            doc.addTitle("Bestellbestätigung");
            doc.setPageSize(PageSize.LETTER);

            doc.open();
            PdfContentByte cb = docWriter.getDirectContent();

            boolean beginPage = true;
            int y = 0;

            for (int i = 0; i < order.getOrderContents().size(); i++) {
                if (beginPage) {
                    beginPage = false;
                    generateLayout(cb);
                    generateHeader(cb, order);
                    y = 615;
                }
                generateDetail(cb, i, y, order);
                y = y - 15;
                if (y < 65) {
                    printPageNumber(cb);
                    doc.newPage();
                    beginPage = true;
                }
            }
            printPageNumber(cb);

            createContent(cb, 560, 55, String.format(PRICE_FORMAT, order.calculateTotalPrice()), PdfContentByte.ALIGN_RIGHT);
            cb.rectangle(500, 50, 70, 20);
            cb.stroke();
        } catch (Exception dex) {
            dex.printStackTrace();
        } finally {
            doc.close();
            if (docWriter != null) {
                docWriter.close();
            }
        }
    }

    private void generateLayout(PdfContentByte cb) {
        try {
            cb.setLineWidth(1f);

            // Invoice Header box layout
            cb.rectangle(420, 700, 150, 60);
            cb.moveTo(420, 720);
            cb.lineTo(570, 720);
            cb.moveTo(420, 740);
            cb.lineTo(570, 740);
            cb.moveTo(480, 700);
            cb.lineTo(480, 760);
            cb.stroke();

            // Invoice Header box Text Headings
            createHeadings(cb, 422, 743, "Account No.");
            createHeadings(cb, 422, 723, "Bestellnr.");
            createHeadings(cb, 422, 703, "Bestelldatum");

            // Invoice Detail box layout
            cb.rectangle(40, 70, 530, 580);
            cb.moveTo(40, 630);
            cb.lineTo(570, 630);
            cb.moveTo(70, 70);
            cb.lineTo(70, 650);
            cb.moveTo(150, 70);
            cb.lineTo(150, 650);
            cb.moveTo(430, 70);
            cb.lineTo(430, 650);
            cb.moveTo(500, 70);
            cb.lineTo(500, 650);
            cb.stroke();

            // Invoice Detail box Text Headings
            createHeadings(cb, 42, 633, "Stk.");
            createHeadings(cb, 72, 633, "Prod. Nr.");
            createHeadings(cb, 152, 633, "Prod. Name");
            createHeadings(cb, 432, 633, "Preis pro Stück");
            createHeadings(cb, 502, 633, "Preis Gesamt");

            printHeading(cb);

            //add the images
//            Image companyLogo = Image.getInstance("images/olympics_logo.gif");
//            companyLogo.setAbsolutePosition(25, 700);
//            companyLogo.scalePercent(25);
//            doc.add(companyLogo);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void generateHeader(PdfContentByte cb, Order order) {
        try {

            createHeadings(cb, 40, 750, "Felerfrei");
            createHeadings(cb, 40, 735, "Straße 1");
            createHeadings(cb, 40, 720, "Address Line 2");
            createHeadings(cb, 40, 705, "1040 Wien");
            createHeadings(cb, 40, 690, "Österreich");

            createHeadings(cb, 500, 743, String.valueOf(order.getUser().getId()));
            createHeadings(cb, 500, 723, String.valueOf(order.getId()));
            createHeadings(cb, 482, 703, order.getOrderdate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void generateDetail(PdfContentByte cb, int index, int y, Order order) {

        try {
            var content = order.getOrderContents().get(index);
            createContent(cb, 58, y, String.valueOf(content.getAmount()), PdfContentByte.ALIGN_RIGHT);
            createContent(cb, 80, y, String.valueOf(content.getProduct().getId()), PdfContentByte.ALIGN_LEFT);
            createContent(cb, 152, y, content.getProduct().getName(), PdfContentByte.ALIGN_LEFT);


            double price = content.getRetailPrice();
            double extPrice = content.getAmount() * price;

            createContent(cb, 495, y, String.format(PRICE_FORMAT, price), PdfContentByte.ALIGN_RIGHT);
            createContent(cb, 565, y, String.format(PRICE_FORMAT, extPrice), PdfContentByte.ALIGN_RIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void createHeadings(PdfContentByte cb, float x, float y, String text) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.setTextMatrix(x, y);
        cb.showText(text.trim());
        cb.endText();
    }

    private void printHeading(PdfContentByte cb) {
        cb.beginText();
        cb.setFontAndSize(bfBold, 20);
        cb.setTextMatrix(150, 720);
        cb.showText("Bestellbestätigung");
        cb.endText();
    }

    private void printPageNumber(PdfContentByte cb) {

        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Page No. " + (pageNumber + 1), 570, 25, 0);
        cb.endText();

        pageNumber++;
    }

    private void createContent(PdfContentByte cb, float x, float y, String text, int align) {
        cb.beginText();
        cb.setFontAndSize(bf, 8);
        cb.showTextAligned(align, text.trim(), x, y, 0);
        cb.endText();
    }

    private void initializeFonts() {
        try {
            bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
