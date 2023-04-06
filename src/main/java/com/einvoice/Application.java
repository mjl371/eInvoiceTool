package com.einvoice;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.einvoice.service.OfdInvoiceExtractor;
import com.einvoice.service.PdfInvoiceExtractor;
import com.alibaba.excel.EasyExcel;

import com.einvoice.entity.Invoice;

public class Application {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入发票文件夹路径: ");
        String input = scanner.nextLine();
        scanner.close();
        System.out.println("输入路径: " + input);
        getFolder(input);


    }

    public static void getFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        int invoiceCount = 0;
        BigDecimal invoiceAmount = new BigDecimal(0);

        List<Invoice> invoiceList = new ArrayList<Invoice>();

        for (File file : listOfFiles) {
            try {
                if (file.isFile()) {
                    String fileName = file.getName().toLowerCase();
                    System.out.println(fileName);
                    Invoice invoice = null;
                    if (fileName.endsWith(".pdf")) {
                        invoice = renameInvoice(file, true);
                    } else if (fileName.endsWith(".ofd")) {
                        invoice = renameInvoice(file, false);
                    } else {
                        continue;
                    }
                    invoiceList.add(invoice);
                    invoiceCount++;
                    invoiceAmount = invoiceAmount.add(invoice.getTotalAmount());

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("解析失败");
                continue;
            }

        }

        writeToExcel(invoiceList, folderPath);

        System.out.println(invoiceCount + " " + invoiceAmount);

    }

    public static Invoice renameInvoice(File invoFile, boolean isPdf) {

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

            return invoice;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void writeToExcel(List<Invoice> invoiceList, String excelPath) {

        String fileName = excelPath + "\\" + System.currentTimeMillis() + "output.xlsx";
        EasyExcel.write(fileName, Invoice.class)
                .sheet("工作表1")
                .doWrite(invoiceList);

    }

}
