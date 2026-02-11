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
public class FormulariosController {

    @FXML
    private Button btnGestionarAeronaves;

    @FXML
    private Button btnGestionarHojasLibro;

    @FXML
    private Button btnRegistrarDisponibilidad;

    @FXML
    private Button btnVolver;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        btnGestionarAeronaves.setOnAction(event -> abrirGestionarAeronaves());
        btnGestionarHojasLibro.setOnAction(event -> abrirGestionarHojasLibro());
        btnRegistrarDisponibilidad.setOnAction(event -> abrirRegistrarDisponibilidad());
        btnVolver.setOnAction(event -> volver());
    }

    private void abrirGestionarAeronaves() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AircraftView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Gestionar Aeronaves");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirGestionarHojasLibro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HojaLibroView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Gestionar Hojas del Libro de Vuelo");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirRegistrarDisponibilidad() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DisponibilidadAeronavesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Disponibilidad de Aeronaves");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirTipoOperacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TipoOperacionView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Gestionar Tipos de Operación");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irAMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("ConFiMIC DB");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void volver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("ConFiMIC DB");
            stage.setMaximized(true);
            stage.show();

            cerrarVentanaActual();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarVentanaActual() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }
}
