package com.einvoice;

import com.einvoice.entity.Invoice;
import com.einvoice.service.OfdInvoiceExtractor;
import com.einvoice.service.PdfInvoiceExtractor;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceApp extends Application {

    private TableView<InvoiceWrapper> tableView;
    private ObservableList<InvoiceWrapper> invoiceList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // 创建表格
        tableView = new TableView<>();
        tableView.setItems(invoiceList);

        // 添加列
        TableColumn<InvoiceWrapper, String> fileNameColumn = new TableColumn<>("发票文件名");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<InvoiceWrapper, String> titleColumn = new TableColumn<>("发票标题");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<InvoiceWrapper, String> machineNumberColumn = new TableColumn<>("机器编号");
        machineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("machineNumber"));

        // 添加其他列...

        tableView.getColumns().addAll(fileNameColumn, titleColumn, machineNumberColumn);

        // 创建按钮
        Button importButton = new Button("导入");
        importButton.setOnAction(e -> importFiles(primaryStage));

        Button parseButton = new Button("解析");
        parseButton.setOnAction(e -> parseFiles());

        // 创建按钮布局
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(importButton, parseButton);
        buttonBox.setPadding(new Insets(10));

        // 创建主布局
        BorderPane root = new BorderPane();
        root.setCenter(tableView);
        root.setBottom(buttonBox);

        // 创建场景
        Scene scene = new Scene(root, 800, 600);

        // 设置舞台
        primaryStage.setTitle("发票解析工具");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void importFiles(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("OFD Files", "*.ofd")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        if (files != null) {
            for (File file : files) {
                invoiceList.add(new InvoiceWrapper(file.getName(), null));
            }
        }
    }

    private void parseFiles() {
        List<InvoiceWrapper> parsedList = new ArrayList<>();
        for (InvoiceWrapper wrapper : invoiceList) {
            File file = new File(wrapper.getFileName());
            try {
                Invoice invoice = null;
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    invoice = PdfInvoiceExtractor.extract(file);
                } else if (file.getName().toLowerCase().endsWith(".ofd")) {
                    invoice = OfdInvoiceExtractor.extract(file);
                }
                parsedList.add(new InvoiceWrapper(wrapper.getFileName(), invoice));
            } catch ( Exception e) {
                e.printStackTrace();
            }
        }
        invoiceList.setAll(parsedList);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class InvoiceWrapper {
        private String fileName;
        private Invoice invoice;

        public InvoiceWrapper(String fileName, Invoice invoice) {
            this.fileName = fileName;
            this.invoice = invoice;
        }

        public String getFileName() {
            return fileName;
        }

        public String getTitle() {
            return invoice != null ? invoice.getTitle() : "";
        }

        public String getMachineNumber() {
            return invoice != null ? invoice.getMachineNumber() : "";
        }

        // 添加其他属性的getter方法...
    }
}