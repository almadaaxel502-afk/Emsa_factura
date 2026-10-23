package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.entity.Operador;
import ar.com.itec1misiones.emsa.repository.OperadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Ingreso al sistema: solo los Operadores (heredan de Usuario) pueden autenticarse. */
@Service
public class LoginService {

    private final OperadorRepository operadores;

    public LoginService(OperadorRepository operadores) {
        this.operadores = operadores;
    }

    @Transactional(readOnly = true)
    public boolean autenticar(String nombre, String contrasena) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        Optional<Operador> operador = operadores.findByNombreIgnoreCase(nombre.trim());
        if (operador.isEmpty()) {
            return false;
        }
        String clave = operador.get().getContrasena();
        return clave != null && clave.equals(contrasena == null ? "" : contrasena);
    }
}
