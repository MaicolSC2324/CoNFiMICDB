package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.HojaLibro;
import com.java.fx.models.PiernaVuelo;
import com.java.fx.services.AircraftService;
import com.java.fx.services.HojaLibroService;
import com.java.fx.services.PiernaVueloService;
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
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
public class HojaLibroController {

    @FXML
    private TabPane tabPaneHojaLibro;
    @FXML
    private Tab tabHojas;
    @FXML
    private Tab tabPiernas;
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
    @FXML
    private TableColumn<HojaLibro, Long> colTotalPiernas;
    @FXML
    private TableColumn<HojaLibro, Double> colTiempoTotal;

    // Campos para piernas de vuelo
    @FXML
    private TextField txtHojaSeleccionada;
    @FXML
    private TextField txtNoPierna;
    @FXML
    private ComboBox<String> cbOrigen;
    @FXML
    private ComboBox<String> cbDestino;
    @FXML
    private TextField txtDespegue;
    @FXML
    private TextField txtAterrizaje;
    @FXML
    private TextField txtTiempoVuelo;
    @FXML
    private TextField txtCiclos;

    @FXML
    private Button btnGuardarPierna;
    @FXML
    private Button btnActualizarPierna;
    @FXML
    private Button btnEliminarPierna;
    @FXML
    private Button btnLimpiarPierna;

    @FXML
    private TableView<PiernaVuelo> tablePiernas;
    @FXML
    private TableColumn<PiernaVuelo, String> colIdPierna;
    @FXML
    private TableColumn<PiernaVuelo, Integer> colNoPiernaTabla;
    @FXML
    private TableColumn<PiernaVuelo, String> colOrigenTabla;
    @FXML
    private TableColumn<PiernaVuelo, String> colDestinoTabla;
    @FXML
    private TableColumn<PiernaVuelo, LocalTime> colDespegueTabla;
    @FXML
    private TableColumn<PiernaVuelo, LocalTime> colAterrizajeTabla;
    @FXML
    private TableColumn<PiernaVuelo, BigDecimal> colTiempoVueloTabla;
    @FXML
    private TableColumn<PiernaVuelo, Integer> colCiclosTabla;

    @Autowired
    private HojaLibroService hojaLibroService;

    @Autowired
    private AircraftService aircraftService;

    @Autowired
    private PiernaVueloService piernaVueloService;

    @Autowired
    private ApplicationContext applicationContext;

    private HojaLibro hojaLibroSeleccionada = null;
    private PiernaVuelo piernaSeleccionada = null;
    private ObservableList<HojaLibro> hojaLibroList = FXCollections.observableArrayList();
    private ObservableList<PiernaVuelo> piernaList = FXCollections.observableArrayList();
    private String matriculaSeleccionada = null;
    private Integer noHojaSeleccionada = null;
    private boolean hojaExiste = false;

