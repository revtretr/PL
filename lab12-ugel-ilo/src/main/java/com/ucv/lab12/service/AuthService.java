package com.ucv.lab12.service;

import com.ucv.lab12.model.Usuario;
import com.ucv.lab12.repository.IUsuarioRepository;
import com.ucv.lab12.util.PasswordUtil;

import java.util.Optional;


public class AuthService implements IAuthService {

    private final IUsuarioRepository usuarioRepository;

    public AuthService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> autenticar(String nombreUsuario, String password) {
        if (nombreUsuario == null || nombreUsuario.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario.trim());
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.isActivo()) {
            return Optional.empty();
        }

        boolean valido = PasswordUtil.verificar(password, usuario.getSalt(), usuario.getPasswordHash());
        return valido ? Optional.of(usuario) : Optional.empty();
    }
}
