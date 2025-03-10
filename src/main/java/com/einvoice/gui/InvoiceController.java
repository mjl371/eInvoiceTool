package com.einvoice.gui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.alibaba.excel.EasyExcel;
import com.einvoice.entity.Invoice;
import com.einvoice.service.OfdInvoiceExtractor;
import com.einvoice.service.PdfInvoiceExtractor;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class InvoiceController implements Initializable {
    
    @FXML
    private Label countLabel;
    
    @FXML
    private TableView<InvoiceWrapper> tableView;

    @FXML private TableColumn<InvoiceWrapper, String> fileNameColumn;
    @FXML private TableColumn<InvoiceWrapper, String> titleColumn;
    @FXML private TableColumn<InvoiceWrapper, String> machineNumberColumn;
    @FXML private TableColumn<InvoiceWrapper, String> codeColumn;
    @FXML private TableColumn<InvoiceWrapper, String> numberColumn;
    @FXML private TableColumn<InvoiceWrapper, String> dateColumn;
    @FXML private TableColumn<InvoiceWrapper, String> buyerNameColumn;
    @FXML private TableColumn<InvoiceWrapper, String> amountColumn;
    
    private ObservableList<InvoiceWrapper> invoiceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // 新增数量绑定
        invoiceList.addListener((ListChangeListener<InvoiceWrapper>) c -> {
            countLabel.setText("已加载发票：" + invoiceList.size());
        });
    }

    private void setupTableColumns() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        machineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("machineNumber"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        tableView.setItems(invoiceList); 

    }

    @FXML
    private void handleImportFiles(ActionEvent event) {
        // 原importFiles方法逻辑迁移至此

        importFiles((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    @FXML 
    private void handleParseFiles(ActionEvent event) {
        // 原parseFiles方法逻辑迁移至此
        parseFiles();
    }

    @FXML
    private void handleImportFolder(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window window = ((Node) event.getSource()).getScene().getWindow();
        File directory = directoryChooser.showDialog(window);
        
        if (directory != null && directory.isDirectory()) {
            List<File> files = new ArrayList<>();
            collectFiles(directory, files);
            
            for (File file : files) {
                invoiceList.add(new InvoiceWrapper(file, null));
            }
        }
    }

    @FXML
    private void handleDeleteSelectedRows(ActionEvent event) {
        ObservableList<InvoiceWrapper> selectedItems = tableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert("提示", "请先选择要删除的行");
            return;
        }
        invoiceList.removeAll(selectedItems);
    }
    
    @FXML
    private void handleClearList(ActionEvent event) {
        invoiceList.clear();
    }
    
    @FXML
    private void handleExportExcel(ActionEvent event) {
        exportToExcel();

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

    // 其他事件处理方法保持不变...
}