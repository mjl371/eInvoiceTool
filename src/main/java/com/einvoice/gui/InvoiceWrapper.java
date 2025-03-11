package com.einvoice.gui;

import java.io.File;

import com.einvoice.entity.Invoice;

public class InvoiceWrapper {
  
  
        public Invoice invoice;
        public File invoiceFile;

        public InvoiceWrapper(File invoiceFile, Invoice invoice) {
            this.invoice = invoice;
            this.invoiceFile = invoiceFile; 
        }

        public String getFilePath() {
            return invoiceFile != null ? invoiceFile.getAbsolutePath() : "";
        }

        public String getFileName() {
            return invoiceFile != null ? invoiceFile.getName() : "";
        }

        public String getTitle() {
            return invoice != null ? invoice.getTitle() : "";
        }

        public String getMachineNumber() {
            return invoice != null ? invoice.getMachineNumber() : "";
        }

        public String getCode() {
            return invoice != null ? invoice.getCode() : "";
        }

        public String getNumber() {
            return invoice != null ? invoice.getNumber() : "";
        }

        public String getDate() {
            return invoice != null ? invoice.getDate() : "";
        }

        public String getBuyerName() {
            return invoice != null ? invoice.getBuyerName() : "";
        }

        public String getAmount() {
            return invoice != null ? invoice.getAmount().toPlainString() : "";
        }
    
    
        public void setInvoiceFile(File file) {
            this.invoiceFile = file;
        }
        
        public void setFileName(File newFile) {
            if (invoiceFile.renameTo(newFile)) {
                System.out.println("重命名成功 " + newFile.getName());
            } else {
                System.out.println("重命名成功失败！检查是否占用文件或存在同名 " + invoiceFile.getName());
            }
        }
}

