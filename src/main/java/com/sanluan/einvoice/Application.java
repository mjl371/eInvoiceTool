package com.sanluan.einvoice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sanluan.einvoice.service.OfdInvoiceExtractor;
import com.sanluan.einvoice.service.PdfInvoiceExtractor;
import com.alibaba.excel.EasyExcel;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
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

        // writeToCsv(invoiceList, folderPath);
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

    public static void writeToCsv(List<Invoice> invoiceList, String csvPath) {

        try {

            FileOutputStream fos = new FileOutputStream(csvPath + "\\"+System.currentTimeMillis()+"output.csv");
            // excel 乱码请用gbk
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            CSVWriter writer = new CSVWriter(osw);

            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setType(Invoice.class);

            StatefulBeanToCsvBuilder<Invoice> builder = new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();

            String[] columns = new String[] {
                    "title", "machineNumber", "code", "number", "date", "checksum", "buyerName", "buyerCode",
                    "buyerAddress", "buyerAccount", "password", "amount", "taxAmount", "totalAmountString",
                    "totalAmount", "sellerName", "sellerCode", "sellerAddress", "sellerAccount", "payee", "reviewer",
                    "drawer", "type"
            };
            mappingStrategy.setColumnMapping(columns);

            beanWriter.write(invoiceList);

            writer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void writeToExcel(List<Invoice> invoiceList, String excelPath) {

        String fileName = excelPath + "\\"+System.currentTimeMillis()+"output.xlsx";
        EasyExcel.write(fileName, Invoice.class)
                .sheet("工作表1")
                .doWrite(invoiceList);

    }

}
