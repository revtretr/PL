package com.ucv.lab12.service;

import com.ucv.lab12.model.DeudaDocente;
import com.ucv.lab12.repository.IDeudaDocenteRepository;

import java.time.LocalDate;
import java.util.List;

public class DeudaDocenteService implements IDeudaDocenteService {

    private final IDeudaDocenteRepository repository;
    private final RiesgoAnalyzer riesgoAnalyzer;

    public DeudaDocenteService(IDeudaDocenteRepository repository) {
        this.repository = repository;
        this.riesgoAnalyzer = new RiesgoAnalyzer();
    }

    @Override
    public List<DeudaDocente> listar() {
        List<DeudaDocente> lista = repository.findAll();
        lista.forEach(this::calcularRiesgo);
        return lista;
    }

    @Override
    public List<DeudaDocente> buscar(String nombreDocente, String tipoDeuda, String situacionLaboral,
                                      LocalDate fechaDesde, LocalDate fechaHasta) {
        List<DeudaDocente> lista = repository.findByFilters(
            nombreDocente, tipoDeuda, situacionLaboral, fechaDesde, fechaHasta);
        lista.forEach(this::calcularRiesgo);
        return lista;
    }

    @Override
    public void crear(DeudaDocente deuda) {
        validar(deuda);
        if (deuda.getEstado() == null || deuda.getEstado().isBlank()) {
            deuda.setEstado("Pendiente");
        }
        repository.save(deuda);
    }

    @Override
    public void actualizar(DeudaDocente deuda) {
        validar(deuda);
        repository.update(deuda);
    }

    @Override
    public void eliminar(int id) {
        repository.delete(id);
    }

    @Override
    public void eliminarSeleccionados(List<Integer> ids) {
        repository.deleteAll(ids);
    }

    @Override
    public void calcularRiesgo(DeudaDocente deuda) {
        deuda.setNivelRiesgo(riesgoAnalyzer.calcularRiesgo(deuda));
    }

    @Override
    public void validar(DeudaDocente d) {
        if (d.getNombreDocente() == null || d.getNombreDocente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del docente es obligatorio.");
        }
        if (d.getNombreDocente().trim().length() > 150) {
            throw new IllegalArgumentException("El nombre del docente no puede superar 150 caracteres.");
        }
        if (d.getDni() == null || d.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del docente es obligatorio.");
        }
        if (!d.getDni().trim().matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos numéricos.");
        }
        if (d.getTipoDeuda() == null || d.getTipoDeuda().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de deuda.");
        }
        if (d.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto de la deuda debe ser mayor a cero.");
        }
        if (d.getFechaDeuda() == null) {
            throw new IllegalArgumentException("Debe indicar la fecha de la deuda.");
        }
        if (d.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("Debe indicar la fecha de vencimiento.");
        }
        if (d.getFechaVencimiento().isBefore(d.getFechaDeuda())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de la deuda.");
        }
        if (d.getSituacionLaboral() == null || d.getSituacionLaboral().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar la situación laboral del docente.");
        }
        if (d.getObservaciones() != null && d.getObservaciones().trim().length() > 300) {
            throw new IllegalArgumentException("Las observaciones no pueden superar 300 caracteres.");
        }
    }
}
