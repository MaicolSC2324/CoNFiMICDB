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
public class ReportesController {

    @FXML
    private Button btnHorasYCiclos;

    @FXML
    private Button btnVolver;

    @Autowired
    private ApplicationContext applicationContext;

    public void initialize() {
        btnHorasYCiclos.setOnAction(event -> abrirReporteHorasYCiclos());
        btnVolver.setOnAction(event -> volver());
    }

    @FXML
    private void abrirReporteHorasYCiclos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HorasYCiclosView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = new Stage();
            stage.setTitle("Reporte: Horas y Ciclos");
            stage.setScene(scene);
            stage.show();

            // Cerrar ventana de reportes
            Stage reportesStage = (Stage) btnHorasYCiclos.getScene().getWindow();
            reportesStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void volver() {
        try {
            // Reabrirla vista principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load(), 900, 600);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("ConFiMICDB");
            stage.show();

            // Cerrar ventana de reportes
            Stage reportesStage = (Stage) btnVolver.getScene().getWindow();
            reportesStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
