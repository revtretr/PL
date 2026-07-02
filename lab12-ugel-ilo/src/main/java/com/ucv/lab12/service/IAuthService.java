package com.ucv.lab12.service;

import com.ucv.lab12.model.Usuario;
import java.util.Optional;

public interface IAuthService {
    Optional<Usuario> autenticar(String nombreUsuario, String password);
}
