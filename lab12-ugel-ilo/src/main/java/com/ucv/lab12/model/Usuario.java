package com.ucv.lab12.model;

/**
 * Entidad Usuario para el módulo de autenticación (CE2: niveles de
 * seguridad de la aplicación en cuanto a autenticación de usuarios).
 *
 * La contraseña NUNCA se guarda en texto plano: se almacena únicamente
 * el hash SHA-256 (con salt único) generado por util.PasswordUtil.
 */
public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String passwordHash;
    private String salt;
    private String rol;      // "ADMINISTRADOR" | "AUDITOR"
    private boolean activo;

    public Usuario() {}

    public Usuario(int idUsuario, String nombreUsuario, String passwordHash,
                    String salt, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.rol = rol;
        this.activo = activo;
    }

    public int getIdUsuario()            { return idUsuario; }
    public String getNombreUsuario()     { return nombreUsuario; }
    public String getPasswordHash()      { return passwordHash; }
    public String getSalt()              { return salt; }
    public String getRol()               { return rol; }
    public boolean isActivo()            { return activo; }

    public void setIdUsuario(int idUsuario)             { this.idUsuario = idUsuario; }
    public void setNombreUsuario(String nombreUsuario)  { this.nombreUsuario = nombreUsuario; }
    public void setPasswordHash(String passwordHash)    { this.passwordHash = passwordHash; }
    public void setSalt(String salt)                    { this.salt = salt; }
    public void setRol(String rol)                      { this.rol = rol; }
    public void setActivo(boolean activo)                { this.activo = activo; }
}
