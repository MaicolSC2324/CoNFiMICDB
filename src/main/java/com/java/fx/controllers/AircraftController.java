package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.services.AircraftService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class AircraftController {

    @FXML
    private TextField txtMatricula;
    @FXML
    private TextField txtFabricante;
    @FXML
    private TextField txtModelo;
    @FXML
    private TextField txtSerie;
    @FXML
    private TextField txtPropietario;
    @FXML
    private TextField txtExplotador;
    @FXML
    private TextField txtTSN;
    @FXML
    private TextField txtCSN;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnVolver;

    @FXML
    private TableView<Aircraft> tableAircraft;
    @FXML
    private TableColumn<Aircraft, String> colMatricula;
    @FXML
    private TableColumn<Aircraft, String> colFabricante;
    @FXML
    private TableColumn<Aircraft, String> colModelo;
    @FXML
    private TableColumn<Aircraft, BigDecimal> colTSN;
    @FXML
    private TableColumn<Aircraft, Integer> colCSN;

    @Autowired
    private AircraftService aircraftService;

    private Aircraft aircraftSeleccionado = null;
    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();

    public void initialize() {
        // Configurar columnas de la tabla
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colFabricante.setCellValueFactory(new PropertyValueFactory<>("fabricante"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colTSN.setCellValueFactory(new PropertyValueFactory<>("tsn"));
        colCSN.setCellValueFactory(new PropertyValueFactory<>("csn"));

        // Cargar datos
        cargarAircraft();

        // Configurar selección de fila
        tableAircraft.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                aircraftSeleccionado = newVal;
                cargarFormulario(newVal);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            }
        });

        // Configurar botones
        btnGuardar.setOnAction(event -> guardar());
        btnActualizar.setOnAction(event -> actualizar());
        btnEliminar.setOnAction(event -> eliminar());
        btnLimpiar.setOnAction(event -> limpiarFormulario());
        btnVolver.setOnAction(event -> volver());
    }

    private void cargarAircraft() {
        try {
            List<Aircraft> lista = aircraftService.findAll();
            aircraftList = FXCollections.observableArrayList(lista);
            tableAircraft.setItems(aircraftList);
        } catch (Exception e) {
            mostrarError("Error al cargar aeronaves", e.getMessage());
        }
    }

    private void cargarFormulario(Aircraft aircraft) {
        txtMatricula.setText(aircraft.getMatricula());
        txtFabricante.setText(aircraft.getFabricante());
        txtModelo.setText(aircraft.getModelo());
        txtSerie.setText(aircraft.getSerie() != null ? aircraft.getSerie() : "");
        txtPropietario.setText(aircraft.getPropietario() != null ? aircraft.getPropietario() : "");
        txtExplotador.setText(aircraft.getExplotador() != null ? aircraft.getExplotador() : "");
        txtTSN.setText(aircraft.getTsn().toString());
        txtCSN.setText(aircraft.getCsn().toString());
    }

    @FXML
    private void guardar() {
        try {
            if (validarFormulario()) {
                Aircraft aircraft = new Aircraft();
                aircraft.setMatricula(txtMatricula.getText().trim());
                aircraft.setFabricante(txtFabricante.getText().trim());
                aircraft.setModelo(txtModelo.getText().trim());
                aircraft.setSerie(txtSerie.getText().isEmpty() ? null : txtSerie.getText().trim());
                aircraft.setPropietario(txtPropietario.getText().isEmpty() ? null : txtPropietario.getText().trim());
                aircraft.setExplotador(txtExplotador.getText().isEmpty() ? null : txtExplotador.getText().trim());
                aircraft.setTsn(new BigDecimal(txtTSN.getText().trim()));
                aircraft.setCsn(Integer.parseInt(txtCSN.getText().trim()));

                aircraftService.save(aircraft);
                mostrarInfo("Éxito", "Aeronave guardada exitosamente");
                limpiarFormulario();
                cargarAircraft();
            }
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        try {
            if (aircraftSeleccionado == null) {
                mostrarError("Error", "Debe seleccionar una aeronave");
                return;
            }

            if (validarFormulario()) {
                aircraftSeleccionado.setMatricula(txtMatricula.getText().trim());
                aircraftSeleccionado.setFabricante(txtFabricante.getText().trim());
                aircraftSeleccionado.setModelo(txtModelo.getText().trim());
                aircraftSeleccionado.setSerie(txtSerie.getText().isEmpty() ? null : txtSerie.getText().trim());
                aircraftSeleccionado.setPropietario(txtPropietario.getText().isEmpty() ? null : txtPropietario.getText().trim());
                aircraftSeleccionado.setExplotador(txtExplotador.getText().isEmpty() ? null : txtExplotador.getText().trim());
                aircraftSeleccionado.setTsn(new BigDecimal(txtTSN.getText().trim()));
                aircraftSeleccionado.setCsn(Integer.parseInt(txtCSN.getText().trim()));

                aircraftService.save(aircraftSeleccionado);
                mostrarInfo("Éxito", "Aeronave actualizada exitosamente");
                limpiarFormulario();
                cargarAircraft();
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar", e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        try {
            if (aircraftSeleccionado == null) {
                mostrarError("Error", "Debe seleccionar una aeronave");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Desea eliminar esta aeronave?");
            alert.setContentText("Matrícula: " + aircraftSeleccionado.getMatricula());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                aircraftService.delete(aircraftSeleccionado);
                mostrarInfo("Éxito", "Aeronave eliminada exitosamente");
                limpiarFormulario();
                cargarAircraft();
                aircraftSeleccionado = null;
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar", e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtMatricula.clear();
        txtFabricante.clear();
        txtModelo.clear();
        txtSerie.clear();
        txtPropietario.clear();
        txtExplotador.clear();
        txtTSN.setText("0.00");
        txtCSN.setText("0");
        tableAircraft.getSelectionModel().clearSelection();
        aircraftSeleccionado = null;
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void volver() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.close();
    }

    private boolean validarFormulario() {
        if (txtMatricula.getText().trim().isEmpty()) {
            mostrarError("Validación", "La matrícula es obligatoria");
            return false;
        }
        if (txtFabricante.getText().trim().isEmpty()) {
            mostrarError("Validación", "El fabricante es obligatorio");
            return false;
        }
        if (txtModelo.getText().trim().isEmpty()) {
            mostrarError("Validación", "El modelo es obligatorio");
            return false;
        }

        try {
            new BigDecimal(txtTSN.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Validación", "TSN debe ser un número válido");
            return false;
        }

        try {
            Integer.parseInt(txtCSN.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Validación", "CSN debe ser un número entero válido");
            return false;
        }

        return true;
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

