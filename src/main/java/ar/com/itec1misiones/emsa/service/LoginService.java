package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository usuarios;

    public LoginService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public boolean autenticar(String nombre, String password) {
        if (nombre == null || password == null) {
            return false;
        }
        return usuarios.findByNombre(nombre.trim())
                .map(u -> password.equals(u.getPassword()))
                .orElse(false);
    }
}
