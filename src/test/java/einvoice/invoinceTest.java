package einvoice;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.einvoice.entity.Invoice;
import com.einvoice.service.OfdInvoiceExtractor;
import com.einvoice.service.PdfInvoiceExtractor;
import com.einvoice.service.QrPdf;
import com.google.zxing.NotFoundException;

public class invoinceTest {


    String pdfTestPath = "";
    String ofdTestPath = "";    


    @Test
    public void ofdTest() throws IOException, DocumentException {
        System.out.println(
                OfdInvoiceExtractor.extract(new File(ofdTestPath)));

    }

    @Test
    public void qrCodeTest() throws IOException, NotFoundException {
        Path pdfPath = Paths.get(pdfTestPath);
        QrPdf qrPdf = new QrPdf(pdfPath);
        String qrcode = qrPdf.getQRCode(1);
        System.out.println("解析后的值 " + qrcode);
        String[] invoiceQrCodeArray = qrcode.split(",");
        Invoice invoice = new Invoice();
        invoice.setCode(invoiceQrCodeArray[2]);
        invoice.setNumber(invoiceQrCodeArray[3]);
        invoice.setTotalAmount(new BigDecimal(invoiceQrCodeArray[4]));
        invoice.setDate(invoiceQrCodeArray[5]);
        invoice.setCheckCode(invoiceQrCodeArray[6]);

        System.out.println(invoice);
    }

    @Test
    public void pdfTest() throws IOException {
        System.out.println(
                PdfInvoiceExtractor.extract(new File(pdfTestPath)));

    }

}