    public void initialize() {
        // Configurar columnas de la tabla hojas (sin matrícula)
        colNoHoja.setCellValueFactory(new PropertyValueFactory<>("noHojaLibro"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoHoja"));

        // Configurar columnas de estadísticas
        colTotalPiernas.setCellValueFactory(cellData -> {
            Integer noHoja = cellData.getValue().getNoHojaLibro();
            Integer total = piernaVueloService.contarPiernasporHoja(noHoja);
            return new javafx.beans.property.SimpleObjectProperty<>(total.longValue());
        });

        colTiempoTotal.setCellValueFactory(cellData -> {
            Integer noHoja = cellData.getValue().getNoHojaLibro();
            Double totalTiempo = piernaVueloService.sumTiempoVueloPorHoja(noHoja);
            return new javafx.beans.property.SimpleObjectProperty<>(totalTiempo);
        });

        // Configurar columnas de piernas
        colIdPierna.setCellValueFactory(new PropertyValueFactory<>("idPierna"));
        colNoPiernaTabla.setCellValueFactory(new PropertyValueFactory<>("noPierna"));
        colOrigenTabla.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colDestinoTabla.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colDespegueTabla.setCellValueFactory(new PropertyValueFactory<>("despegue"));
        colAterrizajeTabla.setCellValueFactory(new PropertyValueFactory<>("aterrizaje"));
        colTiempoVueloTabla.setCellValueFactory(new PropertyValueFactory<>("tiempoVuelo"));
        colCiclosTabla.setCellValueFactory(new PropertyValueFactory<>("ciclos"));

        // Inicialmente ocultar componentes
        hboxNoHoja.setVisible(false);
        hboxNoHoja.setManaged(false);
        vboxFechaEstado.setVisible(false);
        vboxFechaEstado.setManaged(false);
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
        btnLimpiar.setDisable(false);

        // Inicializar botones piernas
        btnGuardarPierna.setDisable(true);
        btnActualizarPierna.setDisable(true);
        btnEliminarPierna.setDisable(true);

        // Deshabilitar pestaña de piernas inicialmente
        tabPiernas.setDisable(true);

        // Cargar matrículas en ComboBox
        cargarMatriculas();

        // Cargar estados en ComboBox
        cargarEstados();

        // Cargar orígenes y destinos
        cargarOrigenesDestinos();

        // Listener para cambio de pestañas
        tabPaneHojaLibro.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == tabPiernas) {
                // Bloquear campos de hojas al entrar a piernas
                cbMatricula.setDisable(true);
                txtNoHojaLibro.setDisable(true);
                btnBuscar.setDisable(true);
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(true);
                btnEliminar.setDisable(true);
                btnLimpiar.setDisable(false);
            } else if (newVal == tabHojas) {
                // Refrescar tabla de hojas cuando se vuelve a la pestaña
                if (matriculaSeleccionada != null) {
                    cargarHojasLibro(matriculaSeleccionada);
                    tableHojaLibro.refresh();
                }
                // Desbloquear campos de hojas al volver
                cbMatricula.setDisable(false);
                txtNoHojaLibro.setDisable(false);
                btnBuscar.setDisable(false);
                btnLimpiar.setDisable(false);
                if (noHojaSeleccionada != null) {
                    btnActualizar.setDisable(false);
                    btnEliminar.setDisable(false);
                }
            }
        });

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

