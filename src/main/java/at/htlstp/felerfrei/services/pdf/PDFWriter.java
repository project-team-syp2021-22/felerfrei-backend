package at.htlstp.felerfrei.services.pdf;

import com.itextpdf.text.Paragraph;

import java.util.List;

public interface PDFWriter {

    void write(String fileName, List<Paragraph> paragraphs);

}
