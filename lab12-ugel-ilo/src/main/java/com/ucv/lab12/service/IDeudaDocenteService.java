package com.ucv.lab12.service;

import com.ucv.lab12.model.DeudaDocente;
import java.time.LocalDate;
import java.util.List;

public interface IDeudaDocenteService {
    List<DeudaDocente> listar();

    List<DeudaDocente> buscar(String nombreDocente, String tipoDeuda, String situacionLaboral,
                               LocalDate fechaDesde, LocalDate fechaHasta);

    void crear(DeudaDocente deuda);
    void actualizar(DeudaDocente deuda);
    void eliminar(int id);
    void eliminarSeleccionados(List<Integer> ids);
    void validar(DeudaDocente deuda);

    /** Calcula y asigna el nivel de riesgo (BAJO/MEDIO/ALTO) de una deuda. */
    void calcularRiesgo(DeudaDocente deuda);
}
