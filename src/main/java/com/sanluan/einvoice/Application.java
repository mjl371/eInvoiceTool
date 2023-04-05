package com.sanluan.einvoice;

import java.io.File;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sanluan.einvoice.service.OfdInvoiceExtractor;
import com.sanluan.einvoice.service.PdfInvoiceExtractor;
import com.sanluan.einvoice.service.Invoice;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入发票文件夹路径: ");
        String input = scanner.nextLine();
        System.out.println("输入路径: " + input);
        getFolder(input);

    }

    public static BigDecimal renameInvoice(File invoFile, boolean isPdf) {

        try {

            Invoice invoice = new Invoice();
            if (isPdf) {
                invoice = PdfInvoiceExtractor.extract(invoFile);
            } else {
                try {
                    invoice = OfdInvoiceExtractor.extract(invoFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String fileExtension = isPdf ? ".pdf" : ".ofd";

            String newName = invoFile.getParent() + "\\"
                    + invoice.getDate().replace("年", "").replace("月", "").replace("日", "")
                    + "_" + invoice.getSellerName()
                    + "_" + invoice.getTotalAmount()
                    + "_" + invoice.getNumber()
                    + fileExtension;

            File newFile = new File(newName);

            if (invoFile.renameTo(newFile)) {
                System.out.println("重命名成功 " + newFile.getName());
            } else {
                System.out.println("重命名成功失败！检查是否占用文件或存在同名 " + invoFile.getName());
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
            try{
                if (file.isFile()) {
                    String fileName = file.getName().toLowerCase();
                    System.out.println(fileName);
                    if (fileName.endsWith(".pdf")) {
    
                        invoiceAmount = invoiceAmount.add(renameInvoice(file, true));
                        invoiceCount++;
    
                    } else if (fileName.endsWith(".ofd")) {
                        invoiceAmount = invoiceAmount.add(renameInvoice(file, false));
                        invoiceCount++;
    
                    }
    
                }
            }catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("解析失败");
                continue;
            }

           
        }

        System.out.println(invoiceCount + " " + invoiceAmount);

    }
}
