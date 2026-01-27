package com.java.fx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class MainController {

    @FXML
    private Button btnFormularios;
    @FXML
    private Button btnReportes;
    @FXML
    private Button btnExit;

    @Autowired
    private ApplicationContext applicationContext;

    public void initialize() {
        btnFormularios.setOnAction(event -> openFormulariosView());
        btnReportes.setOnAction(event -> openReportesView());
        btnExit.setOnAction(event -> handleExit());
    }

    @FXML
    private void openFormulariosView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulariosView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Formularios");
            stage.setMaximized(true);
            stage.show();

            // Cerrar menú principal
            Stage mainStage = (Stage) btnFormularios.getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openReportesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reportes");
            stage.setMaximized(true);
            stage.show();

            // Cerrar menú principal
            Stage mainStage = (Stage) btnReportes.getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}

