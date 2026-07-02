package com.ucv.lab12.controller;

import com.ucv.lab12.config.AppContext;
import com.ucv.lab12.model.DeudaDocente;
import com.ucv.lab12.service.IDeudaDocenteService;
import com.ucv.lab12.util.AlertUtil;
import com.ucv.lab12.util.CryptoUtil;
import com.ucv.lab12.util.PdfExportUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DeudaDocenteController implements Initializable {

    @FXML private TextField        txtDocente;
    @FXML private ComboBox<String> cmbTipoDeuda;
    @FXML private ComboBox<String> cmbSituacionLaboral;
    @FXML private DatePicker       dpFechaDesde;
    @FXML private DatePicker       dpFechaHasta;
    @FXML private Button           btnBuscar;
    @FXML private Button           btnLimpiarFiltros;

    @FXML private Button btnCrear;
    @FXML private Button btnEliminarSeleccionados;
    @FXML private Button btnExportarPdf;
    @FXML private Label  lblTotal;
    @FXML private Label  lblUsuario;

    @FXML private TableView<DeudaDocente>              tableView;
    @FXML private TableColumn<DeudaDocente, Boolean>   colSeleccion;
    @FXML private TableColumn<DeudaDocente, Integer>   colId;
    @FXML private TableColumn<DeudaDocente, String>    colDocente;
    @FXML private TableColumn<DeudaDocente, String>    colDni;
    @FXML private TableColumn<DeudaDocente, String>    colTipoDeuda;
    @FXML private TableColumn<DeudaDocente, Double>    colMonto;
    @FXML private TableColumn<DeudaDocente, LocalDate> colFechaDeuda;
    @FXML private TableColumn<DeudaDocente, LocalDate> colFechaVencimiento;
    @FXML private TableColumn<DeudaDocente, String>    colSituacionLaboral;
    @FXML private TableColumn<DeudaDocente, String>    colEstado;
    @FXML private TableColumn<DeudaDocente, String>    colRiesgo;
    @FXML private TableColumn<DeudaDocente, Void>      colAcciones;

    private final IDeudaDocenteService service;
    private final ObservableList<DeudaDocente> data = FXCollections.observableArrayList();

    public static final String[] TIPOS_DEUDA = {
        "Préstamo Administrativo", "Descuento Judicial", "Multa Disciplinaria",
        "Reposición de Bienes", "Otro"
    };
    public static final String[] SITUACIONES_LABORALES = {
        "Nombrado", "Contratado", "Encargado", "Cesante"
    };
    public static final String[] ESTADOS = { "Pendiente", "En Proceso", "Pagada" };

    public DeudaDocenteController(IDeudaDocenteService service) {
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColumnas();
        cargarDatos();

        txtDocente.setOnAction(e -> onBuscar());
    }
    private void configurarFiltros() {
        cmbTipoDeuda.setItems(FXCollections.observableArrayList(TIPOS_DEUDA));
        cmbSituacionLaboral.setItems(FXCollections.observableArrayList(SITUACIONES_LABORALES));
    }

    private void configurarColumnas() {
        tableView.setEditable(true);

        colSeleccion.setCellValueFactory(cell -> cell.getValue().seleccionadoProperty());
        colSeleccion.setCellFactory(CheckBoxTableCell.forTableColumn(colSeleccion));
        colSeleccion.setEditable(true);
        colSeleccion.setSortable(false);

        colId.setCellValueFactory(new PropertyValueFactory<>("idDeuda"));
        colDocente.setCellValueFactory(new PropertyValueFactory<>("nombreDocente"));

        colDni.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                CryptoUtil.enmascarar(cell.getValue().getDni())));

        colTipoDeuda.setCellValueFactory(new PropertyValueFactory<>("tipoDeuda"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("S/ %.2f", item));
            }
        });

        colFechaDeuda.setCellValueFactory(new PropertyValueFactory<>("fechaDeuda"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colSituacionLaboral.setCellValueFactory(new PropertyValueFactory<>("situacionLaboral"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colRiesgo.setCellValueFactory(new PropertyValueFactory<>("nivelRiesgo"));
        colRiesgo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item) {
                    case "ALTO" -> setStyle("-fx-text-fill:#C62828; -fx-font-weight:bold;");
                    case "MEDIO" -> setStyle("-fx-text-fill:#EF6C00; -fx-font-weight:bold;");
                    default -> setStyle("-fx-text-fill:#2E7D32; -fx-font-weight:bold;");
                }
            }
        });

        colAcciones.setCellFactory(crearCeldaAcciones());
        colAcciones.setSortable(false);

        tableView.setItems(data);
    }

    private Callback<TableColumn<DeudaDocente, Void>, TableCell<DeudaDocente, Void>> crearCeldaAcciones() {
        return col -> new TableCell<>() {
            private final Button btnEditar   = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox   hbox        = new HBox(5, btnEditar, btnEliminar);

            {
                hbox.setAlignment(Pos.CENTER);
                btnEditar.setStyle(
                    "-fx-background-color:#1976D2;-fx-text-fill:white;"
                    + "-fx-cursor:hand;-fx-font-size:11px;");
                btnEliminar.setStyle(
                    "-fx-background-color:#D32F2F;-fx-text-fill:white;"
                    + "-fx-cursor:hand;-fx-font-size:11px;");

                btnEditar.setOnAction(e -> {
                    DeudaDocente d = getTableView().getItems().get(getIndex());
                    abrirFormulario(d);
                });
                btnEliminar.setOnAction(e -> {
                    DeudaDocente d = getTableView().getItems().get(getIndex());
                    confirmarEliminar(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        };
    }

    private void cargarDatos() {
        try {
            List<DeudaDocente> lista = service.buscar(
                txtDocente.getText(),
                cmbTipoDeuda.getValue(),
                cmbSituacionLaboral.getValue(),
                dpFechaDesde.getValue(),
                dpFechaHasta.getValue()
            );
            data.setAll(lista);
            lblTotal.setText("Total: " + data.size() + " registro(s)");
        } catch (Exception e) {
            AlertUtil.error("Error de conexión", "No se pudo cargar los datos:\n" + e.getMessage());
        }
    }

    /** Permite a MainApp mostrar el usuario autenticado en la barra superior. */
    public void setUsuarioLogueado(String nombreUsuario, String rol) {
        if (lblUsuario != null) {
            lblUsuario.setText("Usuario: " + nombreUsuario + " (" + rol + ")");
        }
    }

    @FXML
    private void onBuscar() {
        cargarDatos();
    }

    @FXML
    private void onLimpiarFiltros() {
        txtDocente.clear();
        cmbTipoDeuda.setValue(null);
        cmbSituacionLaboral.setValue(null);
        dpFechaDesde.setValue(null);
        dpFechaHasta.setValue(null);
        cargarDatos();
    }

    @FXML
    private void onCrear() {
        abrirFormulario(null);
    }

    @FXML
    private void onEliminarSeleccionados() {
        List<Integer> ids = data.stream()
            .filter(DeudaDocente::isSeleccionado)
            .map(DeudaDocente::getIdDeuda)
            .collect(Collectors.toList());

        if (ids.isEmpty()) {
            AlertUtil.advertencia("Sin selección",
                "Marque al menos un registro para eliminar.");
            return;
        }

        boolean ok = AlertUtil.confirmar("Confirmar eliminación",
            "¿Está seguro de eliminar " + ids.size() + " registro(s) de deuda seleccionado(s)?");
        if (!ok) return;

        try {
            service.eliminarSeleccionados(ids);
            cargarDatos();
            AlertUtil.info("Éxito", "Se eliminaron " + ids.size() + " registro(s).");
        } catch (Exception e) {
            AlertUtil.error("Error", "No se pudo eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void onExportarPdf() {
        if (data.isEmpty()) {
            AlertUtil.advertencia("Sin datos", "No hay registros para exportar con los filtros actuales.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar reporte de deudas a PDF");
        fileChooser.setInitialFileName("reporte_deudas_docentes.pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));

        File destino = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (destino == null) return;

        try {
            PdfExportUtil.exportar(data, destino);
            AlertUtil.info("Éxito", "Reporte exportado correctamente a:\n" + destino.getAbsolutePath());
        } catch (IOException e) {
            AlertUtil.error("Error", "No se pudo generar el PDF:\n" + e.getMessage());
        }
    }

    private void confirmarEliminar(DeudaDocente d) {
        boolean ok = AlertUtil.confirmar("Confirmar eliminación",
            "¿Eliminar la deuda de: " + d.getNombreDocente() + "?");
        if (!ok) return;

        try {
            service.eliminar(d.getIdDeuda());
            cargarDatos();
        } catch (Exception e) {
            AlertUtil.error("Error", "No se pudo eliminar: " + e.getMessage());
        }
    }

    private void abrirFormulario(DeudaDocente deuda) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ucv/lab12/deuda-form.fxml"));
            loader.setControllerFactory(AppContext.getInstance()::getController);
            Parent root = loader.load();

            DeudaDocenteFormController formCtrl = loader.getController();
            formCtrl.setDeuda(deuda);
            formCtrl.setOnGuardar(this::cargarDatos);

            Stage modal = new Stage();
            modal.setTitle(deuda == null ? "Nueva Deuda de Docente" : "Editar Deuda de Docente");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            AlertUtil.error("Error", "No se pudo abrir el formulario:\n" + e.getMessage());
        }
    }
}
