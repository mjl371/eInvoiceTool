package com.einvoice.gui;

import com.einvoice.entity.Invoice;
import javafx.beans.property.SimpleStringProperty;

public class InvoiceTableModel {
    private final SimpleStringProperty fileName;
    private final SimpleStringProperty code;
    private final SimpleStringProperty number;
    private final SimpleStringProperty amount;

    public InvoiceTableModel(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
        this.code = new SimpleStringProperty("");
        this.number = new SimpleStringProperty("");
        this.amount = new SimpleStringProperty("");
    }

    // 更新方法
    public void updateFromInvoice(Invoice invoice) {
        code.set(invoice.getCode());
        number.set(invoice.getNumber());
        amount.set(invoice.getAmount() != null ? invoice.getAmount().toString() : "");
    }

    // 补充属性访问方法
    public String getFileName() {
        return fileName.get();
    }

    public String getCode() {
        return code.get();
    }

    public String getNumber() {
        return number.get();
    }

    public String getAmount() {
        return amount.get();
    }
}