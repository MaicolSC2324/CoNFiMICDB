package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.DisponibilidadDiaria;
import com.java.fx.repositories.AircraftRepository;
import com.java.fx.repositories.DisponibilidadDiariaRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DisponibilidadAeronavesController {

    @FXML
    private TextField tfAno;

    @FXML
    private ComboBox<String> cbAeronave;

    @FXML
    private ListView<String> lvMeses;

    @FXML
    private VBox vboxSeleccionMeses;

    @FXML
    private Button btnCargarDisponibilidad;

    @FXML
    private VBox vboxTablaDisponibilidad;

    @FXML
    private TableView<Map<String, Object>> tableDisponibilidad;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private DisponibilidadDiariaRepository disponibilidadRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private List<String> mesesList = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private int anoSeleccionado;
    private List<Integer> mesesSeleccionados = new ArrayList<>();
    private String matriculaSeleccionada;

    @FXML
    public void initialize() {
        configurarMeses();
        configurarAeronaves();
        btnCargarDisponibilidad.setOnAction(event -> cargarDisponibilidad());
        btnGuardar.setOnAction(event -> guardarDisponibilidad());
        btnVolver.setOnAction(event -> volver());
    }

    private void configurarMeses() {
        ObservableList<String> meses = FXCollections.observableArrayList(mesesList);
        lvMeses.setItems(meses);
        lvMeses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void configurarAeronaves() {
        List<String> matriculas = aircraftRepository.findAll().stream()
                .map(Aircraft::getMatricula)
                .sorted()
                .collect(Collectors.toList());
        cbAeronave.setItems(FXCollections.observableArrayList(matriculas));
    }

    private void cargarDisponibilidad() {
        // Validar datos de entrada
        if (tfAno.getText().isEmpty() || cbAeronave.getValue() == null || lvMeses.getSelectionModel().getSelectedItems().isEmpty()) {
            mostrarAlerta("Debe completar todos los campos: Año, Aeronave y seleccionar al menos un mes");
            return;
        }

        try {
            anoSeleccionado = Integer.parseInt(tfAno.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("El año debe ser un número válido");
            return;
        }

        // Obtener meses seleccionados
        mesesSeleccionados.clear();
        for (String mesSeleccionado : lvMeses.getSelectionModel().getSelectedItems()) {
            mesesSeleccionados.add(mesesList.indexOf(mesSeleccionado) + 1);
        }

        matriculaSeleccionada = cbAeronave.getValue();

        // Construir tabla de disponibilidad
        construirTablaDisponibilidad();

        // Mostrar tabla y habilitar botón guardar
        vboxTablaDisponibilidad.setVisible(true);
        vboxTablaDisponibilidad.setManaged(true);
        btnGuardar.setDisable(false);
    }

    private void construirTablaDisponibilidad() {
        tableDisponibilidad.getColumns().clear();
        tableDisponibilidad.getItems().clear();

        // Columna "Día" (primera columna)
        TableColumn<Map<String, Object>, String> colDia = new TableColumn<>("Día");
        colDia.setPrefWidth(60);
        colDia.setCellValueFactory(cellData -> {
            int dia = (int) cellData.getValue().get("dia");
            return new SimpleStringProperty(String.valueOf(dia));
        });
        tableDisponibilidad.getColumns().add(colDia);

        // Crear una columna por cada mes seleccionado
        for (Integer mes : mesesSeleccionados) {
            String nombreMes = mesesList.get(mes - 1);
            int mesNum = mes;

            TableColumn<Map<String, Object>, String> colMes = new TableColumn<>(nombreMes);
            colMes.setPrefWidth(120);
            colMes.setCellValueFactory(cellData -> {
                String estadoKey = "estado_mes_" + mesNum;
                String estado = (String) cellData.getValue().get(estadoKey);
                return new SimpleStringProperty(estado != null ? estado : "");
            });

            // Cell factory personalizado para ComboBox editable
            colMes.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>();

                {
                    comboBox.setItems(FXCollections.observableArrayList("A", "D", "I", "S", "N", "O", "°"));
                    comboBox.setStyle("-fx-font-size: 10;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                        String estadoKey = "estado_mes_" + mesNum;
                        String estadoActual = (String) rowData.get(estadoKey);

                        comboBox.setValue(estadoActual != null ? estadoActual : "");
                        comboBox.setOnAction(event -> {
                            rowData.put(estadoKey, comboBox.getValue());
                            rowData.put("mes_" + mesNum, mesNum);
                        });

                        // Deshabilitar si el día no existe en ese mes
                        boolean existe = (boolean) rowData.getOrDefault("existe_mes_" + mesNum, false);
                        comboBox.setDisable(!existe);

                        setGraphic(comboBox);
                    }
                }
            });

            tableDisponibilidad.getColumns().add(colMes);
        }

        // Cargar datos: una fila por día (1-31)
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();

        for (int dia = 1; dia <= 31; dia++) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("dia", dia);

            // Para cada mes seleccionado, cargar su disponibilidad
            for (Integer mes : mesesSeleccionados) {
                List<DisponibilidadDiaria> disponibilidades =
                    disponibilidadRepository.findByMatriculaAcAndAnoAndMes(matriculaSeleccionada, anoSeleccionado, mes);

                Map<Integer, String> mapEstados = disponibilidades.stream()
                        .collect(Collectors.toMap(DisponibilidadDiaria::getDia, DisponibilidadDiaria::getEstadoDisponibilidad));

                int diasEnMes = YearMonth.of(anoSeleccionado, mes).lengthOfMonth();

                String estadoKey = "estado_mes_" + mes;
                String existeKey = "existe_mes_" + mes;

                if (dia <= diasEnMes) {
                    fila.put(estadoKey, mapEstados.getOrDefault(dia, ""));
                    fila.put(existeKey, true);
                    fila.put("mes_" + mes, mes);
                } else {
                    fila.put(estadoKey, "°");
                    fila.put(existeKey, false);
                    fila.put("mes_" + mes, mes);
                }
            }

            datos.add(fila);
        }

        tableDisponibilidad.setItems(datos);
    }

    private void guardarDisponibilidad() {
        // Obtener datos de la tabla
        ObservableList<Map<String, Object>> datos = tableDisponibilidad.getItems();

        try {
            for (Map<String, Object> fila : datos) {
                int dia = (int) fila.get("dia");

                // Para cada mes seleccionado, guardar su estado
                for (Integer mes : mesesSeleccionados) {
                    String estadoKey = "estado_mes_" + mes;
                    String existeKey = "existe_mes_" + mes;

                    boolean existe = (boolean) fila.getOrDefault(existeKey, false);
                    String estado = (String) fila.get(estadoKey);

                    // Solo guardar días que existen en el mes
                    if (existe && estado != null && !estado.isEmpty() && !estado.equals("°")) {
                        DisponibilidadDiaria disponibilidad =
                            disponibilidadRepository.findByMatriculaAcAndAnoAndMesAndDia(
                                matriculaSeleccionada, anoSeleccionado, mes, dia
                            ).orElse(new DisponibilidadDiaria());

                        disponibilidad.setMatriculaAc(matriculaSeleccionada);
                        disponibilidad.setAno(anoSeleccionado);
                        disponibilidad.setMes(mes);
                        disponibilidad.setDia(dia);
                        disponibilidad.setEstadoDisponibilidad(estado);

                        disponibilidadRepository.save(disponibilidad);
                    }
                }
            }

            mostrarConfirmacion("Disponibilidad guardada exitosamente");
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        tfAno.clear();
        cbAeronave.setValue(null);
        lvMeses.getSelectionModel().clearSelection();
        vboxTablaDisponibilidad.setVisible(false);
        vboxTablaDisponibilidad.setManaged(false);
        btnGuardar.setDisable(true);
        tableDisponibilidad.getItems().clear();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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

            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
