package com.java.fx.controllers;

import com.java.fx.models.HojaLibro;
import com.java.fx.models.Discrepancia;
import com.java.fx.models.Aircraft;
import com.java.fx.repositories.HojaLibroRepository;
import com.java.fx.repositories.DiscrepanciaRepository;
import com.java.fx.repositories.AircraftRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AtasRepetitivasController {

    @FXML
    private Spinner<Integer> spinnerYear;

    @FXML
    private ListView<String> listviewMeses;

    @FXML
    private ListView<String> listviewAeronaves;

    @FXML
    private Button btnGenerar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableView<AtasRepetitivasDTO> tableAtas;

    @FXML
    private TableView<SubAtasDTO> tableSubAtas;

    @FXML
    private TextArea textAreaMensajes;

    @FXML
    private StackPane chartContainer;

    @FXML
    private ComboBox<String> comboBoxATA;

    @FXML
    private Button btnMostrarATA;

    @FXML
    private StackPane chartDetailedContainer;

    @Autowired
    private HojaLibroRepository hojaLibroRepository;

    @Autowired
    private DiscrepanciaRepository discrepanciaRepository;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private List<String> aeronavesDisponibles;
    private Map<String, Map<Integer, Map<String, Integer>>> atasDataGlobal;
    private List<Integer> mesesGlobal;
    private List<String> aeronavesGlobal;
    private Map<String, Map<String, Map<Integer, Map<String, Integer>>>> subAtasDataGlobal;

    public void initialize() {
        try {
            // Cargar hoja de estilos
            String css = getClass().getResource("/chart-styles.css").toExternalForm();
            chartContainer.getStylesheets().add(css);

            configurarSpinner();
            configurarListaMeses();
            cargarAeronaves();
            configurarTabla();
            btnGenerar.setOnAction(event -> generarReporte());
            btnMostrarATA.setOnAction(event -> mostrarATA());
            btnVolver.setOnAction(event -> volverReportes());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error en Initialize", "Error al inicializar: " + e.getMessage());
        }
    }

    private void configurarSpinner() {
        spinnerYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2099, YearMonth.now().getYear()));
    }

    private void configurarListaMeses() {
        ObservableList<String> meses = FXCollections.observableArrayList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        listviewMeses.setItems(meses);
        listviewMeses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void cargarAeronaves() {
        try {
            List<Aircraft> aeronaves = aircraftRepository.findByActivoTrue();
            List<String> aeronavesDisplay = new ArrayList<>();

            for (Aircraft a : aeronaves) {
                aeronavesDisplay.add(a.getMatricula() + " - " + a.getModelo());
            }

            aeronavesDisponibles = aeronavesDisplay.stream().sorted().collect(Collectors.toList());

            ObservableList<String> items = FXCollections.observableArrayList(aeronavesDisponibles);
            listviewAeronaves.setItems(items);
            listviewAeronaves.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar aeronaves: " + e.getMessage());
        }
    }

    private void configurarTabla() {
        tableAtas.getColumns().clear();
        TableColumn<AtasRepetitivasDTO, String> colAta = new TableColumn<>("ATA");
        colAta.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().ata));
        colAta.setPrefWidth(80);
        tableAtas.getColumns().add(colAta);
    }

    @FXML
    private void generarReporte() {
        try {
            textAreaMensajes.clear();

            Integer year = spinnerYear.getValue();
            ObservableList<String> mesesSeleccionadosNombres = listviewMeses.getSelectionModel().getSelectedItems();
            ObservableList<String> aeronavesSeleccionadasDisplay = listviewAeronaves.getSelectionModel().getSelectedItems();

            if (mesesSeleccionadosNombres.isEmpty() || aeronavesSeleccionadasDisplay.isEmpty()) {
                mostrarAlerta("Validación", "Por favor selecciona al menos un mes y una aeronave");
                return;
            }

            // Convertir nombres de meses a números
            List<Integer> mesesSeleccionados = new ArrayList<>();
            String[] mesesNombres = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                     "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            for (String mesNombre : mesesSeleccionadosNombres) {
                for (int i = 0; i < mesesNombres.length; i++) {
                    if (mesesNombres[i].equals(mesNombre)) {
                        mesesSeleccionados.add(i + 1);
                        break;
                    }
                }
            }

            // Extraer matrículas del formato "matricula - modelo"
            List<String> aeronavesSeleccionadas = new ArrayList<>();
            for (String display : aeronavesSeleccionadasDisplay) {
                String matricula = display.split(" - ")[0];
                aeronavesSeleccionadas.add(matricula);
            }

            Map<String, Map<Integer, Map<String, Integer>>> atasData = new HashMap<>();
            Map<String, Map<String, Map<Integer, Map<String, Integer>>>> subAtasData = new HashMap<>();

            for (String matricula : aeronavesSeleccionadas) {
                for (Integer mes : mesesSeleccionados) {
                    List<HojaLibro> hojas = hojaLibroRepository.findByMatriculaAndYearAndMonth(matricula, year, mes);

                    for (HojaLibro hoja : hojas) {
                        List<Discrepancia> atas = discrepanciaRepository.findByNoHojaLibro(hoja.getNoHojaLibro());

                        for (Discrepancia ata : atas) {
                            String ataCode = ata.getAta(); // Formato: NN-NN-NN
                            String ataPrincipal = ataCode.split("-")[0];
                            String subAta = ataCode.substring(0, ataCode.lastIndexOf("-")); // NN-NN

                            // Datos de ATA principal
                            atasData.computeIfAbsent(ataPrincipal, k -> new HashMap<>())
                                    .computeIfAbsent(mes, k -> new HashMap<>())
                                    .merge(matricula, 1, Integer::sum);

                            // Datos de SubATA
                            subAtasData.computeIfAbsent(ataPrincipal, k -> new HashMap<>())
                                    .computeIfAbsent(subAta, k -> new HashMap<>())
                                    .computeIfAbsent(mes, k -> new HashMap<>())
                                    .merge(matricula, 1, Integer::sum);
                        }
                    }
                }
            }

            mostrarDatos(atasData, subAtasData, mesesSeleccionados, aeronavesSeleccionadas, year);
            textAreaMensajes.clear();

            // Limpiar solo gráficos y tabla de SubATAs, pero NO el ComboBox de ATAs
            chartDetailedContainer.getChildren().clear();
            tableSubAtas.getColumns().clear();
            tableSubAtas.getItems().clear();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarDatos(Map<String, Map<Integer, Map<String, Integer>>> atasData,
                             Map<String, Map<String, Map<Integer, Map<String, Integer>>>> subAtasData,
                             List<Integer> meses, List<String> aeronaves, Integer year) {
        tableAtas.getColumns().clear();

        // Columna ATA
        TableColumn<AtasRepetitivasDTO, String> colAta = new TableColumn<>("ATA");
        colAta.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().ata));
        colAta.setPrefWidth(80);
        colAta.setMinWidth(50);
        colAta.setMaxWidth(80);
        tableAtas.getColumns().add(colAta);

        Collections.sort(meses);

        // Crear encabezados por mes con subcolumnas para cada aeronave + acumulado
        for (Integer mes : meses) {
            String nombreMes = obtenerNombreMes(mes);

            // Crear columna padre para el mes
            TableColumn<AtasRepetitivasDTO, String> mesColumn = new TableColumn<>(nombreMes);
            mesColumn.setStyle("-fx-alignment: CENTER;");

            // Agregar subcolumnas para cada aeronave en este mes
            for (String matricula : aeronaves) {
                TableColumn<AtasRepetitivasDTO, Integer> col = new TableColumn<>(matricula);
                col.setPrefWidth(70);
                col.setMinWidth(60);
                col.setMaxWidth(150);
                final String finalMatricula = matricula;
                final Integer finalMes = mes;
                col.setCellValueFactory(cellData -> {
                    Map<Integer, Map<String, Integer>> mesData = atasData.getOrDefault(cellData.getValue().ata, new HashMap<>());
                    Map<String, Integer> matriculaData = mesData.getOrDefault(finalMes, new HashMap<>());
                    Integer valor = matriculaData.getOrDefault(finalMatricula, 0);
                    return new javafx.beans.property.SimpleObjectProperty<>(valor);
                });
                // CustomCell para no mostrar ceros
                col.setCellFactory(col1 -> new javafx.scene.control.TableCell<AtasRepetitivasDTO, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item == 0) {
                            setText("");
                        } else {
                            setText(item.toString());
                        }
                    }
                });
                mesColumn.getColumns().add(col);
            }

            // Agregar subcolumna de ACUM
            TableColumn<AtasRepetitivasDTO, Integer> colAcumulado = new TableColumn<>("ACUM");
            colAcumulado.setPrefWidth(65);
            colAcumulado.setMinWidth(60);
            colAcumulado.setMaxWidth(100);
            final Integer finalMes = mes;
            colAcumulado.setCellValueFactory(cellData -> {
                Map<Integer, Map<String, Integer>> mesData = atasData.getOrDefault(cellData.getValue().ata, new HashMap<>());
                Map<String, Integer> matriculaData = mesData.getOrDefault(finalMes, new HashMap<>());
                Integer total = matriculaData.values().stream().mapToInt(Integer::intValue).sum();
                return new javafx.beans.property.SimpleObjectProperty<>(total);
            });
            colAcumulado.setCellFactory(col -> new javafx.scene.control.TableCell<AtasRepetitivasDTO, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item == 0) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            });
            mesColumn.getColumns().add(colAcumulado);

            tableAtas.getColumns().add(mesColumn);
        }

        // Agregar columna TOTAL al final
        TableColumn<AtasRepetitivasDTO, Integer> colTotal = new TableColumn<>("TOTAL");
        colTotal.setPrefWidth(70);
        colTotal.setMinWidth(60);
        colTotal.setMaxWidth(100);
        colTotal.setCellValueFactory(cellData -> {
            Map<Integer, Map<String, Integer>> ataAllData = atasData.getOrDefault(cellData.getValue().ata, new HashMap<>());
            Integer totalAta = ataAllData.values().stream()
                    .flatMap(map -> map.values().stream())
                    .mapToInt(Integer::intValue)
                    .sum();
            return new javafx.beans.property.SimpleObjectProperty<>(totalAta);
        });
        colTotal.setCellFactory(col -> new javafx.scene.control.TableCell<AtasRepetitivasDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else {
                    setText(item.toString());
                }
            }
        });
        tableAtas.getColumns().add(colTotal);

        ObservableList<AtasRepetitivasDTO> datos = FXCollections.observableArrayList();
        for (String ata : atasData.keySet()) {
            datos.add(new AtasRepetitivasDTO(ata));
        }

        // Ordenar ATAs de menor a mayor (como números)
        datos.sort((ata1, ata2) -> {
            try {
                Integer num1 = Integer.parseInt(ata1.ata);
                Integer num2 = Integer.parseInt(ata2.ata);
                return num1.compareTo(num2);
            } catch (NumberFormatException e) {
                return ata1.ata.compareTo(ata2.ata);
            }
        });

        tableAtas.setItems(datos);

        // Guardar datos globales para análisis detallado
        atasDataGlobal = atasData;
        subAtasDataGlobal = subAtasData;
        mesesGlobal = meses;
        aeronavesGlobal = aeronaves;

        // Aplicar estilos a encabezados
        aplicarEstilosEncabezadosTabla(tableAtas);

        // Poblar comboBox de ATAs
        ObservableList<String> atasItems = FXCollections.observableArrayList(atasData.keySet());
        atasItems.sort((ata1, ata2) -> {
            try {
                Integer num1 = Integer.parseInt(ata1);
                Integer num2 = Integer.parseInt(ata2);
                return num1.compareTo(num2);
            } catch (NumberFormatException e) {
                return ata1.compareTo(ata2);
            }
        });
        comboBoxATA.setItems(atasItems);

        // Crear gráfico
        crearGraficoAtas(atasData, meses, aeronaves);
    }

    private void crearGraficoAtas(Map<String, Map<Integer, Map<String, Integer>>> atasData,
                                  List<Integer> meses, List<String> aeronaves) {
        chartContainer.getChildren().clear();

        // Obtener todas las ATAs ordenadas de menor a mayor
        List<String> atasOrdenadas = new ArrayList<>(atasData.keySet());
        atasOrdenadas.sort((ata1, ata2) -> {
            try {
                Integer num1 = Integer.parseInt(ata1);
                Integer num2 = Integer.parseInt(ata2);
                return num1.compareTo(num2);
            } catch (NumberFormatException e) {
                return ata1.compareTo(ata2);
            }
        });

        // Crear eje X (ATAs) con las categorías en orden
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("ATA");
        xAxis.setCategories(FXCollections.observableArrayList(atasOrdenadas));
        xAxis.setStyle("-fx-tick-label-fill: #000000; -fx-font-size: 12; -fx-text-fill: #000000;");

        // Crear eje Y (Cantidad de reportes)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Reportes");
        yAxis.setStyle("-fx-tick-label-fill: #000000; -fx-font-size: 12; -fx-text-fill: #000000;");
        yAxis.setMinorTickVisible(true);

        // Crear gráfico de columnas
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("ATAS Repetitivas por Mes");
        barChart.setLegendVisible(true);
        barChart.setLegendSide(Side.BOTTOM);
        barChart.setAnimated(false);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(true);

        Collections.sort(meses);

        // Agregar series para cada mes
        for (Integer mes : meses) {
            String nombreMes = obtenerNombreMes(mes);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(nombreMes);

            // Para cada ATA en orden, sumar los reportes de todas las aeronaves en ese mes
            for (String ata : atasOrdenadas) {
                Map<Integer, Map<String, Integer>> ataAllMeses = atasData.get(ata);
                Map<String, Integer> ataMesData = ataAllMeses.getOrDefault(mes, new HashMap<>());
                Integer totalAeronavesEnMes = ataMesData.values().stream().mapToInt(Integer::intValue).sum();

                // Agregar siempre, aunque sea 0, para mantener alineación
                series.getData().add(new XYChart.Data<>(ata, totalAeronavesEnMes));
            }

            barChart.getData().add(series);
        }

        chartContainer.getChildren().add(barChart);

        // Aplicar CSS después de agregar al contenedor
        javafx.application.Platform.runLater(() -> {
            barChart.applyCss();
        });
    }

    private void mostrarATA() {
        String ataSeleccionada = comboBoxATA.getValue();
        if (ataSeleccionada == null || ataSeleccionada.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor selecciona una ATA");
            return;
        }

        // Crear gráfico de detalle
        crearGraficoDetallado(ataSeleccionada);

        // Crear tabla de subATAs
        crearTablaSubAtas(ataSeleccionada);
    }

    private void crearGraficoDetallado(String ata) {
        chartDetailedContainer.getChildren().clear();

        // Eje X (Meses)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mes");
        xAxis.setStyle("-fx-tick-label-fill: #000000; -fx-font-size: 12; -fx-text-fill: #000000;");

        // Eje Y (Reportes)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Reportes");
        yAxis.setStyle("-fx-tick-label-fill: #000000; -fx-font-size: 12; -fx-text-fill: #000000;");
        yAxis.setMinorTickVisible(true);

        // Crear gráfico de barras
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("ATA " + ata + " - Comportamiento por Aeronave");
        barChart.setLegendVisible(true);
        barChart.setLegendSide(Side.BOTTOM);
        barChart.setAnimated(false);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(true);
        barChart.setPrefHeight(450);
        barChart.setMinHeight(450);

        // Obtener datos de la ATA seleccionada
        Map<Integer, Map<String, Integer>> ataData = atasDataGlobal.getOrDefault(ata, new HashMap<>());

        // Para cada aeronave, crear una serie
        int colorIndex = 0;
        for (String aeronave : aeronavesGlobal) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(aeronave);

            // Para cada mes, obtener datos de esa aeronave
            for (Integer mes : mesesGlobal) {
                String nombreMes = obtenerNombreMes(mes);
                Map<String, Integer> mesData = ataData.getOrDefault(mes, new HashMap<>());
                Integer valor = mesData.getOrDefault(aeronave, 0);

                if (valor > 0) {
                    series.getData().add(new XYChart.Data<>(nombreMes, valor));
                }
            }

            // Solo agregar serie si tiene datos
            if (!series.getData().isEmpty()) {
                barChart.getData().add(series);
            }

            colorIndex++;
        }

        chartDetailedContainer.getChildren().add(barChart);

        // Cargar CSS después de agregar al contenedor
        javafx.application.Platform.runLater(() -> {
            String css = getClass().getResource("/chart-styles.css").toExternalForm();
            barChart.getStylesheets().clear();
            barChart.getStylesheets().add(css);
            barChart.applyCss();
        });
    }

    private void crearTablaSubAtas(String ata) {
        tableSubAtas.getColumns().clear();

        // Obtener datos de subATAs para el ATA seleccionada
        Map<String, Map<Integer, Map<String, Integer>>> subAtasForAta = subAtasDataGlobal.getOrDefault(ata, new HashMap<>());

        // Columna SubATA
        TableColumn<SubAtasDTO, String> colSubAta = new TableColumn<>("SUB-ATA");
        colSubAta.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().subAta));
        colSubAta.setPrefWidth(70);
        colSubAta.setMinWidth(70);
        colSubAta.setMaxWidth(120);
        tableSubAtas.getColumns().add(colSubAta);

        Collections.sort(mesesGlobal);

        // Crear encabezados por mes con subcolumnas para cada aeronave + acumulado
        for (Integer mes : mesesGlobal) {
            String nombreMes = obtenerNombreMes(mes);

            // Crear columna padre para el mes
            TableColumn<SubAtasDTO, String> mesColumn = new TableColumn<>(nombreMes);
            mesColumn.setStyle("-fx-alignment: CENTER;");

            // Agregar subcolumnas para cada aeronave en este mes
            for (String matricula : aeronavesGlobal) {
                TableColumn<SubAtasDTO, Integer> col = new TableColumn<>(matricula);
                col.setPrefWidth(70);
                col.setMinWidth(60);
                col.setMaxWidth(150);
                final String finalMatricula = matricula;
                final Integer finalMes = mes;
                col.setCellValueFactory(cellData -> {
                    Map<Integer, Map<String, Integer>> mesData = subAtasForAta.getOrDefault(cellData.getValue().subAta, new HashMap<>());
                    Map<String, Integer> matriculaData = mesData.getOrDefault(finalMes, new HashMap<>());
                    Integer valor = matriculaData.getOrDefault(finalMatricula, 0);
                    return new javafx.beans.property.SimpleObjectProperty<>(valor);
                });
                // CustomCell para no mostrar ceros
                col.setCellFactory(col1 -> new javafx.scene.control.TableCell<SubAtasDTO, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item == 0) {
                            setText("");
                        } else {
                            setText(item.toString());
                        }
                    }
                });
                mesColumn.getColumns().add(col);
            }

            // Agregar subcolumna de ACUM
            TableColumn<SubAtasDTO, Integer> colAcumulado = new TableColumn<>("ACUM");
            colAcumulado.setPrefWidth(65);
            colAcumulado.setMinWidth(60);
            colAcumulado.setMaxWidth(100);
            final Integer finalMes = mes;
            colAcumulado.setCellValueFactory(cellData -> {
                Map<Integer, Map<String, Integer>> mesData = subAtasForAta.getOrDefault(cellData.getValue().subAta, new HashMap<>());
                Map<String, Integer> matriculaData = mesData.getOrDefault(finalMes, new HashMap<>());
                Integer total = matriculaData.values().stream().mapToInt(Integer::intValue).sum();
                return new javafx.beans.property.SimpleObjectProperty<>(total);
            });
            colAcumulado.setCellFactory(col -> new javafx.scene.control.TableCell<SubAtasDTO, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item == 0) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            });
            mesColumn.getColumns().add(colAcumulado);

            tableSubAtas.getColumns().add(mesColumn);
        }

        // Agregar columna TOTAL al final
        TableColumn<SubAtasDTO, Integer> colTotal = new TableColumn<>("TOTAL");
        colTotal.setPrefWidth(70);
        colTotal.setMinWidth(60);
        colTotal.setMaxWidth(100);
        colTotal.setCellValueFactory(cellData -> {
            Map<Integer, Map<String, Integer>> subAtaAllData = subAtasForAta.getOrDefault(cellData.getValue().subAta, new HashMap<>());
            Integer totalSubAta = subAtaAllData.values().stream()
                    .flatMap(map -> map.values().stream())
                    .mapToInt(Integer::intValue)
                    .sum();
            return new javafx.beans.property.SimpleObjectProperty<>(totalSubAta);
        });
        colTotal.setCellFactory(col -> new javafx.scene.control.TableCell<SubAtasDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else {
                    setText(item.toString());
                }
            }
        });
        tableSubAtas.getColumns().add(colTotal);

        // Aplicar estilos a la tabla de subATAs
        aplicarEstilosEncabezadosTabla(tableSubAtas);

        // Obtener todas las subATAs ordenadas
        ObservableList<SubAtasDTO> datos = FXCollections.observableArrayList();
        for (String subAta : subAtasForAta.keySet()) {
            datos.add(new SubAtasDTO(subAta));
        }

        // Ordenar SubATAs de menor a mayor
        datos.sort((subAta1, subAta2) -> {
            try {
                Integer num1 = Integer.parseInt(subAta1.subAta.replace("-", ""));
                Integer num2 = Integer.parseInt(subAta2.subAta.replace("-", ""));
                return num1.compareTo(num2);
            } catch (NumberFormatException e) {
                return subAta1.subAta.compareTo(subAta2.subAta);
            }
        });

        tableSubAtas.setItems(datos);
    }

    private String obtenerNombreMes(Integer mes) {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    @FXML
    public void volverReportes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReportesView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Reportes");

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar a reportes");
        }
    }

    private void aplicarEstilosEncabezadosTabla(TableView<?> tabla) {
        javafx.application.Platform.runLater(() -> {
            tabla.lookup(".column-header-background").setStyle(
                "-fx-background-color: #D3D3D3;"
            );

            // Aplicar estilo a todos los column-header
            tabla.lookupAll(".column-header").forEach(node -> {
                node.setStyle(
                    "-fx-text-fill: #000000; " +
                    "-fx-background-color: #D3D3D3; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 12px; " +
                    "-fx-padding: 5px;"
                );
            });

            // Aplicar estilo a los labels dentro de los encabezados
            tabla.lookupAll(".column-header .label").forEach(node -> {
                node.setStyle(
                    "-fx-text-fill: #000000; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 12px;"
                );
            });
        });
    }

    public static class AtasRepetitivasDTO {
        public String ata;

        public AtasRepetitivasDTO(String ata) {
            this.ata = ata;
        }
    }

    public static class SubAtasDTO {
        public String subAta;

        public SubAtasDTO(String subAta) {
            this.subAta = subAta;
        }
    }
}
