package com.einvoice;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class InvoiceApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/einvoice/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 800, 600);
        // 加载 Logo 图片
        Image icon = new Image(getClass().getResourceAsStream("/com/einvoice/logo.png")); // 替换为你的 Logo 文件路径

        // 设置窗口图标
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("发票解析工具");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
