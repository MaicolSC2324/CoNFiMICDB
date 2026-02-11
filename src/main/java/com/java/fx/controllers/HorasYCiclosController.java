package com.java.fx.controllers;

import com.java.fx.dtos.HorasYCiclosDTO;
import com.java.fx.dtos.ReporteTipoAeronaveDTO;
import com.java.fx.services.ReporteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Controller
public class HorasYCiclosController {

    @FXML
    private DatePicker dpFechaInicio;

    @FXML
    private DatePicker dpFechaFin;

    @FXML
    private Button btnGenerar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableView<HorasYCiclosDTO> tablaAeronaveIndividual;

    @FXML
    private TableColumn<HorasYCiclosDTO, String> colMatricula;

    @FXML
    private TableColumn<HorasYCiclosDTO, String> colFabricante;

    @FXML
    private TableColumn<HorasYCiclosDTO, String> colModelo;

    @FXML
    private TableColumn<HorasYCiclosDTO, String> colTotalHorasIndividual;

    @FXML
    private TableColumn<HorasYCiclosDTO, Integer> colTotalCiclosIndividual;

    @FXML
    private TableView<ReporteTipoAeronaveDTO> tablaReporte;

    @FXML
    private TableColumn<ReporteTipoAeronaveDTO, String> colTipoAeronave;

    @FXML
    private TableColumn<ReporteTipoAeronaveDTO, Integer> colCantidad;

    @FXML
    private TableColumn<ReporteTipoAeronaveDTO, String> colTotalHoras;

    @FXML
    private TableColumn<ReporteTipoAeronaveDTO, Integer> colTotalCiclos;

    @FXML
    private Label lblGranTotalHoras;

    @FXML
    private Label lblGranTotalCiclos;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ApplicationContext applicationContext;

    private ObservableList<HorasYCiclosDTO> reporteAeronaveList = FXCollections.observableArrayList();
    private ObservableList<ReporteTipoAeronaveDTO> reporteTipoList = FXCollections.observableArrayList();

    public void initialize() {
        // Configurar columnas tabla aeronave individual
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colFabricante.setCellValueFactory(new PropertyValueFactory<>("fabricante"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colTotalHorasIndividual.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
        colTotalCiclosIndividual.setCellValueFactory(new PropertyValueFactory<>("totalCiclos"));

        // Configurar columnas tabla tipo de aeronave
        colTipoAeronave.setCellValueFactory(new PropertyValueFactory<>("tipoAeronave"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAeronaves"));
        colTotalHoras.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
        colTotalCiclos.setCellValueFactory(new PropertyValueFactory<>("totalCiclos"));

        // Establecer fechas por defecto
        LocalDate hoy = LocalDate.now();
        LocalDate primerDiaDelMes = hoy.withDayOfMonth(1);

        dpFechaInicio.setValue(primerDiaDelMes);
        dpFechaFin.setValue(hoy);


        // Configurar botones
        btnGenerar.setOnAction(event -> generarReporte());
        btnVolver.setOnAction(event -> volver());
    }

    @FXML
    private void generarReporte() {
        try {
            // Obtener fechas de los DatePicker
            LocalDate fechaInicio = dpFechaInicio.getValue();
            LocalDate fechaFin = dpFechaFin.getValue();

            // Validar que se hayan seleccionado fechas
            if (fechaInicio == null || fechaFin == null) {
                mostrarError("Validación", "Debe seleccionar rango de fechas");
                return;
            }

            // Validar que la fecha de inicio sea menor o igual a la fecha de fin
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarError("Validación", "La fecha de inicio debe ser menor o igual a la fecha de fin");
                return;
            }

            // Convertir LocalDate a YearMonth para compatibilidad con el servicio
            YearMonth yearMonthInicio = YearMonth.from(fechaInicio);
            YearMonth yearMonthFin = YearMonth.from(fechaFin);

            // Generar reporte individual por aeronave
            List<HorasYCiclosDTO> datosIndividuales = reporteService.generarReporteHorasYCiclosConFechas(fechaInicio, fechaFin);

            // Generar reporte por tipo de aeronave
            Map<String, Object> resultadoTipo = reporteService.generarReportePorTipoAeronaveConFechas(fechaInicio, fechaFin);

            @SuppressWarnings("unchecked")
            List<ReporteTipoAeronaveDTO> datosTipo = (List<ReporteTipoAeronaveDTO>) resultadoTipo.get("reportePorTipo");
            String granTotalHoras = (String) resultadoTipo.get("granTotalHoras");
            Integer granTotalCiclos = (Integer) resultadoTipo.get("granTotalCiclos");

            // Llenar tabla de aeronaves individuales
            reporteAeronaveList.clear();
            reporteAeronaveList.addAll(datosIndividuales);
            tablaAeronaveIndividual.setItems(reporteAeronaveList);

            // Llenar tabla de tipos de aeronave
            reporteTipoList.clear();
            reporteTipoList.addAll(datosTipo);
            tablaReporte.setItems(reporteTipoList);

            // Mostrar gran total
            lblGranTotalHoras.setText(granTotalHoras);
            lblGranTotalCiclos.setText(String.valueOf(granTotalCiclos));

            mostrarInfo("Éxito", "Reporte generado correctamente");
        } catch (Exception e) {
            mostrarError("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void abrirHojaLibroView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HojaLibroView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setTitle("Gestionar Hojas del Libro de Vuelo");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "Error al abrir gestionar hojas: " + e.getMessage());
        }
    }

    @FXML
    private void volver() {
        try {
            // Reabrirla vista de Reportes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reportes");
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana de HorasYCiclos
            Stage horasYCiclosStage = (Stage) btnVolver.getScene().getWindow();
            horasYCiclosStage.close();
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
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
