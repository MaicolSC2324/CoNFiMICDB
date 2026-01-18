package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.HojaLibro;
import com.java.fx.services.AircraftService;
import com.java.fx.services.HojaLibroService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class HojaLibroController {

    @FXML
    private ComboBox<String> cbMatricula;
    @FXML
    private HBox hboxNoHoja;
    @FXML
    private TextField txtNoHojaLibro;
    @FXML
    private Button btnBuscar;
    @FXML
    private VBox vboxFechaEstado;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ComboBox<String> cbEstadoHoja;

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
    private TableView<HojaLibro> tableHojaLibro;
    @FXML
    private TableColumn<HojaLibro, Integer> colNoHoja;
    @FXML
    private TableColumn<HojaLibro, LocalDate> colFecha;
    @FXML
    private TableColumn<HojaLibro, String> colEstado;

    @Autowired
    private HojaLibroService hojaLibroService;

    @Autowired
    private AircraftService aircraftService;

    @Autowired
    private ApplicationContext applicationContext;

    private HojaLibro hojaLibroSeleccionada = null;
    private ObservableList<HojaLibro> hojaLibroList = FXCollections.observableArrayList();
    private String matriculaSeleccionada = null;
    private boolean hojaExiste = false;

    public void initialize() {
        // Configurar columnas de la tabla (sin matrícula)
        colNoHoja.setCellValueFactory(new PropertyValueFactory<>("noHojaLibro"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoHoja"));

        // Inicialmente ocultar componentes
        hboxNoHoja.setVisible(false);
        hboxNoHoja.setManaged(false);
        vboxFechaEstado.setVisible(false);
        vboxFechaEstado.setManaged(false);
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
        btnLimpiar.setDisable(false);

        // Cargar matrículas en ComboBox
        cargarMatriculas();

        // Cargar estados en ComboBox
        cargarEstados();

        // Listener para ComboBox de matrícula
        cbMatricula.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                matriculaSeleccionada = newVal;
                cargarHojasLibro(newVal);
                mostrarFormularioNoHoja();
                limpiarFormularioCompleto();
            } else {
                ocultarFormularioNoHoja();
                hojaLibroList.clear();
                tableHojaLibro.setItems(hojaLibroList);
            }
        });

        // Listener para selección en tabla
        tableHojaLibro.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hojaLibroSeleccionada = newVal;
                cargarFormularioEdicion(newVal);
                hojaExiste = true;
                mostrarFechaEstado();
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            }
        });

        // Listener para búsqueda automática al cambiar el número de hoja
        txtNoHojaLibro.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && matriculaSeleccionada != null) {
                try {
                    Integer noHoja = Integer.parseInt(newVal.trim());
                    Optional<HojaLibro> hojaOpt = hojaLibroService.findByNoHojaLibro(noHoja);

                    if (hojaOpt.isPresent()) {
                        HojaLibro hoja = hojaOpt.get();
                        if (hoja.getMatriculaAc().equals(matriculaSeleccionada)) {
                            cargarFormularioEdicion(hoja);
                            hojaLibroSeleccionada = hoja;
                            hojaExiste = true;
                            mostrarFechaEstado();
                            btnGuardar.setDisable(true);
                            btnActualizar.setDisable(false);
                            btnEliminar.setDisable(false);
                            tableHojaLibro.getSelectionModel().select(hoja);
                        } else {
                            ocultarFechaEstado();
                            hojaExiste = false;
                            btnGuardar.setDisable(true);
                            btnActualizar.setDisable(true);
                            btnEliminar.setDisable(true);
                        }
                    } else {
                        ocultarFechaEstado();
                        hojaExiste = false;
                        btnGuardar.setDisable(false);
                        btnActualizar.setDisable(true);
                        btnEliminar.setDisable(true);
                        tableHojaLibro.getSelectionModel().clearSelection();
                        hojaLibroSeleccionada = null;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar si no es un número válido
                }
            } else if (newVal.trim().isEmpty()) {
                ocultarFechaEstado();
                dpFecha.setValue(null);
                cbEstadoHoja.setValue(null);
                tableHojaLibro.getSelectionModel().clearSelection();
                hojaLibroSeleccionada = null;
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(true);
                btnEliminar.setDisable(true);
            }
        });

        // Configurar botones
        btnGuardar.setOnAction(event -> guardar());
        btnActualizar.setOnAction(event -> actualizar());
        btnEliminar.setOnAction(event -> eliminar());
        btnBuscar.setOnAction(event -> buscar());
        btnLimpiar.setOnAction(event -> limpiarFormularioFecha());
        btnVolver.setOnAction(event -> volver());
    }

    private void mostrarFormularioNoHoja() {
        hboxNoHoja.setVisible(true);
        hboxNoHoja.setManaged(true);
    }

    private void ocultarFormularioNoHoja() {
        hboxNoHoja.setVisible(false);
        hboxNoHoja.setManaged(false);
    }

    private void mostrarFechaEstado() {
        vboxFechaEstado.setVisible(true);
        vboxFechaEstado.setManaged(true);
    }

    private void ocultarFechaEstado() {
        vboxFechaEstado.setVisible(false);
        vboxFechaEstado.setManaged(false);
    }

    private void cargarMatriculas() {
        try {
            List<Aircraft> lista = aircraftService.findAll();
            ObservableList<String> matriculas = FXCollections.observableArrayList();
            for (Aircraft a : lista) {
                matriculas.add(a.getMatricula());
            }
            cbMatricula.setItems(matriculas);
        } catch (Exception e) {
            mostrarError("Error", "Error al cargar matrículas: " + e.getMessage());
        }
    }

    private void cargarEstados() {
        ObservableList<String> estados = FXCollections.observableArrayList(
                "ARCHIVADA",
                "CON_NOVEDAD",
                "SIN_ENTREGAR"
        );
        cbEstadoHoja.setItems(estados);
    }

    private void cargarHojasLibro(String matricula) {
        try {
            List<HojaLibro> lista = hojaLibroService.findByMatriculaAc(matricula);
            hojaLibroList.clear();
            hojaLibroList.addAll(lista);
            tableHojaLibro.setItems(hojaLibroList);
            tableHojaLibro.refresh();
        } catch (Exception e) {
            mostrarError("Error al cargar hojas", e.getMessage());
        }
    }

    private void cargarFormularioEdicion(HojaLibro hojaLibro) {
        txtNoHojaLibro.setText(hojaLibro.getNoHojaLibro().toString());
        dpFecha.setValue(hojaLibro.getFecha());
        cbEstadoHoja.setValue(hojaLibro.getEstadoHoja());
    }

    @FXML
    private void buscar() {
        try {
            if (txtNoHojaLibro.getText().trim().isEmpty()) {
                mostrarError("Validación", "Debe ingresar un número de hoja");
                return;
            }

            Integer noHoja = Integer.parseInt(txtNoHojaLibro.getText().trim());
            Optional<HojaLibro> hojaOpt = hojaLibroService.findByNoHojaLibro(noHoja);

            if (hojaOpt.isPresent()) {
                HojaLibro hoja = hojaOpt.get();
                if (hoja.getMatriculaAc().equals(matriculaSeleccionada)) {
                    cargarFormularioEdicion(hoja);
                    hojaLibroSeleccionada = hoja;
                    hojaExiste = true;
                    mostrarFechaEstado();
                    btnGuardar.setDisable(true);
                    btnActualizar.setDisable(false);
                    btnEliminar.setDisable(false);
                    tableHojaLibro.getSelectionModel().select(hoja);
                } else {
                    mostrarError("Error", "La hoja no pertenece a la aeronave seleccionada");
                }
            } else {
                hojaExiste = false;
                mostrarPreguntaAdicionar();
            }
        } catch (NumberFormatException e) {
            mostrarError("Validación", "El número de hoja debe ser un número entero válido");
        } catch (Exception e) {
            mostrarError("Error al buscar", e.getMessage());
        }
    }

    private void mostrarPreguntaAdicionar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hoja no encontrada");
        alert.setHeaderText("La hoja no ha sido creada");
        alert.setContentText("¿Desea adicionar esta hoja?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            hojaExiste = false;
            mostrarFechaEstado();
            btnGuardar.setDisable(false);
            btnActualizar.setDisable(true);
            btnEliminar.setDisable(true);
            tableHojaLibro.getSelectionModel().clearSelection();
            hojaLibroSeleccionada = null;
            ocultarFechaEstado();
            dpFecha.setValue(null);
            cbEstadoHoja.setValue(null);
            mostrarFechaEstado();
        } else {
            limpiarFormularioFecha();
        }
    }

    @FXML
    private void guardar() {
        try {
            if (validarFormulario()) {
                // Validar que no exista el número de hoja
                Optional<HojaLibro> existente = hojaLibroService.findByNoHojaLibro(Integer.parseInt(txtNoHojaLibro.getText().trim()));
                if (existente.isPresent()) {
                    mostrarError("Validación", "Ya existe una hoja con este número");
                    return;
                }

                HojaLibro hojaLibro = new HojaLibro();
                hojaLibro.setMatriculaAc(matriculaSeleccionada);
                hojaLibro.setNoHojaLibro(Integer.parseInt(txtNoHojaLibro.getText().trim()));
                hojaLibro.setFecha(dpFecha.getValue());
                hojaLibro.setEstadoHoja(cbEstadoHoja.getValue());

                hojaLibroService.save(hojaLibro);
                mostrarInfo("Éxito", "Hoja del libro guardada exitosamente");
                cargarHojasLibro(matriculaSeleccionada);
                limpiarFormularioFecha();
            }
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        try {
            if (hojaLibroSeleccionada == null) {
                mostrarError("Error", "Debe seleccionar una hoja");
                return;
            }

            if (validarFormulario()) {
                hojaLibroSeleccionada.setFecha(dpFecha.getValue());
                hojaLibroSeleccionada.setEstadoHoja(cbEstadoHoja.getValue());

                hojaLibroService.save(hojaLibroSeleccionada);
                mostrarInfo("Éxito", "Hoja del libro actualizada exitosamente");
                cargarHojasLibro(matriculaSeleccionada);
                limpiarFormularioFecha();
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar", e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        try {
            if (hojaLibroSeleccionada == null) {
                mostrarError("Error", "Debe seleccionar una hoja");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Desea eliminar esta hoja del libro?");
            alert.setContentText("No. Hoja: " + hojaLibroSeleccionada.getNoHojaLibro());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                hojaLibroService.delete(hojaLibroSeleccionada);
                mostrarInfo("Éxito", "Hoja del libro eliminada exitosamente");
                cargarHojasLibro(matriculaSeleccionada);
                limpiarFormularioFecha();
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar", e.getMessage());
        }
    }

    @FXML
    private void limpiarFormularioFecha() {
        dpFecha.setValue(null);
        cbEstadoHoja.setValue(null);
        txtNoHojaLibro.clear();
        tableHojaLibro.getSelectionModel().clearSelection();
        hojaLibroSeleccionada = null;
        ocultarFechaEstado();
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private void limpiarFormularioCompleto() {
        txtNoHojaLibro.clear();
        dpFecha.setValue(null);
        cbEstadoHoja.setValue(null);
        tableHojaLibro.getSelectionModel().clearSelection();
        hojaLibroSeleccionada = null;
        ocultarFechaEstado();
        btnGuardar.setDisable(true);
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
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        if (txtNoHojaLibro.getText().trim().isEmpty()) {
            mostrarError("Validación", "El número de hoja es obligatorio");
            return false;
        }

        try {
            Integer.parseInt(txtNoHojaLibro.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Validación", "El número de hoja debe ser un número entero válido");
            return false;
        }

        if (dpFecha.getValue() == null) {
            mostrarError("Validación", "La fecha es obligatoria");
            return false;
        }

        if (cbEstadoHoja.getValue() == null || cbEstadoHoja.getValue().isEmpty()) {
            mostrarError("Validación", "El estado es obligatorio");
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



