package at.htlstp.felerfrei.services.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

@Service("pdfWriter")
public class PDFWriterService implements PDFWriter {

    /**
     * Writes a PDF file with the given paragraphs.
     * @param fileName the name of the file to write to, excluding the file extension
     * @param paragraphs the paragraphs to write
     */
    @Override
    public void write(String fileName, List<Paragraph> paragraphs) {
        var document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("fileoutput/" + fileName + ".pdf"));
            document.open();

            var header = new Chunk("Felerfrei");
            var address = new Chunk("""
                    Felerfrei
                    Hauptstraße 1
                    1040 Wien
                    Telefon: +43 1 511 511
                    """);

            var headerParagraph = new Paragraph(header);
            headerParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(headerParagraph);

            var addressParagraph = new Paragraph(address);
            addressParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(addressParagraph);

            for (var paragraph : paragraphs) {
                document.add(paragraph);
            }

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

    }
}
