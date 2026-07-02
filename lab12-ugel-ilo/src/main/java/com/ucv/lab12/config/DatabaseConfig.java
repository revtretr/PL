package com.ucv.lab12.config;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class DatabaseConfig implements AutoCloseable {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=ugel_ilo_deudas;"
            + "user=adm;"
            + "password=123456;"
            + "trustServerCertificate=true;"
            + "encrypt=true;";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    @Override
    public void close() {
        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                    // Ignorar errores individuales al cerrar drivers
                }
            }
        } catch (Exception ignored) {
            // No se pudo limpiar drivers, pero la aplicación puede continuar
        }
    }
}
