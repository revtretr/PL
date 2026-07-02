package com.ucv.lab12.repository;

import com.ucv.lab12.config.DatabaseConfig;
import com.ucv.lab12.model.DeudaDocente;
import com.ucv.lab12.util.CryptoUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio JDBC de DeudaDocente. El campo DNI se cifra con AES
 * (CryptoUtil) antes de escribirlo en la base de datos y se descifra al
 * leerlo, de modo que la información confidencial nunca queda en texto
 * plano en el motor de base de datos (CE2).
 */
public class DeudaDocenteRepository implements IDeudaDocenteRepository, AutoCloseable {

    private final DatabaseConfig dbConfig;

    public DeudaDocenteRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public List<DeudaDocente> findAll() {
        return findByFilters(null, null, null, null, null);
    }

    @Override
    public List<DeudaDocente> findByFilters(String nombreDocente, String tipoDeuda,
                                             String situacionLaboral, LocalDate fechaDesde,
                                             LocalDate fechaHasta) {
        String sql = """
                SELECT IdDeuda, NombreDocente, DniEncriptado, TipoDeuda, Monto,
                       FechaDeuda, FechaVencimiento, SituacionLaboral, Estado, Observaciones
                FROM DeudaDocente
                WHERE NombreDocente LIKE ?
                  AND (? = '' OR TipoDeuda = ?)
                  AND (? = '' OR SituacionLaboral = ?)
                  AND (? IS NULL OR FechaDeuda >= ?)
                  AND (? IS NULL OR FechaDeuda <= ?)
                ORDER BY FechaVencimiento ASC
                """;
        List<DeudaDocente> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String tipo = tipoDeuda == null ? "" : tipoDeuda.trim();
            String situacion = situacionLaboral == null ? "" : situacionLaboral.trim();

            ps.setString(1, "%" + (nombreDocente == null ? "" : nombreDocente.trim()) + "%");
            ps.setString(2, tipo);
            ps.setString(3, tipo);
            ps.setString(4, situacion);
            ps.setString(5, situacion);
            setNullableDate(ps, 6, fechaDesde);
            setNullableDate(ps, 7, fechaDesde);
            setNullableDate(ps, 8, fechaHasta);
            setNullableDate(ps, 9, fechaHasta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar deudas de docentes", e);
        }
        return list;
    }

    @Override
    public void save(DeudaDocente d) {
        String sql = """
                INSERT INTO DeudaDocente
                    (NombreDocente, DniEncriptado, TipoDeuda, Monto, FechaDeuda,
                     FechaVencimiento, SituacionLaboral, Estado, Observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCamposComunes(ps, d);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar deuda de docente", e);
        }
    }

    @Override
    public void update(DeudaDocente d) {
        String sql = """
                UPDATE DeudaDocente
                SET NombreDocente     = ?,
                    DniEncriptado     = ?,
                    TipoDeuda         = ?,
                    Monto             = ?,
                    FechaDeuda        = ?,
                    FechaVencimiento  = ?,
                    SituacionLaboral  = ?,
                    Estado            = ?,
                    Observaciones     = ?
                WHERE IdDeuda = ?
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCamposComunes(ps, d);
            ps.setInt(10, d.getIdDeuda());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar deuda de docente", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM DeudaDocente WHERE IdDeuda = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar deuda de docente", e);
        }
    }

    @Override
    public void deleteAll(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder sb = new StringBuilder("DELETE FROM DeudaDocente WHERE IdDeuda IN (");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        sb.append(")");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar deudas en bloque", e);
        }
    }

    // --- helpers ---

    private void bindCamposComunes(PreparedStatement ps, DeudaDocente d) throws SQLException {
        ps.setString(1, d.getNombreDocente());
        ps.setString(2, CryptoUtil.encriptar(nvl(d.getDni())));
        ps.setString(3, nvl(d.getTipoDeuda()));
        ps.setDouble(4, d.getMonto());
        ps.setDate(5, d.getFechaDeuda() != null ? Date.valueOf(d.getFechaDeuda()) : null);
        ps.setDate(6, d.getFechaVencimiento() != null ? Date.valueOf(d.getFechaVencimiento()) : null);
        ps.setString(7, nvl(d.getSituacionLaboral()));
        ps.setString(8, nvl(d.getEstado()));
        ps.setString(9, nvl(d.getObservaciones()));
    }

    private DeudaDocente mapRow(ResultSet rs) throws SQLException {
        Date fechaDeuda = rs.getDate("FechaDeuda");
        Date fechaVenc = rs.getDate("FechaVencimiento");
        String dniEncriptado = rs.getString("DniEncriptado");
        String dniPlano = "";
        try {
            dniPlano = CryptoUtil.desencriptar(dniEncriptado);
        } catch (Exception ignored) {
            // Si el dato antiguo no está cifrado o hay un problema de formato,
            // se muestra vacío en vez de romper la carga de la tabla.
        }

        return new DeudaDocente(
            rs.getInt("IdDeuda"),
            rs.getString("NombreDocente"),
            dniPlano,
            rs.getString("TipoDeuda"),
            rs.getDouble("Monto"),
            fechaDeuda != null ? fechaDeuda.toLocalDate() : null,
            fechaVenc != null ? fechaVenc.toLocalDate() : null,
            rs.getString("SituacionLaboral"),
            rs.getString("Estado"),
            rs.getString("Observaciones")
        );
    }

    private void setNullableDate(PreparedStatement ps, int index, LocalDate fecha) throws SQLException {
        if (fecha == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(fecha));
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public void close() {
        dbConfig.close();
    }
}
