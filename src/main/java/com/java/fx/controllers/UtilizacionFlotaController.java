package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.HojaLibro;
import com.java.fx.models.PiernaVuelo;
import com.java.fx.repositories.AircraftRepository;
import com.java.fx.repositories.HojaLibroRepository;
import com.java.fx.repositories.PiernaVueloRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UtilizacionFlotaController {

    @FXML
    private TabPane tabPaneUtilizacion;

    @FXML
    private Tab tabComparativoUtilizacion;

    @FXML
    private ListView<String> lvMatriculas;

    @FXML
    private ComboBox<String> cbAnio;

    @FXML
    private ListView<String> lvMeses;

    @FXML
    private Button btnGenerarComparativo;

    @FXML
    private Button btnVolver;

    @FXML
    private TableView<Map<String, String>> tableComparativo;

    @FXML
    private TableColumn<Map<String, String>, String> colAnio;

    @FXML
    private LineChart<String, Number> chartComparativo;

    @Autowired
    private AircraftRepository aircraftRepository;

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

    @FXML
    public void initialize() {
        configurarListViewMatriculas();
        configurarComboBoxAnio();
        configurarListViewMeses();
        configurarColumnasTabla();
        btnGenerarComparativo.setOnAction(event -> generarReporte());
        btnVolver.setOnAction(event -> volver());
    }

    private void configurarListViewMatriculas() {
        lvMatriculas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        List<String> matriculas = aircraftRepository.findAll().stream()
                .map(Aircraft::getMatricula)
                .sorted()
                .collect(Collectors.toList());
        lvMatriculas.setItems(FXCollections.observableArrayList(matriculas));
    }

    private void configurarComboBoxAnio() {
        int anioActual = java.time.LocalDate.now().getYear();
        ObservableList<String> anios = FXCollections.observableArrayList();
        for (int i = anioActual - 5; i <= anioActual; i++) {
            anios.add(String.valueOf(i));
        }
        cbAnio.setItems(anios);
        cbAnio.setValue(String.valueOf(anioActual));
        cbAnio.setEditable(true);
    }

    private void configurarListViewMeses() {
        lvMeses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvMeses.setItems(FXCollections.observableArrayList(mesesList));
        lvMeses.getSelectionModel().selectAll();
    }

    private void configurarColumnasTabla() {
        colAnio.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("anio")));
    }

    private void crearColumnasParaMeses(List<Integer> mesesSeleccionados) {
        tableComparativo.getColumns().clear();

        TableColumn<Map<String, String>, String> colAnioNew = new TableColumn<>("Año");
        colAnioNew.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("anio")));
        colAnioNew.setPrefWidth(80);
        tableComparativo.getColumns().add(colAnioNew);

        for (Integer mes : mesesSeleccionados) {
            String nombreMes = mesesList.get(mes - 1);
            TableColumn<Map<String, String>, String> colMes = new TableColumn<>(nombreMes);
            final String mesKey = "mes_" + mes;
            colMes.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getOrDefault(mesKey, "0:00")));
            colMes.setPrefWidth(100);
            tableComparativo.getColumns().add(colMes);
        }
    }

    private void generarReporte() {
        List<String> matriculasSeleccionadas = lvMatriculas.getSelectionModel().getSelectedItems();
        List<Integer> mesesSeleccionados = lvMeses.getSelectionModel().getSelectedItems().stream()
                .map(mesesList::indexOf)
                .map(i -> i + 1)
                .collect(Collectors.toList());

        String anioTexto = cbAnio.getValue();

        if (matriculasSeleccionadas.isEmpty() || mesesSeleccionados.isEmpty() || anioTexto == null || anioTexto.trim().isEmpty()) {
            mostrarAlerta("Selecciona matrículas, meses y año para generar el reporte");
            return;
        }

        try {
            Integer anioSeleccionado = Integer.parseInt(anioTexto.trim());

            crearColumnasParaMeses(mesesSeleccionados);

            ObservableList<Map<String, String>> datosTabla = FXCollections.observableArrayList();

            Map<String, String> filaAnioActual = new HashMap<>();
            filaAnioActual.put("anio", "Año " + anioSeleccionado);
            for (Integer mes : mesesSeleccionados) {
                String horas = calcularHorasPorMes(matriculasSeleccionadas, mes, anioSeleccionado);
                filaAnioActual.put("mes_" + mes, horas);
            }
            datosTabla.add(filaAnioActual);

            Map<String, String> filaAnioAnterior = new HashMap<>();
            filaAnioAnterior.put("anio", "Año " + (anioSeleccionado - 1));
            for (Integer mes : mesesSeleccionados) {
                String horas = calcularHorasPorMes(matriculasSeleccionadas, mes, anioSeleccionado - 1);
                filaAnioAnterior.put("mes_" + mes, horas);
            }
            datosTabla.add(filaAnioAnterior);

            tableComparativo.setItems(datosTabla);

            generarGraficoTranspuesto(filaAnioActual, filaAnioAnterior, mesesSeleccionados, anioSeleccionado);

            mostrarConfirmacion("Informe generado adecuadamente");
        } catch (NumberFormatException e) {
            mostrarAlerta("El año ingresado no es válido. Ingrese un número entero.");
        }
    }

    private String calcularHorasPorMes(List<String> matriculas, Integer mes, Integer anio) {
        long totalMinutos = 0;

        for (String matricula : matriculas) {
            List<HojaLibro> hojas = hojaLibroRepository.findByMatriculaAndMesAndAnio(matricula, mes, anio);
            for (HojaLibro hoja : hojas) {
                List<PiernaVuelo> piernas = piernaVueloRepository.findByNoHojaLibroOrderByNoPiernaAsc(hoja.getNoHojaLibro());
                for (PiernaVuelo pierna : piernas) {
                    if (pierna.getTiempoVuelo() != null) {
                        totalMinutos += convertirLocalTimeAMinutos(pierna.getTiempoVuelo());
                    }
                }
            }
        }

        return convertirMinutosAFormatoHora(totalMinutos);
    }

    private long convertirLocalTimeAMinutos(java.time.LocalTime tiempo) {
        return (tiempo.getHour() * 60L) + tiempo.getMinute();
    }

    private String convertirMinutosAFormatoHora(long minutos) {
        long horas = minutos / 60;
        long mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    private void generarGraficoTranspuesto(Map<String, String> datosAnioActual,
                                            Map<String, String> datosAnioAnterior,
                                            List<Integer> meses, Integer anio) {
        chartComparativo.getData().clear();

        XYChart.Series<String, Number> serieAnioActual = new XYChart.Series<>();
        serieAnioActual.setName("Año " + anio);

        XYChart.Series<String, Number> serieAnioAnterior = new XYChart.Series<>();
        serieAnioAnterior.setName("Año " + (anio - 1));

        for (Integer mes : meses) {
            String mesNombre = mesesList.get(mes - 1);
            String horasActual = datosAnioActual.get("mes_" + mes);
            String horasAnterior = datosAnioAnterior.get("mes_" + mes);

            double valorActual = convertirFormatoHoraADecimal(horasActual);
            double valorAnterior = convertirFormatoHoraADecimal(horasAnterior);

            serieAnioActual.getData().add(new XYChart.Data<>(mesNombre, valorActual));
            serieAnioAnterior.getData().add(new XYChart.Data<>(mesNombre, valorAnterior));
        }

        chartComparativo.getData().add(serieAnioActual);
        chartComparativo.getData().add(serieAnioAnterior);
    }

    private double convertirFormatoHoraADecimal(String horaFormato) {
        if (horaFormato == null || horaFormato.isEmpty() || horaFormato.equals("0:00")) {
            return 0;
        }
        String[] partes = horaFormato.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        return horas + (minutos / 60.0);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reportes");
            stage.setMaximized(true);
            stage.show();

            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
