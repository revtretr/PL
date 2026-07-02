package com.ucv.lab12.controller;

import com.ucv.lab12.model.Usuario;
import com.ucv.lab12.service.IAuthService;
import com.ucv.lab12.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class LoginController implements Initializable {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnIngresar;

    private final IAuthService authService;
    private Consumer<Usuario> onLoginExitoso;

    public LoginController(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setVisible(false);
        txtPassword.setOnAction(e -> onIngresar());
        txtUsuario.setOnAction(e -> onIngresar());
    }

    public void setOnLoginExitoso(Consumer<Usuario> callback) {
        this.onLoginExitoso = callback;
    }

    @FXML
    private void onIngresar() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario == null || usuario.isBlank() || password == null || password.isEmpty()) {
            mostrarError("Ingrese usuario y contraseña.");
            return;
        }

        try {
            Optional<Usuario> resultado = authService.autenticar(usuario, password);
            if (resultado.isPresent()) {
                lblError.setVisible(false);
                if (onLoginExitoso != null) {
                    onLoginExitoso.accept(resultado.get());
                }
            } else {
                mostrarError("Usuario o contraseña incorrectos.");
                txtPassword.clear();
            }
        } catch (Exception e) {
            AlertUtil.error("Error de conexión",
                "No se pudo validar las credenciales:\n" + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
