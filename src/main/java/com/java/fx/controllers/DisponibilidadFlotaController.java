package com.java.fx.controllers;

import com.java.fx.models.Aircraft;
import com.java.fx.models.DisponibilidadDiaria;
import com.java.fx.repositories.DisponibilidadDiariaRepository;
import com.java.fx.repositories.AircraftRepository;
import com.java.fx.repositories.HojaLibroRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import org.springframework.context.ApplicationContext;

@Component
public class DisponibilidadFlotaController {

    @FXML private TextField tfAño;
    @FXML private ListView<String> lvMeses;
    @FXML private ListView<String> lvAeronaves;
    @FXML private Label lblDisponibilidadGeneral;
    @FXML private Label lblUtilizacion;
    @FXML private Label lblTotalVuelos;
    @FXML private Label lblCancelaciones;
    @FXML private TableView<DisponibilidadPorMesDTO> tableDisponibilidad;
    @FXML private LineChart<String, Number> chartDisponibilidad;
    @FXML private PieChart chartEstados;

    @Autowired private DisponibilidadDiariaRepository disponibilidadRepository;
    @Autowired private AircraftRepository aircraftRepository;
    @Autowired private HojaLibroRepository hojaLibroRepository;
    @Autowired private ApplicationContext applicationContext;

    private Stage stageReportes;

