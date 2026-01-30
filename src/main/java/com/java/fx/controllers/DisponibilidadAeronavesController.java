package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.DisponibilidadDiaria;
import com.java.fx.models.HojaLibro;
import com.java.fx.models.PiernaVuelo;
import com.java.fx.repositories.AircraftRepository;
import com.java.fx.repositories.DisponibilidadDiariaRepository;
import com.java.fx.repositories.HojaLibroRepository;
import com.java.fx.repositories.PiernaVueloRepository;
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
    private HojaLibroRepository hojaLibroRepository;

    @Autowired
    private PiernaVueloRepository piernaVueloRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private List<String> mesesList = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private int anoSeleccionado;
    private List<Integer> mesesSeleccionados = new ArrayList<>();
    private String matriculaSeleccionada;
    private boolean haycambiosSinGuardar = false;
    private Map<String, String> datosOriginalesBD = new HashMap<>(); // Almacenar datos de la BD

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
                .filter(Aircraft::getActivo)
                .map(Aircraft::getMatricula)
                .sorted()
                .collect(Collectors.toList());
        cbAeronave.setItems(FXCollections.observableArrayList(matriculas));
    }

    private void cargarDisponibilidad() {
        // Verificar si hay cambios sin guardar (incluyendo los asignados automáticamente)
        if (hayDiferenciasConBD() || haycambiosSinGuardar) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cambios sin guardar");
            alert.setHeaderText(null);
            alert.setContentText("¿Deseas cargar nuevas disponibilidades sin guardar los cambios realizados?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return; // Cancelar si el usuario no confirma
            }
            haycambiosSinGuardar = false; // Resetear después de confirmar
        }

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

        // Columna "Mes" (primera columna)
        TableColumn<Map<String, Object>, String> colMes = new TableColumn<>("Mes");
        colMes.setPrefWidth(80);
        colMes.setCellValueFactory(cellData -> {
            String mes = (String) cellData.getValue().get("mes");
            return new SimpleStringProperty(mes);
        });
        tableDisponibilidad.getColumns().add(colMes);

        // Crear una columna por cada día (1-31)
        for (int dia = 1; dia <= 31; dia++) {
            final int diaNum = dia;

            TableColumn<Map<String, Object>, String> colDia = new TableColumn<>(String.valueOf(dia));
            colDia.setPrefWidth(45);

            // CellValueFactory que lee directamente de la fila con la clave correcta
            colDia.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null) {
                    return new SimpleStringProperty("");
                }
                String estadoKey = "estado_dia_" + diaNum;
                String estado = (String) cellData.getValue().get(estadoKey);
                return new SimpleStringProperty(estado != null ? estado : "");
            });

            // Cell factory personalizado para ComboBox editable
            colDia.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
                private ComboBox<String> comboBox;

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || getTableView().getItems().isEmpty()) {
                        setGraphic(null);
                        comboBox = null;
                    } else {
                        // Obtener la fila actual (SIEMPRE)
                        Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                        if (rowData == null) {
                            setGraphic(null);
                            return;
                        }

                        // Crear el ComboBox si no existe
                        if (comboBox == null) {
                            comboBox = new ComboBox<>();
                            comboBox.setItems(FXCollections.observableArrayList("A", "D", "I", "S", "N", "O", "°"));
                            comboBox.setStyle("-fx-font-size: 10;");
                            comboBox.setPrefWidth(15);
                        }

                        // Claves para este día - USAR diaNum correctamente
                        String estadoKey = "estado_dia_" + diaNum;
                        String existeKey = "existe_dia_" + diaNum;

                        // Obtener el estado actual del Map de ESTA fila específica
                        String estadoActual = (String) rowData.get(estadoKey);

                        // Establecer el valor del ComboBox sin que dispare eventos
                        comboBox.setOnAction(null);
                        comboBox.setValue(estadoActual != null ? estadoActual : "");

                        // Deshabilitar si el día no existe en ese mes
                        boolean existe = (boolean) rowData.getOrDefault(existeKey, true);
                        comboBox.setDisable(!existe);

                        // Listener para detectar cambios - GUARDAR en rowData
                        comboBox.setOnAction(event -> {
                            String nuevoEstado = comboBox.getValue();
                            rowData.put(estadoKey, nuevoEstado);
                            haycambiosSinGuardar = true;
                        });

                        setGraphic(comboBox);
                    }
                }
            });

            tableDisponibilidad.getColumns().add(colDia);
        }

        // Cargar datos: una fila por mes seleccionado
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        datosOriginalesBD.clear(); // Limpiar copia anterior

        for (Integer mes : mesesSeleccionados) {
            // IMPORTANTE: Crear un nuevo Map para CADA mes
            Map<String, Object> fila = new HashMap<>();
            String nombreMes = mesesList.get(mes - 1);
            fila.put("mes", nombreMes);
            fila.put("mesNumero", mes);

            // Cargar disponibilidades de ESTE mes específico
            List<DisponibilidadDiaria> disponibilidades =
                disponibilidadRepository.findByMatriculaAcAndAnoAndMes(matriculaSeleccionada, anoSeleccionado, mes);

            // Crear mapa SOLO con disponibilidades de este mes
            Map<Integer, String> mapEstados = disponibilidades.stream()
                    .collect(Collectors.toMap(DisponibilidadDiaria::getDia, DisponibilidadDiaria::getEstadoDisponibilidad));

            int diasEnMes = YearMonth.of(anoSeleccionado, mes).lengthOfMonth();

            // Para cada día del mes (1-31)
            for (int dia = 1; dia <= 31; dia++) {
                String estadoKey = "estado_dia_" + dia;
                String existeKey = "existe_dia_" + dia;

                if (dia <= diasEnMes) {
                    // Obtener el estado DE ESTE MES específico
                    String estado = mapEstados.get(dia);
                    String estadoOriginalBD = estado; // Guardar estado original de BD

                    // Si no hay estado registrado, buscar si hay vuelo automáticamente
                    if (estado == null || estado.isEmpty()) {
                        String estadoAutomatico = verificarSiHayVuelo(matriculaSeleccionada, anoSeleccionado, mes, dia);
                        if (estadoAutomatico != null) {
                            estado = estadoAutomatico;
                        } else {
                            estado = "";
                        }
                    }

                    fila.put(estadoKey, estado != null ? estado : "");
                    fila.put(existeKey, true);

                    // Guardar el estado original de la BD (lo que estaba antes)
                    String claveOriginal = "original_dia_" + dia + "_mes_" + mes;
                    datosOriginalesBD.put(claveOriginal, estadoOriginalBD != null ? estadoOriginalBD : "");
                } else {
                    // Día no existe en este mes
                    fila.put(estadoKey, "°");
                    fila.put(existeKey, false);
                }
            }

            // Agregar esta fila (mes) a los datos
            datos.add(fila);
        }


        tableDisponibilidad.setItems(datos);
        // Refresh explícito para asegurar que se rendericen correctamente
        tableDisponibilidad.refresh();
    }

    private void guardarDisponibilidad() {
        // Obtener datos de la tabla
        ObservableList<Map<String, Object>> datos = tableDisponibilidad.getItems();

        try {
            for (Map<String, Object> fila : datos) {
                Integer mes = (Integer) fila.get("mesNumero");
                if (mes == null) continue;

                int diasEnMes = YearMonth.of(anoSeleccionado, mes).lengthOfMonth();

                // Para cada día del mes
                for (int dia = 1; dia <= diasEnMes; dia++) {
                    String estadoKey = "estado_dia_" + dia;
                    String estado = (String) fila.get(estadoKey);

                    // Solo guardar si tiene un estado válido
                    if (estado != null && !estado.isEmpty() && !estado.equals("°")) {
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
            haycambiosSinGuardar = false;
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        lvMeses.getSelectionModel().clearSelection();
        vboxTablaDisponibilidad.setVisible(false);
        vboxTablaDisponibilidad.setManaged(false);
        btnGuardar.setDisable(true);
        tableDisponibilidad.getItems().clear();
        haycambiosSinGuardar = false;
        datosOriginalesBD.clear(); // Limpiar copia de datos originales
    }

    // Verificar si hay vuelo para una aeronave en una fecha específica
    private String verificarSiHayVuelo(String matricula, int ano, int mes, int dia) {
        try {
            // Construir la fecha
            java.time.LocalDate fecha = java.time.LocalDate.of(ano, mes, dia);

            // Buscar si existe una HojaLibro para esta aeronave en esta fecha
            List<HojaLibro> hojas = hojaLibroRepository.findByMatriculaAndFecha(matricula, fecha);

            for (HojaLibro hoja : hojas) {
                // Buscar si hay piernas de vuelo en esta hoja
                List<PiernaVuelo> piernas = piernaVueloRepository.findByNoHojaLibroOrderByNoPiernaAsc(hoja.getNoHojaLibro());
                if (!piernas.isEmpty()) {
                    // Si hay al menos una pierna, hay vuelo
                    return "A"; // Vuelo Efectuado
                }
            }
        } catch (Exception e) {
            // Si hay error, simplemente retorna null (no asigna automáticamente)
        }
        return null; // No hay vuelo o no se encontró información
    }

    // Verificar si hay cambios comparando datos actuales con originales de la BD
    private boolean hayDiferenciasConBD() {
        ObservableList<Map<String, Object>> datosActuales = tableDisponibilidad.getItems();

        for (Map<String, Object> fila : datosActuales) {
            Integer mes = (Integer) fila.get("mesNumero");
            if (mes == null) continue;

            int diasEnMes = YearMonth.of(anoSeleccionado, mes).lengthOfMonth();

            for (int dia = 1; dia <= diasEnMes; dia++) {
                String estadoKey = "estado_dia_" + dia;
                String claveOriginal = "original_dia_" + dia + "_mes_" + mes;

                String estadoActual = (String) fila.get(estadoKey);
                String estadoOriginal = datosOriginalesBD.getOrDefault(claveOriginal, "");

                // Si el estado actual es diferente al original, hay cambios
                if (!estadoActual.equals(estadoOriginal)) {
                    // Ignorar cambios donde ambos son vacíos o null
                    if ((estadoActual == null || estadoActual.isEmpty()) && estadoOriginal.isEmpty()) {
                        continue;
                    }
                    // Hay cambio real
                    return true;
                }
            }
        }

        return false; // No hay cambios
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
        // Si hay cambios sin guardar (incluyendo los asignados automáticamente), mostrar confirmación
        if (hayDiferenciasConBD() || haycambiosSinGuardar) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cambios sin guardar");
            alert.setHeaderText(null);
            alert.setContentText("¿Deseas salir sin guardar los cambios realizados?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return; // No salir si el usuario cancela
            }
        }

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
