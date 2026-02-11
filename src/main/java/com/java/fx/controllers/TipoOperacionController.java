package com.java.fx.controllers;

import com.java.fx.models.TipoOperacion;
import com.java.fx.services.TipoOperacionService;
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
import java.util.List;
import java.util.Optional;

@Controller
public class TipoOperacionController {

    @FXML
    private TextField txtDescripcion;

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
    private TableView<TipoOperacion> tablaTipoOperacion;

    @FXML
    private TableColumn<TipoOperacion, Integer> colId;

    @FXML
    private TableColumn<TipoOperacion, String> colDescripcion;

    @FXML
    private Label lblMensaje;

    @Autowired
    private TipoOperacionService tipoOperacionService;

    @Autowired
    private ApplicationContext applicationContext;

    private ObservableList<TipoOperacion> tipoOperacionList = FXCollections.observableArrayList();
    private TipoOperacion tipoOperacionSeleccionado = null;

    public void initialize() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Cargar datos
        cargarTiposOperacion();

        // Configurar selección de fila
        tablaTipoOperacion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tipoOperacionSeleccionado = newVal;
                cargarFormulario(newVal);
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            } else {
                btnGuardar.setDisable(false);
                btnActualizar.setDisable(true);
                btnEliminar.setDisable(true);
                tipoOperacionSeleccionado = null;
            }
        });

        // Validar entrada
        txtDescripcion.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                btnGuardar.setDisable(false);
            }
        });

        // Configurar botones
        btnGuardar.setOnAction(event -> guardar());
        btnActualizar.setOnAction(event -> actualizar());
        btnEliminar.setOnAction(event -> eliminar());
        btnLimpiar.setOnAction(event -> limpiar());
        btnVolver.setOnAction(event -> volver());
    }

    private void cargarTiposOperacion() {
        try {
            List<TipoOperacion> tipos = tipoOperacionService.findAll();
            tipoOperacionList.clear();
            tipoOperacionList.addAll(tipos);
            tablaTipoOperacion.setItems(tipoOperacionList);
        } catch (Exception e) {
            mostrarError("Error", "Error al cargar tipos de operación: " + e.getMessage());
        }
    }

    private void cargarFormulario(TipoOperacion tipo) {
        txtDescripcion.setText(tipo.getDescripcion());
    }

    @FXML
    private void guardar() {
        try {
            String descripcion = txtDescripcion.getText().trim();

            if (descripcion.isEmpty()) {
                mostrarError("Validación", "Debe ingresar una descripción");
                return;
            }

            TipoOperacion tipoOperacion = new TipoOperacion();
            tipoOperacion.setDescripcion(descripcion);
            tipoOperacionService.save(tipoOperacion);

            mostrarInfo("Éxito", "Tipo de operación guardado correctamente");
            limpiar();
            cargarTiposOperacion();
        } catch (Exception e) {
            mostrarError("Error", "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        try {
            if (tipoOperacionSeleccionado == null) {
                mostrarError("Error", "Debe seleccionar un tipo de operación");
                return;
            }

            String descripcion = txtDescripcion.getText().trim();

            if (descripcion.isEmpty()) {
                mostrarError("Validación", "Debe ingresar una descripción");
                return;
            }

            tipoOperacionSeleccionado.setDescripcion(descripcion);
            tipoOperacionService.save(tipoOperacionSeleccionado);

            mostrarInfo("Éxito", "Tipo de operación actualizado correctamente");
            limpiar();
            cargarTiposOperacion();
        } catch (Exception e) {
            mostrarError("Error", "Error al actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        try {
            if (tipoOperacionSeleccionado == null) {
                mostrarError("Error", "Debe seleccionar un tipo de operación");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Desea eliminar este tipo de operación?");
            alert.setContentText("Descripción: " + tipoOperacionSeleccionado.getDescripcion());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                tipoOperacionService.delete(tipoOperacionSeleccionado);
                mostrarInfo("Éxito", "Tipo de operación eliminado correctamente");
                limpiar();
                cargarTiposOperacion();
            }
        } catch (Exception e) {
            mostrarError("Error", "Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        txtDescripcion.clear();
        tablaTipoOperacion.getSelectionModel().clearSelection();
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
        tipoOperacionSeleccionado = null;
        lblMensaje.setText("");
    }

    @FXML
    private void volver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulariosView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Formularios");
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
        lblMensaje.setText(mensaje);
    }
}
