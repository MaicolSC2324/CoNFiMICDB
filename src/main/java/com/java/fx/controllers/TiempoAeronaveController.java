package com.java.fx.controllers;

import com.java.fx.models.TiempoAeronave;
import com.java.fx.models.HojaLibroDetalle;
import com.java.fx.services.AircraftService;
import com.java.fx.services.TiempoAeronaveService;
import com.java.fx.services.HojaLibroDetalleService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TiempoAeronaveController {

    @FXML
    private ComboBox<String> cbMatricula;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private Button btnCalcular;
    @FXML
    private Button btnVolver;
    @FXML
    private Button btnGestionarHojas;
    @FXML
    private Button btnCargarMas;
    @FXML
    private TableView<HojaLibroDetalle> tableHojas;
    @FXML
    private Label lblTiempoTotal;
    @FXML
    private Label lblCiclosTotal;
    @FXML
    private Label lblResultados;

    @Autowired
    private AircraftService aircraftService;

    @Autowired
    private TiempoAeronaveService tiempoAeronaveService;

    @Autowired
    private HojaLibroDetalleService hojaLibroDetalleService;

    @Autowired
    private ApplicationContext applicationContext;

    public void initialize() {
        // Cargar matrículas
        cargarMatriculas();

        // Configurar botones
        btnCalcular.setOnAction(event -> calcular());
        btnVolver.setOnAction(event -> volverAReportes());
        btnGestionarHojas.setOnAction(event -> abrirGestionarHojas());
        btnCargarMas.setOnAction(event -> cargarTodosRegistros());

        // Deshabilitar botón inicialmente
        btnCalcular.setDisable(true);

        // Listener para habilitar botón cuando se seleccione matrícula y fecha
        cbMatricula.valueProperty().addListener((obs, oldVal, newVal) -> habilitarBoton());
        dpFecha.valueProperty().addListener((obs, oldVal, newVal) -> habilitarBoton());
    }

    private void cargarMatriculas() {
        try {
            List<String> matriculas = aircraftService.findAll()
                    .stream()
                    .map(aircraft -> aircraft.getMatricula())
                    .collect(Collectors.toList());

            cbMatricula.setItems(FXCollections.observableArrayList(matriculas));
        } catch (Exception e) {
            mostrarError("Error", "Error al cargar matrículas: " + e.getMessage());
        }
    }

    private void habilitarBoton() {
        boolean habilitado = cbMatricula.getValue() != null && dpFecha.getValue() != null;
        btnCalcular.setDisable(!habilitado);
    }

    @FXML
    private void calcular() {
        try {
            String matricula = cbMatricula.getValue();
            LocalDate fecha = dpFecha.getValue();

            if (matricula == null || matricula.trim().isEmpty() || fecha == null) {
                mostrarError("Validación", "Debe seleccionar matrícula y fecha");
                return;
            }

            TiempoAeronave resultado = tiempoAeronaveService.calcularTiemposHastaFecha(matricula, fecha);

            if (resultado == null) {
                mostrarError("Error", "No se encontró la aeronave");
                return;
            }

            // Mostrar resultados
            lblTiempoTotal.setText("Tiempo Total: " + resultado.getTiempoTotal() + " hrs");
            lblCiclosTotal.setText("Ciclos Total: " + resultado.getCiclosTotal());
            lblResultados.setText("Cálculo realizado para: " + matricula + " hasta " + fecha);

            // Cargar tabla de hojas
            cargarTablaHojas(matricula, fecha);

            // Mostrar mensaje de confirmación
            mostrarConfirmacion("Informe Generado", "El informe fue generado exitosamente");

        } catch (Exception e) {
            mostrarError("Error al calcular", e.getMessage());
        }
    }

    private void cargarTablaHojas(String matricula, LocalDate fecha) {
        try {
            List<HojaLibroDetalle> hojas = hojaLibroDetalleService.obtenerHojasConVuelos(matricula, fecha, 50);

            // PASO 1: Obtener el total acumulado de la aeronave
            TiempoAeronave totalAeronave = tiempoAeronaveService.calcularTiemposHastaFecha(matricula, fecha);
            long ttafTotalSegundos = convertirFormatoASegundos(totalAeronave.getTiempoTotal());
            int tcafTotal = Integer.parseInt(totalAeronave.getCiclosTotal().toString());

            // PASO 2: Calcular TTAF y TCAF acumulativo restando
            long ttafActualSegundos = ttafTotalSegundos;
            int tcafActual = tcafTotal;

            for (HojaLibroDetalle hoja : hojas) {
                // Asignar el TTAF y TCAF actual
                hoja.setTtaf(convertirSegundosAFormato(ttafActualSegundos));
                hoja.setTcaf(tcafActual);

                // RESTAR para la siguiente fila
                long tiempoVueloSegundos = convertirFormatoASegundos(hoja.getTiempoVueloHoja());
                ttafActualSegundos -= tiempoVueloSegundos;
                tcafActual -= hoja.getCiclosHoja();
            }

            tableHojas.setItems(FXCollections.observableArrayList(hojas));

            // Mostrar/ocultar botón cargar más
            boolean hayMas = hojaLibroDetalleService.hayMasRegistros(matricula, fecha);
            btnCargarMas.setVisible(hayMas);
            btnCargarMas.setManaged(hayMas);

        } catch (Exception e) {
            mostrarError("Error", "Error al cargar hojas de vuelo: " + e.getMessage());
        }
    }

    private long convertirFormatoASegundos(String formato) {
        if (formato == null || formato.isEmpty()) {
            return 0;
        }
        String[] partes = formato.split(":");
        if (partes.length == 2) {
            long horas = Long.parseLong(partes[0]);
            long minutos = Long.parseLong(partes[1]);
            return horas * 3600 + minutos * 60;
        }
        return 0;
    }

    private String convertirSegundosAFormato(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        return String.format("%d:%02d", horas, minutos);
    }

    private void cargarTodosRegistros() {
        try {
            String matricula = cbMatricula.getValue();
            LocalDate fecha = dpFecha.getValue();

            List<HojaLibroDetalle> hojas = hojaLibroDetalleService.obtenerTodasHojasConVuelos(matricula, fecha);

            // PASO 1: Obtener el total acumulado de la aeronave
            TiempoAeronave totalAeronave = tiempoAeronaveService.calcularTiemposHastaFecha(matricula, fecha);
            long ttafTotalSegundos = convertirFormatoASegundos(totalAeronave.getTiempoTotal());
            int tcafTotal = Integer.parseInt(totalAeronave.getCiclosTotal().toString());

            // PASO 2: Calcular TTAF y TCAF acumulativo restando
            long ttafActualSegundos = ttafTotalSegundos;
            int tcafActual = tcafTotal;

            for (HojaLibroDetalle hoja : hojas) {
                // Asignar el TTAF y TCAF actual
                hoja.setTtaf(convertirSegundosAFormato(ttafActualSegundos));
                hoja.setTcaf(tcafActual);

                // RESTAR para la siguiente fila
                long tiempoVueloSegundos = convertirFormatoASegundos(hoja.getTiempoVueloHoja());
                ttafActualSegundos -= tiempoVueloSegundos;
                tcafActual -= hoja.getCiclosHoja();
            }

            tableHojas.setItems(FXCollections.observableArrayList(hojas));

            // Ocultar botón cargar más
            btnCargarMas.setVisible(false);
            btnCargarMas.setManaged(false);

        } catch (Exception e) {
            mostrarError("Error", "Error al cargar registros: " + e.getMessage());
        }
    }

    @FXML
    private void volverAReportes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reportes");
            stage.setMaximized(true);
            stage.show();

            // Cerrar esta ventana
            Stage currentStage = (Stage) btnVolver.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirGestionarHojas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HojaLibroView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Gestionar Hojas del Libro de Vuelo");
            stage.setMaximized(true);
            stage.show();

            // Cerrar esta ventana
            Stage currentStage = (Stage) btnGestionarHojas.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
