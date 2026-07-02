package com.ucv.lab12.config;

import com.ucv.lab12.controller.DeudaDocenteController;
import com.ucv.lab12.controller.DeudaDocenteFormController;
import com.ucv.lab12.controller.LoginController;
import com.ucv.lab12.repository.DeudaDocenteRepository;
import com.ucv.lab12.repository.IDeudaDocenteRepository;
import com.ucv.lab12.repository.IUsuarioRepository;
import com.ucv.lab12.repository.UsuarioRepository;
import com.ucv.lab12.service.*;

/**
 * Contenedor de Inyección de Dependencias (DI).
 * Instancia y conecta todas las capas: config -> repository -> service -> controller.
 */
public class AppContext {

    private static AppContext instance;

    private final DatabaseConfig dbConfig;
    private final IDeudaDocenteRepository deudaRepository;
    private final IDeudaDocenteService    deudaService;
    private final IUsuarioRepository      usuarioRepository;
    private final IAuthService            authService;

    private AppContext() {
        this.dbConfig          = new DatabaseConfig();
        this.deudaRepository   = new DeudaDocenteRepository(dbConfig);
        this.deudaService      = new DeudaDocenteService(deudaRepository);
        this.usuarioRepository = new UsuarioRepository(dbConfig);
        this.authService       = new AuthService(usuarioRepository);
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /**
     * Factory de controladores para FXMLLoader.setControllerFactory().
     * Inyecta los servicios correspondientes en cada controlador.
     */
    public Object getController(Class<?> type) {
        if (type == DeudaDocenteController.class) {
            return new DeudaDocenteController(deudaService);
        }
        if (type == DeudaDocenteFormController.class) {
            return new DeudaDocenteFormController(deudaService);
        }
        if (type == LoginController.class) {
            return new LoginController(authService);
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el controlador: " + type.getName(), e);
        }
    }

    public IDeudaDocenteService getDeudaDocenteService() {
        return deudaService;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void destroy() {
        dbConfig.close();
    }
}
