package com.einvoice.gui;

import com.einvoice.extractor.ParseExtractor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.util.Arrays; // 添加缺失的导入语句
import java.util.Objects;
import com.einvoice.entity.Invoice;
import java.io.IOException;

public class MainController {
    @FXML private Button btnSelectFolder;
    @FXML private ListView<String> fileListView;
    @FXML private ProgressBar progressBar;
    // 新增表格相关控件
    @FXML private TableView<InvoiceTableModel> invoiceTable;
    @FXML private TableColumn<InvoiceTableModel, String> colFileName;
    @FXML private TableColumn<InvoiceTableModel, String> colCode;
    @FXML private TableColumn<InvoiceTableModel, String> colNumber;
    @FXML private TableColumn<InvoiceTableModel, String> colAmount;
    
    private final ObservableList<InvoiceTableModel> tableData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // 初始化表格列绑定
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        invoiceTable.setItems(tableData);
    }
    
    @FXML
    private void handleSelectFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File selectedDir = chooser.showDialog(btnSelectFolder.getScene().getWindow());
        
        if (selectedDir != null) {
            processFiles(selectedDir);
        }
    }
    
    // 修改后的文件处理逻辑
private void processFiles(File directory) {
    tableData.clear();
    Arrays.stream(Objects.requireNonNull(directory.listFiles()))
        .filter(f -> f.getName().toLowerCase().matches(".*\\.(pdf|ofd)$"))
        .forEach(f -> tableData.add(new InvoiceTableModel(f.getName())));
}
    
    @FXML
    private void handleParse() {
        tableData.forEach(item -> {
            try {
                // 假设 InvoiceTableModel 中有一个方法可以获取文件名
                // 此处原代码中 getFileName 方法未定义，需要确保 InvoiceTableModel 类有该方法
                // 若没有该方法，需要添加该方法到 InvoiceTableModel 类中
                String fileName = item.getFileName(); 
                File file = new File(new File(System.getProperty("user.dir")), fileName);
                Invoice invoice = ParseExtractor.getInvoice(file);
                item.updateFromInvoice(invoice);
            } catch (IOException e) {
                // 定义 showErrorDialog 方法
              
            }


        });
    }
}