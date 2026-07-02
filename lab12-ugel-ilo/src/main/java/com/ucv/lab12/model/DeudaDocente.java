package com.ucv.lab12.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Entidad DeudaDocente con JavaFX Properties para binding en TableView.
 *
 * Representa un registro de deuda administrativa de un docente de la
 * UGEL - Ilo (préstamos, descuentos judiciales, multas disciplinarias,
 * reposición de bienes, etc.).
 *
 * El DNI se maneja siempre en texto plano dentro del modelo en memoria;
 * la capa de Repository es responsable de cifrarlo/descifrarlo al
 * persistirlo (ver util.CryptoUtil), cumpliendo con el requisito de
 * encriptación de datos confidenciales (CE2 / ISO-IEC 27001).
 */
public class DeudaDocente {

    private final IntegerProperty idDeuda           = new SimpleIntegerProperty();
    private final StringProperty  nombreDocente      = new SimpleStringProperty();
    private final StringProperty  dni                = new SimpleStringProperty();
    private final StringProperty  tipoDeuda          = new SimpleStringProperty();
    private final DoubleProperty  monto              = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> fechaDeuda        = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> fechaVencimiento  = new SimpleObjectProperty<>();
    private final StringProperty  situacionLaboral   = new SimpleStringProperty();
    private final StringProperty  estado             = new SimpleStringProperty();
    private final StringProperty  observaciones      = new SimpleStringProperty();

    private final BooleanProperty seleccionado = new SimpleBooleanProperty(false);

    private final StringProperty nivelRiesgo = new SimpleStringProperty("BAJO");

    public DeudaDocente() {}

    public DeudaDocente(int idDeuda, String nombreDocente, String dni, String tipoDeuda,
                         double monto, LocalDate fechaDeuda, LocalDate fechaVencimiento,
                         String situacionLaboral, String estado, String observaciones) {
        setIdDeuda(idDeuda);
        setNombreDocente(nombreDocente);
        setDni(dni);
        setTipoDeuda(tipoDeuda);
        setMonto(monto);
        setFechaDeuda(fechaDeuda);
        setFechaVencimiento(fechaVencimiento);
        setSituacionLaboral(situacionLaboral);
        setEstado(estado);
        setObservaciones(observaciones);
    }

    // --- Properties ---
    public IntegerProperty idDeudaProperty()          { return idDeuda; }
    public StringProperty  nombreDocenteProperty()     { return nombreDocente; }
    public StringProperty  dniProperty()               { return dni; }
    public StringProperty  tipoDeudaProperty()         { return tipoDeuda; }
    public DoubleProperty  montoProperty()             { return monto; }
    public ObjectProperty<LocalDate> fechaDeudaProperty()       { return fechaDeuda; }
    public ObjectProperty<LocalDate> fechaVencimientoProperty() { return fechaVencimiento; }
    public StringProperty  situacionLaboralProperty()  { return situacionLaboral; }
    public StringProperty  estadoProperty()            { return estado; }
    public StringProperty  observacionesProperty()     { return observaciones; }
    public BooleanProperty seleccionadoProperty()      { return seleccionado; }
    public StringProperty  nivelRiesgoProperty()       { return nivelRiesgo; }

    public int        getIdDeuda()          { return idDeuda.get(); }
    public String      getNombreDocente()    { return nombreDocente.get(); }
    public String      getDni()              { return dni.get(); }
    public String      getTipoDeuda()        { return tipoDeuda.get(); }
    public double       getMonto()            { return monto.get(); }
    public LocalDate    getFechaDeuda()       { return fechaDeuda.get(); }
    public LocalDate    getFechaVencimiento() { return fechaVencimiento.get(); }
    public String       getSituacionLaboral() { return situacionLaboral.get(); }
    public String       getEstado()           { return estado.get(); }
    public String       getObservaciones()    { return observaciones.get(); }
    public boolean      isSeleccionado()      { return seleccionado.get(); }
    public String       getNivelRiesgo()      { return nivelRiesgo.get(); }

    public void setIdDeuda(int v)                    { idDeuda.set(v); }
    public void setNombreDocente(String v)           { nombreDocente.set(v); }
    public void setDni(String v)                     { dni.set(v); }
    public void setTipoDeuda(String v)               { tipoDeuda.set(v); }
    public void setMonto(double v)                   { monto.set(v); }
    public void setFechaDeuda(LocalDate v)           { fechaDeuda.set(v); }
    public void setFechaVencimiento(LocalDate v)     { fechaVencimiento.set(v); }
    public void setSituacionLaboral(String v)        { situacionLaboral.set(v); }
    public void setEstado(String v)                  { estado.set(v); }
    public void setObservaciones(String v)           { observaciones.set(v); }
    public void setSeleccionado(boolean v)           { seleccionado.set(v); }
    public void setNivelRiesgo(String v)             { nivelRiesgo.set(v); }

    @Override
    public String toString() {
        return getNombreDocente() + " - " + getTipoDeuda() + " (S/ " + getMonto() + ")";
    }
}
