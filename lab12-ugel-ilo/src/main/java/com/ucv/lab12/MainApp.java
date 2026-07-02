package com.ucv.lab12;

import com.ucv.lab12.config.AppContext;
import com.ucv.lab12.controller.DeudaDocenteController;
import com.ucv.lab12.controller.LoginController;
import com.ucv.lab12.model.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación. Se muestra primero la pantalla de
 * login (CE2: autenticación de usuarios); solo tras una autenticación
 * exitosa se carga la ventana principal de gestión de deudas.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppContext context = AppContext.getInstance();
        mostrarLogin(stage, context);
    }

    private void mostrarLogin(Stage stage, AppContext context) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/ucv/lab12/login-view.fxml")
        );
        loader.setControllerFactory(context::getController);

        Scene scene = new Scene(loader.load());
        LoginController loginController = loader.getController();
        loginController.setOnLoginExitoso(usuario -> {
            try {
                mostrarVentanaPrincipal(stage, context, usuario);
            } catch (Exception e) {
                throw new RuntimeException("No se pudo abrir la ventana principal", e);
            }
        });

        stage.setTitle("UGEL - Ilo | Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void mostrarVentanaPrincipal(Stage stage, AppContext context, Usuario usuario) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/ucv/lab12/deuda-view.fxml")
        );
        loader.setControllerFactory(context::getController);

        Scene scene = new Scene(loader.load(), 1200, 650);
        DeudaDocenteController controller = loader.getController();
        controller.setUsuarioLogueado(usuario.getNombreUsuario(), usuario.getRol());

        stage.setTitle("UGEL - Ilo | Gestión de Deudas Administrativas de Docentes");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(550);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        AppContext.getInstance().destroy();
    }
}
