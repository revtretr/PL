package com.ucv.lab12.controller;

import com.ucv.lab12.model.DeudaDocente;
import com.ucv.lab12.service.IDeudaDocenteService;
import com.ucv.lab12.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DeudaDocenteFormController implements Initializable {

    @FXML private Label     lblTitulo;
    @FXML private TextField txtNombreDocente;
    @FXML private TextField txtDni;
    @FXML private ComboBox<String> cmbTipoDeuda;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dpFechaDeuda;
    @FXML private DatePicker dpFechaVencimiento;
    @FXML private ComboBox<String> cmbSituacionLaboral;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea  txtObservaciones;

    @FXML private Label lblNombreDocenteError;
    @FXML private Label lblDniError;
    @FXML private Label lblMontoError;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final IDeudaDocenteService service;
    private DeudaDocente deuda;
    private Runnable onGuardar;

    public DeudaDocenteFormController(IDeudaDocenteService service) {
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTipoDeuda.setItems(FXCollections.observableArrayList(DeudaDocenteController.TIPOS_DEUDA));
        cmbSituacionLaboral.setItems(FXCollections.observableArrayList(DeudaDocenteController.SITUACIONES_LABORALES));
        cmbEstado.setItems(FXCollections.observableArrayList(DeudaDocenteController.ESTADOS));
        cmbEstado.setValue("Pendiente");

        lblNombreDocenteError.setVisible(false);
        lblDniError.setVisible(false);
        lblMontoError.setVisible(false);

        limitarLongitud(txtNombreDocente, 150);
        limitarLongitud(txtDni, 8);
        limitarLongitud(txtObservaciones, 300);

        // Solo dígitos en el campo DNI
        txtDni.textProperty().addListener((o, v1, v2) -> {
            if (v2 != null && !v2.matches("\\d*")) {
                txtDni.setText(v2.replaceAll("[^\\d]", ""));
            }
            lblDniError.setVisible(false);
        });

        // Solo números y punto decimal en el campo Monto
        txtMonto.textProperty().addListener((o, v1, v2) -> {
            if (v2 != null && !v2.matches("\\d*(\\.\\d{0,2})?")) {
                txtMonto.setText(v1);
            } else {
                lblMontoError.setVisible(false);
            }
        });

        txtNombreDocente.textProperty().addListener((o, v1, v2) ->
            lblNombreDocenteError.setVisible(false));
    }

    // -------------------------------------------------------------------------
    // API pública para el controller padre
    // -------------------------------------------------------------------------

    public void setDeuda(DeudaDocente d) {
        this.deuda = d;
        if (d != null) {
            lblTitulo.setText("Editar Deuda de Docente");
            txtNombreDocente.setText(d.getNombreDocente());
            txtDni.setText(d.getDni() != null ? d.getDni() : "");
            cmbTipoDeuda.setValue(d.getTipoDeuda());
            txtMonto.setText(d.getMonto() > 0 ? String.valueOf(d.getMonto()) : "");
            dpFechaDeuda.setValue(d.getFechaDeuda());
            dpFechaVencimiento.setValue(d.getFechaVencimiento());
            cmbSituacionLaboral.setValue(d.getSituacionLaboral());
            cmbEstado.setValue(d.getEstado());
            txtObservaciones.setText(d.getObservaciones() != null ? d.getObservaciones() : "");
        } else {
            lblTitulo.setText("Nueva Deuda de Docente");
            dpFechaDeuda.setValue(LocalDate.now());
        }
    }

    public void setOnGuardar(Runnable callback) {
        this.onGuardar = callback;
    }

    // -------------------------------------------------------------------------
    // Handlers FXML
    // -------------------------------------------------------------------------

    @FXML
    private void onGuardar() {
        if (!validarFormulario()) return;

        Double monto = parseMonto();
        if (monto == null) return;

        DeudaDocente d = deuda != null ? deuda : new DeudaDocente();
        d.setNombreDocente(txtNombreDocente.getText().trim());
        d.setDni(txtDni.getText().trim());
        d.setTipoDeuda(cmbTipoDeuda.getValue());
        d.setMonto(monto);
        d.setFechaDeuda(dpFechaDeuda.getValue());
        d.setFechaVencimiento(dpFechaVencimiento.getValue());
        d.setSituacionLaboral(cmbSituacionLaboral.getValue());
        d.setEstado(cmbEstado.getValue());
        d.setObservaciones(txtObservaciones.getText() != null ? txtObservaciones.getText().trim() : "");

        try {
            if (deuda == null) {
                service.crear(d);
                AlertUtil.info("Éxito", "Deuda registrada exitosamente.");
            } else {
                service.actualizar(d);
                AlertUtil.info("Éxito", "Deuda actualizada exitosamente.");
            }
            if (onGuardar != null) onGuardar.run();
            cerrar();
        } catch (IllegalArgumentException ex) {
            AlertUtil.advertencia("Validación", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.error("Error", "No se pudo guardar:\n" + ex.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private boolean validarFormulario() {
        boolean ok = true;

        if (txtNombreDocente.getText() == null || txtNombreDocente.getText().trim().isEmpty()) {
            lblNombreDocenteError.setText("El nombre del docente es obligatorio.");
            lblNombreDocenteError.setVisible(true);
            txtNombreDocente.requestFocus();
            ok = false;
        }

        String dni = txtDni.getText() == null ? "" : txtDni.getText().trim();
        if (!dni.matches("^\\d{8}$")) {
            lblDniError.setText("El DNI debe tener exactamente 8 dígitos.");
            lblDniError.setVisible(true);
            if (ok) txtDni.requestFocus();
            ok = false;
        }

        if (cmbTipoDeuda.getValue() == null) {
            AlertUtil.advertencia("Validación", "Debe seleccionar el tipo de deuda.");
            ok = false;
        }

        if (ok && (dpFechaDeuda.getValue() == null || dpFechaVencimiento.getValue() == null)) {
            AlertUtil.advertencia("Validación", "Debe indicar la fecha de la deuda y la fecha de vencimiento.");
            ok = false;
        } else if (ok && dpFechaVencimiento.getValue().isBefore(dpFechaDeuda.getValue())) {
            AlertUtil.advertencia("Validación", "La fecha de vencimiento no puede ser anterior a la fecha de la deuda.");
            ok = false;
        }

        if (ok && cmbSituacionLaboral.getValue() == null) {
            AlertUtil.advertencia("Validación", "Debe seleccionar la situación laboral del docente.");
            ok = false;
        }

        return ok;
    }

    private Double parseMonto() {
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            if (monto <= 0) {
                lblMontoError.setText("El monto debe ser mayor a cero.");
                lblMontoError.setVisible(true);
                txtMonto.requestFocus();
                return null;
            }
            return monto;
        } catch (NumberFormatException | NullPointerException e) {
            lblMontoError.setText("Ingrese un monto válido (ej: 1500.50).");
            lblMontoError.setVisible(true);
            txtMonto.requestFocus();
            return null;
        }
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    private void limitarLongitud(TextInputControl control, int max) {
        control.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > max) {
                control.setText(oldVal);
            }
        });
    }
}
