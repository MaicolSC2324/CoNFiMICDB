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
    private TableColumn<Aircraft, String> colTSN;
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
        // Convertir TSN a formato [h]:mm para mostrar en tabla
        colTSN.setCellValueFactory(cellData -> {
            String tsnFormato = convertirDecimalAFormato(cellData.getValue().getTsn());
            return new javafx.beans.property.SimpleStringProperty(tsnFormato);
        });
        colCSN.setCellValueFactory(new PropertyValueFactory<>("csn"));

        // Cargar datos en ComboBox
        cargarComboBoxes();

        // Cargar datos de la tabla
        cargarAircraft();

        // Estado inicial: Guardar habilitado, Actualizar/Eliminar deshabilitado
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);

        // Listener para búsqueda automática de matrículas
        txtMatricula.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                try {
                    Optional<Aircraft> aircraftOpt = aircraftService.findByMatricula(newVal.trim());

                    if (aircraftOpt.isPresent()) {
                        // Si existe, cargar los datos y habilitar actualizar y eliminar
                        aircraftSeleccionado = aircraftOpt.get();
                        cargarFormulario(aircraftSeleccionado);
                        btnGuardar.setDisable(true);
                        btnActualizar.setDisable(false);
                        btnEliminar.setDisable(false);
                        tableAircraft.getSelectionModel().select(aircraftSeleccionado);
                    } else {
                        // Si no existe, limpiar campos excepto matrícula y habilitar guardar
                        aircraftSeleccionado = null;
                        limpiarCamposExceptoMatricula();
                        btnGuardar.setDisable(false);
                        btnActualizar.setDisable(true);
                        btnEliminar.setDisable(true);
                        tableAircraft.getSelectionModel().clearSelection();
                    }
                } catch (Exception e) {
                    aircraftSeleccionado = null;
                    limpiarCamposExceptoMatricula();
                    btnGuardar.setDisable(false);
                    btnActualizar.setDisable(true);
                    btnEliminar.setDisable(true);
                }
            } else {
                // Si está vacío, limpiar todo
                aircraftSeleccionado = null;
                limpiarFormulario();
            }
        });

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
        // Convertir TSN de decimal a formato [h]:mm
        txtTSN.setText(convertirDecimalAFormato(aircraft.getTsn()));
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

                // Convertir TSN de formato [h]:mm a decimal
                BigDecimal tsnDecimal = convertirFormatoADecimal(txtTSN.getText().trim());
                if (tsnDecimal == null) {
                    mostrarError("Formato inválido", "TSN debe estar en formato [h]:mm (ej: 5999:06)");
                    return;
                }
                aircraft.setTsn(tsnDecimal);
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

                // Convertir TSN de formato [h]:mm a decimal
                BigDecimal tsnDecimal = convertirFormatoADecimal(txtTSN.getText().trim());
                if (tsnDecimal == null) {
                    mostrarError("Formato inválido", "TSN debe estar en formato [h]:mm (ej: 5999:06)");
                    return;
                }
                aircraftSeleccionado.setTsn(tsnDecimal);
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
        txtTSN.setText("0:00");
        txtCSN.setText("0");
        tableAircraft.getSelectionModel().clearSelection();
        aircraftSeleccionado = null;
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private void limpiarCamposExceptoMatricula() {
        cbFabricante.setValue(null);
        cbModelo.setValue(null);
        txtSerie.clear();
        cbPropietario.setValue(null);
        cbExplotador.setValue(null);
        txtTSN.setText("0:00");
        txtCSN.setText("0");
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

        // Validar TSN en formato [h]:mm
        String tsnText = txtTSN.getText().trim();
        if (!validarFormatoHoras(tsnText)) {
            mostrarError("Validación", "TSN debe estar en formato [h]:mm (ej: 100:30)");
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

    private boolean validarFormatoHoras(String formato) {
        if (formato == null || formato.trim().isEmpty()) {
            return false;
        }

        try {
            String limpio = formato.trim();

            // DEBUG: Mostrar lo que se está validando
            System.out.println("DEBUG validarFormatoHoras: '" + limpio + "'");

            // Buscar la posición del ":"
            int indiceDosPuntos = limpio.indexOf(":");
            System.out.println("DEBUG indiceDosPuntos: " + indiceDosPuntos);

            if (indiceDosPuntos == -1) {
                System.out.println("DEBUG: No contiene ':'");
                return false; // No tiene ":"
            }

            // Extraer las partes antes y después del ":"
            String horasStr = limpio.substring(0, indiceDosPuntos).trim();
            String minutosStr = limpio.substring(indiceDosPuntos + 1).trim();

            System.out.println("DEBUG horasStr: '" + horasStr + "' minutosStr: '" + minutosStr + "'");

            // Validar que no estén vacías
            if (horasStr.isEmpty() || minutosStr.isEmpty()) {
                System.out.println("DEBUG: Horas o minutos vacíos");
                return false;
            }

            // Convertir a números
            long horas = Long.parseLong(horasStr);
            long minutos = Long.parseLong(minutosStr);

            System.out.println("DEBUG horas: " + horas + " minutos: " + minutos);

            // Validar que horas sea positivo o cero
            if (horas < 0) {
                System.out.println("DEBUG: Horas negativas");
                return false;
            }

            // Validar que los minutos estén entre 0 y 59
            if (minutos < 0 || minutos > 59) {
                System.out.println("DEBUG: Minutos fuera de rango (0-59), valor: " + minutos);
                return false;
            }

            System.out.println("DEBUG: Validación exitosa");
            return true;
        } catch (NumberFormatException e) {
            System.out.println("DEBUG NumberFormatException: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("DEBUG Exception: " + e.getMessage());
            return false;
        }
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

    // Convertir decimal (ej: 100.5) a formato [h]:mm (ej: 100:30)
    private String convertirDecimalAFormato(BigDecimal decimal) {
        if (decimal == null) return "0:00";

        double horas = decimal.doubleValue();
        long horasCompletas = (long) horas;
        long minutos = Math.round((horas - horasCompletas) * 60);

        return String.format(java.util.Locale.US, "%d:%02d", horasCompletas, minutos);
    }

    // Convertir formato [h]:mm (ej: 5999:06) a decimal (ej: 5999.1)
    private BigDecimal convertirFormatoADecimal(String formato) {
        if (formato == null || formato.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            String limpio = formato.trim();

            System.out.println("DEBUG convertirFormatoADecimal: '" + limpio + "'");

            // Buscar la posición del ":"
            int indiceDosPuntos = limpio.indexOf(":");
            if (indiceDosPuntos == -1) {
                System.out.println("DEBUG: No contiene ':'");
                return null; // No tiene ":", error
            }

            // Extraer las partes
            String horasStr = limpio.substring(0, indiceDosPuntos).trim();
            String minutosStr = limpio.substring(indiceDosPuntos + 1).trim();

            System.out.println("DEBUG horasStr: '" + horasStr + "' minutosStr: '" + minutosStr + "'");

            long horas = Long.parseLong(horasStr);
            long minutos = Long.parseLong(minutosStr);

            System.out.println("DEBUG horas: " + horas + " minutos: " + minutos);

            // Validar que los minutos estén entre 0 y 59
            if (minutos < 0 || minutos > 59) {
                System.out.println("DEBUG: Minutos fuera de rango: " + minutos);
                return null;
            }

            // Calcular el total sin usar String.format (que depende de la configuración regional)
            double minutosDecimal = minutos / 60.0;
            double total = horas + minutosDecimal;

            // Crear BigDecimal directamente del double
            BigDecimal resultado = BigDecimal.valueOf(total);
            System.out.println("DEBUG resultado: " + resultado);
            return resultado;
        } catch (NumberFormatException e) {
            System.out.println("DEBUG NumberFormatException: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("DEBUG Exception: " + e.getMessage());
            return null;
        }
    }
}


