package com.ucv.lab12.repository;

import com.ucv.lab12.model.DeudaDocente;
import java.time.LocalDate;
import java.util.List;

public interface IDeudaDocenteRepository {
    List<DeudaDocente> findAll();

    List<DeudaDocente> findByFilters(String nombreDocente, String tipoDeuda,
                                      String situacionLaboral, LocalDate fechaDesde,
                                      LocalDate fechaHasta);

    void save(DeudaDocente deuda);
    void update(DeudaDocente deuda);
    void delete(int id);
    void deleteAll(List<Integer> ids);
}
