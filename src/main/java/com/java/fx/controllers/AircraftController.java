package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.services.AircraftService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class AircraftController {

    @FXML
    private TextField txtMatricula;
    @FXML
    private ComboBox<String> cbFabricante;
    @FXML
    private ComboBox<String> cbModelo;
    @FXML
    private TextField txtSerie;
    @FXML
    private ComboBox<String> cbPropietario;
    @FXML
    private ComboBox<String> cbExplotador;
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
    private TableColumn<Aircraft, String> colSerie;
    @FXML
    private TableColumn<Aircraft, BigDecimal> colTSN;
    @FXML
    private TableColumn<Aircraft, Integer> colCSN;

    @Autowired
    private AircraftService aircraftService;

    @Autowired
    private ApplicationContext applicationContext;

    private Aircraft aircraftSeleccionado = null;
    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();

    public void initialize() {
        // Configurar columnas de la tabla
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colFabricante.setCellValueFactory(new PropertyValueFactory<>("fabricante"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colSerie.setCellValueFactory(new PropertyValueFactory<>("serie"));
        colTSN.setCellValueFactory(new PropertyValueFactory<>("tsn"));
        colCSN.setCellValueFactory(new PropertyValueFactory<>("csn"));

        // Cargar datos en ComboBox
        cargarComboBoxes();

        // Cargar datos de la tabla
        cargarAircraft();

        // Estado inicial: Guardar habilitado, Actualizar/Eliminar deshabilitado
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);

        // Configurar selección de fila
        tableAircraft.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                aircraftSeleccionado = newVal;
                cargarFormulario(newVal);
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            } else {
                btnGuardar.setDisable(false);
                btnActualizar.setDisable(true);
                btnEliminar.setDisable(true);
                aircraftSeleccionado = null;
            }
        });

        // Configurar botones
        btnGuardar.setOnAction(event -> guardar());
        btnActualizar.setOnAction(event -> actualizar());
        btnEliminar.setOnAction(event -> eliminar());
        btnLimpiar.setOnAction(event -> limpiarFormulario());
        btnVolver.setOnAction(event -> volver());
    }

    private void cargarComboBoxes() {
        try {
            List<Aircraft> lista = aircraftService.findAll();

            Set<String> fabricantes = new HashSet<>();
            Set<String> modelos = new HashSet<>();
            Set<String> propietarios = new HashSet<>();
            Set<String> explotadores = new HashSet<>();

            for (Aircraft a : lista) {
                if (a.getFabricante() != null) fabricantes.add(a.getFabricante());
                if (a.getModelo() != null) modelos.add(a.getModelo());
                if (a.getPropietario() != null) propietarios.add(a.getPropietario());
                if (a.getExplotador() != null) explotadores.add(a.getExplotador());
            }

            cbFabricante.setItems(FXCollections.observableArrayList(fabricantes));
            cbModelo.setItems(FXCollections.observableArrayList(modelos));
            cbPropietario.setItems(FXCollections.observableArrayList(propietarios));
            cbExplotador.setItems(FXCollections.observableArrayList(explotadores));
        } catch (Exception e) {
            mostrarError("Error", "Error al cargar ComboBox: " + e.getMessage());
        }
    }

    private void cargarAircraft() {
        try {
            List<Aircraft> lista = aircraftService.findAll();
            aircraftList.clear();
            aircraftList.addAll(lista);
            tableAircraft.setItems(aircraftList);
            tableAircraft.refresh();
        } catch (Exception e) {
            mostrarError("Error al cargar aeronaves", e.getMessage());
        }
    }

    private void cargarFormulario(Aircraft aircraft) {
        txtMatricula.setText(aircraft.getMatricula());
        cbFabricante.setValue(aircraft.getFabricante());
        cbModelo.setValue(aircraft.getModelo());
        txtSerie.setText(aircraft.getSerie() != null ? aircraft.getSerie() : "");
        cbPropietario.setValue(aircraft.getPropietario() != null ? aircraft.getPropietario() : "");
        cbExplotador.setValue(aircraft.getExplotador() != null ? aircraft.getExplotador() : "");
        txtTSN.setText(aircraft.getTsn().toString());
        txtCSN.setText(aircraft.getCsn().toString());
    }

    @FXML
    private void guardar() {
        try {
            if (validarFormulario()) {
                // Validar unicidad de matrícula y serie
                List<Aircraft> lista = aircraftService.findAll();
                for (Aircraft a : lista) {
                    if (a.getMatricula().equalsIgnoreCase(txtMatricula.getText().trim())) {
                        mostrarError("Validación", "Ya existe una aeronave con esta matrícula");
                        return;
                    }
                    if (a.getSerie().equalsIgnoreCase(txtSerie.getText().trim())) {
                        mostrarError("Validación", "Ya existe una aeronave con esta serie");
                        return;
                    }
                }

                Aircraft aircraft = new Aircraft();
                aircraft.setMatricula(txtMatricula.getText().trim());
                aircraft.setFabricante(cbFabricante.getValue() != null ? cbFabricante.getValue().trim() : "");
                aircraft.setModelo(cbModelo.getValue() != null ? cbModelo.getValue().trim() : "");
                aircraft.setSerie(txtSerie.getText().isEmpty() ? null : txtSerie.getText().trim());
                aircraft.setPropietario(cbPropietario.getValue() != null && !cbPropietario.getValue().isEmpty() ? cbPropietario.getValue().trim() : null);
                aircraft.setExplotador(cbExplotador.getValue() != null && !cbExplotador.getValue().isEmpty() ? cbExplotador.getValue().trim() : null);
                aircraft.setTsn(new BigDecimal(txtTSN.getText().trim()));
                aircraft.setCsn(Integer.parseInt(txtCSN.getText().trim()));

                aircraftService.save(aircraft);
                mostrarInfo("Éxito", "Aeronave guardada exitosamente");
                limpiarFormulario();
                cargarComboBoxes();
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
                // Validar unicidad de matrícula y serie (excluyendo el actual)
                List<Aircraft> lista = aircraftService.findAll();
                for (Aircraft a : lista) {
                    if (a.getId() != aircraftSeleccionado.getId()) {
                        if (a.getMatricula().equalsIgnoreCase(txtMatricula.getText().trim())) {
                            mostrarError("Validación", "Ya existe otra aeronave con esta matrícula");
                            return;
                        }
                        if (a.getSerie().equalsIgnoreCase(txtSerie.getText().trim())) {
                            mostrarError("Validación", "Ya existe otra aeronave con esta serie");
                            return;
                        }
                    }
                }

                aircraftSeleccionado.setMatricula(txtMatricula.getText().trim());
                aircraftSeleccionado.setFabricante(cbFabricante.getValue() != null ? cbFabricante.getValue().trim() : "");
                aircraftSeleccionado.setModelo(cbModelo.getValue() != null ? cbModelo.getValue().trim() : "");
                aircraftSeleccionado.setSerie(txtSerie.getText().isEmpty() ? null : txtSerie.getText().trim());
                aircraftSeleccionado.setPropietario(cbPropietario.getValue() != null && !cbPropietario.getValue().isEmpty() ? cbPropietario.getValue().trim() : null);
                aircraftSeleccionado.setExplotador(cbExplotador.getValue() != null && !cbExplotador.getValue().isEmpty() ? cbExplotador.getValue().trim() : null);
                aircraftSeleccionado.setTsn(new BigDecimal(txtTSN.getText().trim()));
                aircraftSeleccionado.setCsn(Integer.parseInt(txtCSN.getText().trim()));

                aircraftService.save(aircraftSeleccionado);
                mostrarInfo("Éxito", "Aeronave actualizada exitosamente");
                limpiarFormulario();
                cargarComboBoxes();
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
                cargarComboBoxes();
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
        cbFabricante.setValue(null);
        cbModelo.setValue(null);
        txtSerie.clear();
        cbPropietario.setValue(null);
        cbExplotador.setValue(null);
        txtTSN.setText("0.00");
        txtCSN.setText("0");
        tableAircraft.getSelectionModel().clearSelection();
        aircraftSeleccionado = null;
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void volver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("ConFiMICDB - Sistema de Gestión de Aeronaves");
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        if (txtMatricula.getText().trim().isEmpty()) {
            mostrarError("Validación", "La matrícula es obligatoria");
            return false;
        }
        if (cbFabricante.getValue() == null || cbFabricante.getValue().trim().isEmpty()) {
            mostrarError("Validación", "El fabricante es obligatorio");
            return false;
        }
        if (cbModelo.getValue() == null || cbModelo.getValue().trim().isEmpty()) {
            mostrarError("Validación", "El modelo es obligatorio");
            return false;
        }
        if (txtSerie.getText().trim().isEmpty()) {
            mostrarError("Validación", "La serie es obligatoria");
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


