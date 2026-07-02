package com.ucv.lab12.repository;

import com.ucv.lab12.config.DatabaseConfig;
import com.ucv.lab12.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioRepository implements IUsuarioRepository {

    private final DatabaseConfig dbConfig;

    public UsuarioRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
        String sql = """
                SELECT IdUsuario, NombreUsuario, PasswordHash, Salt, Rol, Activo
                FROM Usuario
                WHERE NombreUsuario = ?
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario == null ? "" : nombreUsuario.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                        rs.getInt("IdUsuario"),
                        rs.getString("NombreUsuario"),
                        rs.getString("PasswordHash"),
                        rs.getString("Salt"),
                        rs.getString("Rol"),
                        rs.getBoolean("Activo")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar usuario", e);
        }
    }
}
