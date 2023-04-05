package com.sanluan.einvoice;

import java.io.File;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sanluan.einvoice.service.OfdInvoiceExtractor;
import com.sanluan.einvoice.service.PdfInvoiceExtractor;
import com.sanluan.einvoice.service.Invoice;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // getFolder("C:\\workCode\\test");
        // Logger logger = LoggerFactory.getLogger(Application.class);

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入发票文件夹路径: ");
        String input = scanner.nextLine();
        System.out.println("输入路径: " + input);
        getFolder(input);

    }

    public static BigDecimal testInvoice(File invoFile, boolean isPdf) {

        try {

            Invoice invoice = new Invoice();
            if (isPdf) {
                invoice = PdfInvoiceExtractor.extract(invoFile);
            } else {
                try {
                    invoice = OfdInvoiceExtractor.extract(invoFile);
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
            String fileExtension = isPdf ? ".pdf" : ".ofd";

            String newName = invoFile.getParent() + "\\"
                    + invoice.getCode() +
                    "_" + invoice.getNumber()
                    + "_" + invoice.getDate()
                    + fileExtension;

            File newFile = new File(newName);

            if (invoFile.renameTo(newFile)) {
                System.out.println("Rename successful " + newFile.getName());
            } else {
                System.out.println("Rename failed " + invoFile.getName());

            }

            System.out.println(invoice.getTotalAmount());

            return invoice.getTotalAmount();

        } catch (IOException e) {
            e.printStackTrace();
            return new BigDecimal(0);
        }

    }

    public static void getFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        int invoiceCount = 0;

        BigDecimal invoiceAmount = new BigDecimal(0);

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String fileName = file.getName().toLowerCase();
                System.out.println(fileName);
                if (fileName.endsWith(".pdf")) {

                    invoiceAmount = invoiceAmount.add(testInvoice(file, true));
                    invoiceCount++;

                } else if (fileName.endsWith(".ofd")) {
                    invoiceAmount = invoiceAmount.add(testInvoice(file, false));
                    invoiceCount++;

                }

            }
        }

        System.out.println(invoiceCount + " " + invoiceAmount);

    }
}