    @FXML
    public void initialize() {
        configurarMeses();
        configurarAeronaves();
        lvMeses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvAeronaves.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void configurarMeses() {
        ObservableList<String> meses = FXCollections.observableArrayList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        lvMeses.setItems(meses);
    }

    private void configurarAeronaves() {
        List<Aircraft> aeronaves = aircraftRepository.findByActivoTrue();
        ObservableList<String> matriculas = FXCollections.observableArrayList();
        for (Aircraft a : aeronaves) {
            matriculas.add(a.getMatricula() + " - " + a.getModelo());
        }
        lvAeronaves.setItems(matriculas);
    }

    @FXML
    public void generarReporte() {
        ObservableList<Integer> mesesSeleccionados = lvMeses.getSelectionModel().getSelectedIndices();
        ObservableList<Integer> aeronavesIndices = lvAeronaves.getSelectionModel().getSelectedIndices();

        if (mesesSeleccionados.isEmpty() || aeronavesIndices.isEmpty() || tfAño.getText().isEmpty()) {
            mostrarAlerta("Seleccionar Datos", "Por favor seleccione año, meses y aeronaves");
            return;
        }

        try {
            int año = Integer.parseInt(tfAño.getText());
            List<Aircraft> todasAeronaves = aircraftRepository.findByActivoTrue();

            // Agrupar por TIPO DE AERONAVE (Modelo)
            Map<String, List<String>> matriculasPorModelo = new HashMap<>();
            for (int idx : aeronavesIndices) {
                if (idx < todasAeronaves.size()) {
                    Aircraft a = todasAeronaves.get(idx);
                    matriculasPorModelo.computeIfAbsent(a.getModelo(), k -> new ArrayList<>()).add(a.getMatricula());
                }
            }

            // Para cada modelo, calcular disponibilidad por MES
            List<DisponibilidadPorMesDTO> datosTabla = new ArrayList<>();
            Map<String, Integer> conteoEstados = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : matriculasPorModelo.entrySet()) {
                String modelo = entry.getKey();
                List<String> matriculasDelModelo = entry.getValue();

                // Para cada mes seleccionado
                for (Integer mesIdx : mesesSeleccionados) {
                    int mes = mesIdx + 1;
                    String[] mesesNombres = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};

                    DisponibilidadPorMesDTO dtoMes = new DisponibilidadPorMesDTO();
                    dtoMes.setModelo(modelo);
                    dtoMes.setMes(mesesNombres[mesIdx]);
                    dtoMes.setMesNum(mes);

                    // Acumular conteos para todas las aeronaves del modelo en este mes
                    int totalS = 0, totalI = 0, totalD = 0, totalO = 0, totalN = 0, totalA = 0;

                    for (String matricula : matriculasDelModelo) {
                        List<DisponibilidadDiaria> disponibilidades = disponibilidadRepository
                            .findByMatriculaAcAndAnoAndMes(matricula, año, mes);

                        for (DisponibilidadDiaria d : disponibilidades) {
                            String estado = d.getEstadoDisponibilidad();
                            if ("S".equals(estado)) totalS++;
                            else if ("I".equals(estado)) totalI++;
                            else if ("D".equals(estado)) totalD++;
                            else if ("O".equals(estado)) totalO++;
                            else if ("N".equals(estado)) totalN++;
                            else if ("A".equals(estado)) totalA++;

                            conteoEstados.put(estado, conteoEstados.getOrDefault(estado, 0) + 1);
                        }
                    }

                    // Calcular totales y porcentajes
                    int totalDias = totalS + totalI + totalD + totalO + totalN + totalA;

                    dtoMes.setCantidadS(totalS);
                    dtoMes.setPorcentajeS(totalDias > 0 ? (totalS * 100.0 / totalDias) : 0);

                    dtoMes.setCantidadI(totalI);
                    dtoMes.setPorcentajeI(totalDias > 0 ? (totalI * 100.0 / totalDias) : 0);

                    dtoMes.setCantidadD(totalD);
                    dtoMes.setPorcentajeD(totalDias > 0 ? (totalD * 100.0 / totalDias) : 0);

                    dtoMes.setCantidadO(totalO);
                    dtoMes.setPorcentajeO(totalDias > 0 ? (totalO * 100.0 / totalDias) : 0);

                    dtoMes.setCantidadN(totalN);
                    dtoMes.setPorcentajeN(totalDias > 0 ? (totalN * 100.0 / totalDias) : 0);

                    dtoMes.setCantidadA(totalA);
                    dtoMes.setPorcentajeA(totalDias > 0 ? (totalA * 100.0 / totalDias) : 0);

                    // Disponibilidad mensual = (A + N) / total
                    dtoMes.setDisponibilidadMensual(totalDias > 0 ? ((totalA + totalN) * 100.0 / totalDias) : 0);
                    dtoMes.setTotalDiasMes(totalDias);
                    dtoMes.setCantidadAeronaves(matriculasDelModelo.size());

                    datosTabla.add(dtoMes);
                }
            }

            actualizarTablaPorMes(datosTabla);
            calcularMetricasGenerales(datosTabla, conteoEstados);
            generarGraficos(datosTabla, conteoEstados, mesesSeleccionados);

            mostrarAlerta("Éxito", "Informe de disponibilidad generado correctamente");
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato Inválido", "Por favor ingrese un año válido");
        }
    }

    private void actualizarTablaPorMes(List<DisponibilidadPorMesDTO> datos) {
        tableDisponibilidad.getColumns().clear();

        // Columna MES
        TableColumn<DisponibilidadPorMesDTO, String> colMes = new TableColumn<>("MES");
        colMes.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMes()));
        colMes.setPrefWidth(60);

        // Columna CANTIDAD AERONAVES
        TableColumn<DisponibilidadPorMesDTO, Integer> colCantAero = new TableColumn<>("A.E.L\nSERVICIO");
        colCantAero.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getCantidadAeronaves()));
        colCantAero.setPrefWidth(70);

        // FALTA DE PARTES (S)
        TableColumn<DisponibilidadPorMesDTO, String> colSQty = new TableColumn<>("Qty.");
        colSQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadS())));
        colSQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colSPct = new TableColumn<>("%");
        colSPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeS())));
        colSPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colSHeader = new TableColumn<>("FALTA DE\nPARTES (S)");
        colSHeader.getColumns().addAll(colSQty, colSPct);
        colSHeader.setPrefWidth(100);

        // MANTO PROGRAMADO (I)
        TableColumn<DisponibilidadPorMesDTO, String> colIQty = new TableColumn<>("Qty.");
        colIQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadI())));
        colIQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colIPct = new TableColumn<>("%");
        colIPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeI())));
        colIPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colIHeader = new TableColumn<>("MANTO\nPROGRAMADO (I)");
        colIHeader.getColumns().addAll(colIQty, colIPct);
        colIHeader.setPrefWidth(100);

        // MANTO NO PROGRAMADO (D)
        TableColumn<DisponibilidadPorMesDTO, String> colDQty = new TableColumn<>("Qty.");
        colDQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadD())));
        colDQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colDPct = new TableColumn<>("%");
        colDPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeD())));
        colDPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colDHeader = new TableColumn<>("MANTO NO\nPROGRAMADO (D)");
        colDHeader.getColumns().addAll(colDQty, colDPct);
        colDHeader.setPrefWidth(100);

        // OTROS (O)
        TableColumn<DisponibilidadPorMesDTO, String> colOQty = new TableColumn<>("Qty.");
        colOQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadO())));
        colOQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colOPct = new TableColumn<>("%");
        colOPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeO())));
        colOPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colOHeader = new TableColumn<>("OTROS (O)");
        colOHeader.getColumns().addAll(colOQty, colOPct);
        colOHeader.setPrefWidth(100);

        // LISTO, NO PROGRAMADO (N)
        TableColumn<DisponibilidadPorMesDTO, String> colNQty = new TableColumn<>("Qty.");
        colNQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadN())));
        colNQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colNPct = new TableColumn<>("%");
        colNPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeN())));
        colNPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colNHeader = new TableColumn<>("LISTO, NO\nPROGRAMADO (N)");
        colNHeader.getColumns().addAll(colNQty, colNPct);
        colNHeader.setPrefWidth(100);

        // VUELOS EFECTUADOS (A)
        TableColumn<DisponibilidadPorMesDTO, String> colAQty = new TableColumn<>("Qty.");
        colAQty.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getCantidadA())));
        colAQty.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colAPct = new TableColumn<>("%");
        colAPct.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getPorcentajeA())));
        colAPct.setPrefWidth(50);

        TableColumn<DisponibilidadPorMesDTO, String> colAHeader = new TableColumn<>("VUELOS\nEFECTUADOS (A)");
        colAHeader.getColumns().addAll(colAQty, colAPct);
        colAHeader.setPrefWidth(100);

        // DISPONIBILIDAD MENSUAL
        TableColumn<DisponibilidadPorMesDTO, String> colDisp = new TableColumn<>("DISPONIBILIDAD\nMENSUAL (X)");
        colDisp.setCellValueFactory(param -> new SimpleStringProperty(String.format("%.1f%%", param.getValue().getDisponibilidadMensual())));
        colDisp.setPrefWidth(100);

        // DIAS X MES
        TableColumn<DisponibilidadPorMesDTO, Integer> colDias = new TableColumn<>("DIAS X\nMES");
        colDias.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTotalDiasMes()));
        colDias.setPrefWidth(70);

        tableDisponibilidad.getColumns().addAll(colMes, colCantAero, colSHeader, colIHeader, colDHeader, colOHeader, colNHeader, colAHeader, colDisp, colDias);
        tableDisponibilidad.setItems(FXCollections.observableArrayList(datos));
    }

    private DisponibilidadFlotaDTO calcularDisponibilidadAeronave(String matricula, int año, int mes) {
        DisponibilidadFlotaDTO dto = new DisponibilidadFlotaDTO();
        dto.setMatricula(matricula);

        Aircraft aeronave = aircraftRepository.findByMatricula(matricula).orElse(null);
        if (aeronave != null) {
            dto.setModelo(aeronave.getModelo());
        }

        // Obtener disponibilidades del mes
        List<DisponibilidadDiaria> disponibilidades = disponibilidadRepository
            .findByMatriculaAcAndAnoAndMes(matricula, año, mes);

        // Contar cada estado
        int countA = (int) disponibilidades.stream().filter(d -> "A".equals(d.getEstadoDisponibilidad())).count();
        int countD = (int) disponibilidades.stream().filter(d -> "D".equals(d.getEstadoDisponibilidad())).count();
        int countI = (int) disponibilidades.stream().filter(d -> "I".equals(d.getEstadoDisponibilidad())).count();
        int countS = (int) disponibilidades.stream().filter(d -> "S".equals(d.getEstadoDisponibilidad())).count();
        int countN = (int) disponibilidades.stream().filter(d -> "N".equals(d.getEstadoDisponibilidad())).count();
        int countO = (int) disponibilidades.stream().filter(d -> "O".equals(d.getEstadoDisponibilidad())).count();

        dto.setVuelosEfectuados(countA);
        dto.setNoDisponible(countD);
        dto.setServicioProgramado(countI);
        dto.setPorPartes(countS);
        dto.setListoNoProgramado(countN);
        dto.setOtros(countO);

        // Calcular porcentaje de disponibilidad (A + N) / total
        int totalDias = countA + countD + countI + countS + countN + countO;
        if (totalDias > 0) {
            double porcentaje = ((double) (countA + countN) / totalDias) * 100;
            dto.setPorcentajeDisponibilidad(porcentaje);
        }

        return dto;
    }

    private void actualizarConteoEstados(Map<String, Integer> conteo, String matricula, int año, int mes) {
        List<DisponibilidadDiaria> disponibilidades = disponibilidadRepository
            .findByMatriculaAcAndAnoAndMes(matricula, año, mes);

        for (DisponibilidadDiaria d : disponibilidades) {
            String estado = d.getEstadoDisponibilidad();
            if (estado != null) {
                conteo.put(estado, conteo.getOrDefault(estado, 0) + 1);
            }
        }
    }

    private void calcularMetricasGenerales(List<DisponibilidadPorMesDTO> datos, Map<String, Integer> conteoEstados) {
        double disponibilidadPromedio = datos.stream()
            .mapToDouble(DisponibilidadPorMesDTO::getDisponibilidadMensual)
            .average()
            .orElse(0);

        int totalDias = datos.stream()
            .mapToInt(DisponibilidadPorMesDTO::getTotalDiasMes)
            .sum();

        int totalVuelos = datos.stream()
            .mapToInt(DisponibilidadPorMesDTO::getCantidadA)
            .sum();

        int totalNoDisp = conteoEstados.getOrDefault("D", 0) +
                         conteoEstados.getOrDefault("S", 0);

        double utilizacion = totalDias > 0 ?
            ((double) conteoEstados.getOrDefault("A", 0) / totalDias) * 100 : 0;

        lblDisponibilidadGeneral.setText(String.format("%.2f%%", disponibilidadPromedio));
        lblUtilizacion.setText(String.format("%.2f%%", utilizacion));
        lblTotalVuelos.setText(String.valueOf(totalVuelos));
        lblCancelaciones.setText(String.valueOf(totalNoDisp));
    }

    private void generarGraficos(List<DisponibilidadPorMesDTO> datos, Map<String, Integer> conteoEstados, ObservableList<Integer> mesesSel) {
        // Gráfico de líneas - Disponibilidad por mes
        ObservableList<XYChart.Series<String, Number>> seriesLinea = FXCollections.observableArrayList();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Disponibilidad General");

        for (DisponibilidadPorMesDTO dto : datos) {
            serie.getData().add(new XYChart.Data<>(dto.getMes(), dto.getDisponibilidadMensual()));
        }

        seriesLinea.add(serie);
        chartDisponibilidad.setData(seriesLinea);

        // Gráfico de pastel - Distribución de estados con porcentajes
        int totalEstados = conteoEstados.values().stream().mapToInt(Integer::intValue).sum();

        ObservableList<PieChart.Data> dataPie = FXCollections.observableArrayList();

        int countA = conteoEstados.getOrDefault("A", 0);
        int countD = conteoEstados.getOrDefault("D", 0);
        int countI = conteoEstados.getOrDefault("I", 0);
        int countS = conteoEstados.getOrDefault("S", 0);
        int countN = conteoEstados.getOrDefault("N", 0);
        int countO = conteoEstados.getOrDefault("O", 0);

        double pctA = totalEstados > 0 ? (countA * 100.0 / totalEstados) : 0;
        double pctD = totalEstados > 0 ? (countD * 100.0 / totalEstados) : 0;
        double pctI = totalEstados > 0 ? (countI * 100.0 / totalEstados) : 0;
        double pctS = totalEstados > 0 ? (countS * 100.0 / totalEstados) : 0;
        double pctN = totalEstados > 0 ? (countN * 100.0 / totalEstados) : 0;
        double pctO = totalEstados > 0 ? (countO * 100.0 / totalEstados) : 0;

        dataPie.add(new PieChart.Data(String.format("Vuelos (A) %.1f%%", pctA), countA));
        dataPie.add(new PieChart.Data(String.format("No Disponible (D) %.1f%%", pctD), countD));
        dataPie.add(new PieChart.Data(String.format("Servicio (I) %.1f%%", pctI), countI));
        dataPie.add(new PieChart.Data(String.format("Partes (S) %.1f%%", pctS), countS));
        dataPie.add(new PieChart.Data(String.format("Listo (N) %.1f%%", pctN), countN));
        dataPie.add(new PieChart.Data(String.format("Otros (O) %.1f%%", pctO), countO));

        chartEstados.setData(dataPie);
    }

    @FXML
    public void volverReportes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) chartDisponibilidad.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Reportes");

            // Obtener el tamaño de la pantalla usando JavaFX Screen
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar a reportes");
        }
    }

    public void setStageReportes(Stage stage) {
        this.stageReportes = stage;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // DTO para transferencia de datos por MES
    public static class DisponibilidadPorMesDTO {
        private String modelo;
        private String mes;
        private int mesNum;
        private int cantidadAeronaves;

        // Estados y sus cantidades/porcentajes
        private int cantidadS, cantidadI, cantidadD, cantidadO, cantidadN, cantidadA;
        private double porcentajeS, porcentajeI, porcentajeD, porcentajeO, porcentajeN, porcentajeA;

        private double disponibilidadMensual;
        private int totalDiasMes;

        // Getters y Setters
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }

        public String getMes() { return mes; }
        public void setMes(String mes) { this.mes = mes; }

        public int getMesNum() { return mesNum; }
        public void setMesNum(int mesNum) { this.mesNum = mesNum; }

        public int getCantidadAeronaves() { return cantidadAeronaves; }
        public void setCantidadAeronaves(int cantidadAeronaves) { this.cantidadAeronaves = cantidadAeronaves; }

        public int getCantidadS() { return cantidadS; }
        public void setCantidadS(int cantidadS) { this.cantidadS = cantidadS; }

        public double getPorcentajeS() { return porcentajeS; }
        public void setPorcentajeS(double porcentajeS) { this.porcentajeS = porcentajeS; }

        public int getCantidadI() { return cantidadI; }
        public void setCantidadI(int cantidadI) { this.cantidadI = cantidadI; }

        public double getPorcentajeI() { return porcentajeI; }
        public void setPorcentajeI(double porcentajeI) { this.porcentajeI = porcentajeI; }

        public int getCantidadD() { return cantidadD; }
        public void setCantidadD(int cantidadD) { this.cantidadD = cantidadD; }

        public double getPorcentajeD() { return porcentajeD; }
        public void setPorcentajeD(double porcentajeD) { this.porcentajeD = porcentajeD; }

        public int getCantidadO() { return cantidadO; }
        public void setCantidadO(int cantidadO) { this.cantidadO = cantidadO; }

        public double getPorcentajeO() { return porcentajeO; }
        public void setPorcentajeO(double porcentajeO) { this.porcentajeO = porcentajeO; }

        public int getCantidadN() { return cantidadN; }
        public void setCantidadN(int cantidadN) { this.cantidadN = cantidadN; }

        public double getPorcentajeN() { return porcentajeN; }
        public void setPorcentajeN(double porcentajeN) { this.porcentajeN = porcentajeN; }

        public int getCantidadA() { return cantidadA; }
        public void setCantidadA(int cantidadA) { this.cantidadA = cantidadA; }

        public double getPorcentajeA() { return porcentajeA; }
        public void setPorcentajeA(double porcentajeA) { this.porcentajeA = porcentajeA; }

        public double getDisponibilidadMensual() { return disponibilidadMensual; }
        public void setDisponibilidadMensual(double disponibilidadMensual) { this.disponibilidadMensual = disponibilidadMensual; }

        public int getTotalDiasMes() { return totalDiasMes; }
        public void setTotalDiasMes(int totalDiasMes) { this.totalDiasMes = totalDiasMes; }
    }

    // ...existing code...
    // DTO para transferencia de datos
    public static class DisponibilidadFlotaDTO {
        private String matricula;
        private String modelo;
        private int vuelosEfectuados;
        private int noDisponible;
        private int servicioProgramado;
        private int porPartes;
        private int listoNoProgramado;
        private int otros;
        private double porcentajeDisponibilidad;

        // Getters y Setters
        public String getMatricula() { return matricula; }
        public void setMatricula(String matricula) { this.matricula = matricula; }

        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }

        public int getVuelosEfectuados() { return vuelosEfectuados; }
        public void setVuelosEfectuados(int vuelosEfectuados) { this.vuelosEfectuados = vuelosEfectuados; }

        public int getNoDisponible() { return noDisponible; }
        public void setNoDisponible(int noDisponible) { this.noDisponible = noDisponible; }

        public int getServicioProgramado() { return servicioProgramado; }
        public void setServicioProgramado(int servicioProgramado) { this.servicioProgramado = servicioProgramado; }

        public int getPorPartes() { return porPartes; }
        public void setPorPartes(int porPartes) { this.porPartes = porPartes; }

        public int getListoNoProgramado() { return listoNoProgramado; }
        public void setListoNoProgramado(int listoNoProgramado) { this.listoNoProgramado = listoNoProgramado; }

        public int getOtros() { return otros; }
        public void setOtros(int otros) { this.otros = otros; }

        public double getPorcentajeDisponibilidad() { return porcentajeDisponibilidad; }
        public void setPorcentajeDisponibilidad(double porcentajeDisponibilidad) {
            this.porcentajeDisponibilidad = porcentajeDisponibilidad;
        }
    }
}
