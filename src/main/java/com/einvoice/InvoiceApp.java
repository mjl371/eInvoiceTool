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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.excel.EasyExcel;

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
        // 新增列
        TableColumn<InvoiceWrapper, String> codeColumn = new TableColumn<>("发票代码");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<InvoiceWrapper, String> numberColumn = new TableColumn<>("发票号码"); 
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        
        TableColumn<InvoiceWrapper, String> dateColumn = new TableColumn<>("开票日期");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<InvoiceWrapper, String> buyerNameColumn = new TableColumn<>("购方名称");
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        
        TableColumn<InvoiceWrapper, String> amountColumn = new TableColumn<>("金额");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        // 更新列添加列表（替换原来的addAll）
        tableView.getColumns().addAll(fileNameColumn, titleColumn, machineNumberColumn, 
            codeColumn, numberColumn, dateColumn, buyerNameColumn, amountColumn);
        // 创建按钮
        Button importButton = new Button("导入");
        importButton.setOnAction(e -> importFiles(primaryStage));

        // 新增文件夹导入按钮
        Button importFolderButton = new Button("导入文件夹");
        importFolderButton.setOnAction(e -> importFolder(primaryStage));
        
        Button parseButton = new Button("解析");
        parseButton.setOnAction(e -> parseFiles());

        // 新增导出按钮
        Button exportButton = new Button("导出Excel");
        exportButton.setOnAction(e -> exportToExcel());

        // 更新按钮布局（添加导出按钮）
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(importButton, importFolderButton, parseButton, exportButton);
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
                invoiceList.add(new InvoiceWrapper(file, null));
            }
        }
    }

    private void parseFiles() {
        List<InvoiceWrapper> parsedList = new ArrayList<>();
        for (InvoiceWrapper wrapper : invoiceList) {
            File file = wrapper.invoiceFile;
            try {
                Invoice invoice = null;
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    invoice = PdfInvoiceExtractor.extract(file);
                } else if (file.getName().toLowerCase().endsWith(".ofd")) {
                    invoice = OfdInvoiceExtractor.extract(file);
                }
                parsedList.add(new InvoiceWrapper(file, invoice));
            } catch ( Exception e) {
                e.printStackTrace();
            }
        }
        invoiceList.setAll(parsedList);
    }

    // 新增文件夹导入方法
    private void importFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(primaryStage);
        
        if (directory != null && directory.isDirectory()) {
            List<File> files = new ArrayList<>();
            collectFiles(directory, files);
            
            for (File file : files) {
                invoiceList.add(new InvoiceWrapper(file, null));
            }
        }
    }
    // 递归收集文件的方法
    private void collectFiles(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectFiles(file, fileList);
                } else {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".pdf") || name.endsWith(".ofd")) {
                        fileList.add(file);
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }

    // 新增导出方法
private void exportToExcel() {
    List<Invoice> invoices = new ArrayList<>();
    for (InvoiceWrapper wrapper : invoiceList) {
        if (wrapper.invoice != null) {
            invoices.add(wrapper.invoice);
        }
    }
    
    if (invoices.isEmpty()) {
        showAlert("提示", "没有可导出的发票数据");
        return;
    }

    DirectoryChooser chooser = new DirectoryChooser();
    File directory = chooser.showDialog(null);
    
    if (directory != null) {
        String path = directory.getAbsolutePath();
        String fileName = path + "\\发票数据_" + System.currentTimeMillis() + ".xlsx";
        
        try {
            EasyExcel.write(fileName, Invoice.class)
                    .sheet("发票数据")
                    .doWrite(invoices);
            showAlert("导出成功", "文件已保存至：" + fileName);
        } catch (Exception e) {
            showAlert("导出失败", "错误信息：" + e.getMessage());
        }
    }
}

// 新增警告框方法
private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
}

    public static class InvoiceWrapper {
  
        private Invoice invoice;
        private File invoiceFile;

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
    }
}