        // Listener para selección en tabla hojas
        tableHojaLibro.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hojaLibroSeleccionada = newVal;
                noHojaSeleccionada = newVal.getNoHojaLibro();
                cargarFormularioEdicion(newVal);
                hojaExiste = true;
                mostrarFechaEstado();
                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
                // Habilitar pestaña de piernas
                tabPiernas.setDisable(false);
            }
        });

        // Listener para búsqueda automática de hojas
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
                            noHojaSeleccionada = hoja.getNoHojaLibro();
                            hojaExiste = true;
                            mostrarFechaEstado();
                            btnGuardar.setDisable(true);
                            btnActualizar.setDisable(false);
                            btnEliminar.setDisable(false);
                            // Habilitar pestaña de piernas
                            tabPiernas.setDisable(false);
                            tableHojaLibro.getSelectionModel().select(hoja);
                        } else {
                            ocultarFechaEstado();
                            hojaExiste = false;
                            btnGuardar.setDisable(true);
                            btnActualizar.setDisable(true);
                            btnEliminar.setDisable(true);
                            // Deshabilitar pestaña de piernas
                            tabPiernas.setDisable(true);
                        }
                    } else {
                        ocultarFechaEstado();
                        hojaExiste = false;
                        btnGuardar.setDisable(false);
                        btnActualizar.setDisable(true);
                        btnEliminar.setDisable(true);
                        // Deshabilitar pestaña de piernas
                        tabPiernas.setDisable(true);
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
                // Deshabilitar pestaña de piernas
                tabPiernas.setDisable(true);
            }
        });

        // Listener para selección de piernas
        tablePiernas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                piernaSeleccionada = newVal;
                cargarFormularioPiernaEdicion(newVal);
                btnGuardarPierna.setDisable(true);
                btnActualizarPierna.setDisable(false);
                btnEliminarPierna.setDisable(false);
            }
        });

        // Listener para cálculo automático de tiempo de vuelo
        txtDespegue.textProperty().addListener((obs, oldVal, newVal) -> {
            calcularTiempoVuelo();
            habilitarBotonGuardarPierna();
        });
        txtAterrizaje.textProperty().addListener((obs, oldVal, newVal) -> {
            calcularTiempoVuelo();
            habilitarBotonGuardarPierna();
        });

        // Listeners para campos de pierna
        txtNoPierna.textProperty().addListener((obs, oldVal, newVal) -> habilitarBotonGuardarPierna());
        cbOrigen.valueProperty().addListener((obs, oldVal, newVal) -> habilitarBotonGuardarPierna());
        cbDestino.valueProperty().addListener((obs, oldVal, newVal) -> habilitarBotonGuardarPierna());
        txtCiclos.textProperty().addListener((obs, oldVal, newVal) -> habilitarBotonGuardarPierna());

        // Configurar botones hojas
        btnGuardar.setOnAction(event -> guardar());
        btnActualizar.setOnAction(event -> actualizar());
        btnEliminar.setOnAction(event -> eliminar());
        btnBuscar.setOnAction(event -> buscar());
        btnLimpiar.setOnAction(event -> limpiarFormularioFecha());

        // Configurar botones piernas
        btnGuardarPierna.setOnAction(event -> guardarPierna());
        btnActualizarPierna.setOnAction(event -> actualizarPierna());
        btnEliminarPierna.setOnAction(event -> eliminarPierna());
        btnLimpiarPierna.setOnAction(event -> limpiarFormularioPierna());

        btnVolver.setOnAction(event -> volver());
    }

    private void cargarOrigenesDestinos() {
        try {
            List<String> origenes = piernaVueloService.findDistinctOrigenes();
            cbOrigen.setItems(FXCollections.observableArrayList(origenes));

            List<String> destinos = piernaVueloService.findDistinctDestinos();
            cbDestino.setItems(FXCollections.observableArrayList(destinos));
        } catch (Exception e) {
            // Ignorar si no hay datos
        }
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
        noHojaSeleccionada = hojaLibro.getNoHojaLibro();
        txtHojaSeleccionada.setText(noHojaSeleccionada.toString());
        cargarPiernasVuelo(noHojaSeleccionada);
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
                noHojaSeleccionada = hojaLibro.getNoHojaLibro();
                mostrarInfo("Éxito", "Hoja del libro guardada exitosamente");
                cargarHojasLibro(matriculaSeleccionada);
                cargarPiernasVuelo(noHojaSeleccionada);
                txtHojaSeleccionada.setText(noHojaSeleccionada.toString());
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
                cargarPiernasVuelo(noHojaSeleccionada);
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
                piernaList.clear();
                tablePiernas.setItems(piernaList);
                limpiarFormularioFecha();
                limpiarFormularioPierna();
                noHojaSeleccionada = null;
                txtHojaSeleccionada.clear();
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
        // Deshabilitar pestaña de piernas
        tabPiernas.setDisable(true);
    }

    private void limpiarFormularioCompleto() {
        txtNoHojaLibro.clear();
        dpFecha.setValue(null);
        cbEstadoHoja.setValue(null);
        tableHojaLibro.getSelectionModel().clearSelection();
        hojaLibroSeleccionada = null;
        noHojaSeleccionada = null;
        txtHojaSeleccionada.clear();
        ocultarFechaEstado();
        limpiarFormularioPierna();
        piernaList.clear();
        tablePiernas.setItems(piernaList);
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
        // Deshabilitar pestaña de piernas
        tabPiernas.setDisable(true);
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

    private void habilitarBotonGuardarPierna() {
        boolean tieneNoPierna = !txtNoPierna.getText().trim().isEmpty();
        boolean tieneOrigen = cbOrigen.getValue() != null && !cbOrigen.getValue().isEmpty();
        boolean tieneDestino = cbDestino.getValue() != null && !cbDestino.getValue().isEmpty();
        boolean tieneDespegue = !txtDespegue.getText().trim().isEmpty();
        boolean tieneAterrizaje = !txtAterrizaje.getText().trim().isEmpty();
        boolean tieneCiclos = !txtCiclos.getText().trim().isEmpty();
        boolean tienetiempoVuelo = !txtTiempoVuelo.getText().trim().isEmpty();

        boolean todosLosDatos = tieneNoPierna && tieneOrigen && tieneDestino && tieneDespegue && tieneAterrizaje && tieneCiclos && tienetiempoVuelo;

        // Si no hay pierna seleccionada, es una inserción nueva
        if (piernaSeleccionada == null) {
            btnGuardarPierna.setDisable(!todosLosDatos);
            btnActualizarPierna.setDisable(true);
        } else {
            // Si hay pierna seleccionada, es una actualización
            btnGuardarPierna.setDisable(true);
            btnActualizarPierna.setDisable(!todosLosDatos);
        }
    }

    private void calcularTiempoVuelo() {
        try {
            if (!txtDespegue.getText().trim().isEmpty() && !txtAterrizaje.getText().trim().isEmpty()) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                LocalTime despegue = LocalTime.parse(txtDespegue.getText().trim(), formatter);
                LocalTime aterrizaje = LocalTime.parse(txtAterrizaje.getText().trim(), formatter);

                long minutosVuelo = ChronoUnit.MINUTES.between(despegue, aterrizaje);
                if (minutosVuelo < 0) {
                    minutosVuelo += 24 * 60;
                }

                // Convertir minutos a decimal (horas.decimales)
                BigDecimal tiempoVuelo = BigDecimal.valueOf(minutosVuelo).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                txtTiempoVuelo.setText(tiempoVuelo.toString());
            }
        } catch (Exception e) {
            // Ignorar si el formato de hora es inválido
        }
    }

    private void cargarFormularioPiernaEdicion(PiernaVuelo pierna) {
        txtNoPierna.setText(pierna.getNoPierna().toString());
        cbOrigen.setValue(pierna.getOrigen());
        cbDestino.setValue(pierna.getDestino());
        txtDespegue.setText(pierna.getDespegue().toString());
        txtAterrizaje.setText(pierna.getAterrizaje().toString());
        txtTiempoVuelo.setText(pierna.getTiempoVuelo().toString());
        txtCiclos.setText(pierna.getCiclos().toString());
        // Habilitar botón de actualizar cuando se cargue una pierna
        habilitarBotonGuardarPierna();
    }

    @FXML
    private void guardarPierna() {
        try {
            if (noHojaSeleccionada == null) {
                mostrarError("Validación", "Debe seleccionar una hoja primero");
                return;
            }

            if (txtNoPierna.getText().trim().isEmpty()) {
                mostrarError("Validación", "Debe ingresar el número de pierna");
                return;
            }

            if (!validarFormularioPierna()) {
                return;
            }

            Integer noPierna = Integer.parseInt(txtNoPierna.getText().trim());
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            LocalTime despegue = LocalTime.parse(txtDespegue.getText().trim(), formatter);
            LocalTime aterrizaje = LocalTime.parse(txtAterrizaje.getText().trim(), formatter);
            Integer ciclos = Integer.parseInt(txtCiclos.getText().trim());

            PiernaVuelo pierna = new PiernaVuelo(
                    noHojaSeleccionada,
                    noPierna,
                    cbOrigen.getValue(),
                    cbDestino.getValue(),
                    despegue,
                    aterrizaje,
                    ciclos
            );

            // El tiempo de vuelo se calcula automáticamente en el constructor
            piernaVueloService.save(pierna);
            mostrarInfo("Éxito", "Pierna de vuelo guardada exitosamente");
            cargarPiernasVuelo(noHojaSeleccionada);
            limpiarFormularioPierna();
        } catch (NumberFormatException e) {
            mostrarError("Error de validación", "El número de pierna debe ser un número entero");
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void actualizarPierna() {
        try {
            if (piernaSeleccionada == null) {
                mostrarError("Error", "Debe seleccionar una pierna");
                return;
            }

            if (!validarFormularioPierna()) {
                return;
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            piernaSeleccionada.setOrigen(cbOrigen.getValue());
            piernaSeleccionada.setDestino(cbDestino.getValue());
            piernaSeleccionada.setDespegue(LocalTime.parse(txtDespegue.getText().trim(), formatter));
            piernaSeleccionada.setAterrizaje(LocalTime.parse(txtAterrizaje.getText().trim(), formatter));
            piernaSeleccionada.setCiclos(Integer.parseInt(txtCiclos.getText().trim()));

            piernaVueloService.save(piernaSeleccionada);
            mostrarInfo("Éxito", "Pierna de vuelo actualizada exitosamente");
            cargarPiernasVuelo(noHojaSeleccionada);
            limpiarFormularioPierna();
        } catch (Exception e) {
            mostrarError("Error al actualizar", e.getMessage());
        }
    }

    @FXML
    private void eliminarPierna() {
        try {
            if (piernaSeleccionada == null) {
                mostrarError("Error", "Debe seleccionar una pierna");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Desea eliminar esta pierna de vuelo?");
            alert.setContentText("ID Pierna: " + piernaSeleccionada.getIdPierna());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                piernaVueloService.delete(piernaSeleccionada);
                mostrarInfo("Éxito", "Pierna de vuelo eliminada exitosamente");
                cargarPiernasVuelo(noHojaSeleccionada);
                limpiarFormularioPierna();
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar", e.getMessage());
        }
    }

    @FXML
    private void limpiarFormularioPierna() {
        txtNoPierna.clear();
        cbOrigen.setValue(null);
        cbDestino.setValue(null);
        txtDespegue.clear();
        txtAterrizaje.clear();
        txtTiempoVuelo.clear();
        txtCiclos.clear();
        tablePiernas.getSelectionModel().clearSelection();
        piernaSeleccionada = null;
        btnGuardarPierna.setDisable(true);
        btnActualizarPierna.setDisable(true);
        btnEliminarPierna.setDisable(true);
    }

    private void cargarPiernasVuelo(Integer noHojaLibro) {
        try {
            List<PiernaVuelo> lista = piernaVueloService.findByNoHojaLibro(noHojaLibro);
            piernaList.clear();
            piernaList.addAll(lista);
            tablePiernas.setItems(piernaList);
            tablePiernas.refresh();
        } catch (Exception e) {
            mostrarError("Error al cargar piernas", e.getMessage());
        }
    }

    private boolean validarFormularioPierna() {
        if (cbOrigen.getValue() == null || cbOrigen.getValue().isEmpty()) {
            mostrarError("Validación", "El origen es obligatorio");
            return false;
        }

        if (cbDestino.getValue() == null || cbDestino.getValue().isEmpty()) {
            mostrarError("Validación", "El destino es obligatorio");
            return false;
        }

        if (txtDespegue.getText().trim().isEmpty()) {
            mostrarError("Validación", "La hora de despegue es obligatoria");
            return false;
        }

        if (txtAterrizaje.getText().trim().isEmpty()) {
            mostrarError("Validación", "La hora de aterrizaje es obligatoria");
            return false;
        }

        if (txtCiclos.getText().trim().isEmpty()) {
            mostrarError("Validación", "Los ciclos son obligatorios");
            return false;
        }

        try {
            LocalTime.parse(txtDespegue.getText().trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(txtAterrizaje.getText().trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            Integer.parseInt(txtCiclos.getText().trim());
        } catch (Exception e) {
            mostrarError("Validación", "Formato inválido. Use HH:mm para horas");
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



