
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

public class PdfTest {
    public static void main(String[] args) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            contentStream.setNonStrokingColor(new PDColor(new float[] { 36 / 255f, 64 / 255f, 111 / 255f }, PDDeviceRGB.INSTANCE));
            contentStream.addRect(0, 792 - 115, 612, 115);
            contentStream.fill();
            
            contentStream.beginText();
            contentStream.setNonStrokingColor(new PDColor(new float[] { 255 / 255f, 255 / 255f, 255 / 255f }, PDDeviceRGB.INSTANCE));
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.newLineAtOffset(50, 792 - 50);
            contentStream.showText("Reporte de Evaluacion");
            contentStream.endText();
            
            contentStream.close();
            document.save(new ByteArrayOutputStream());
            document.close();
            System.out.println("PDF generation SUCCESS!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
