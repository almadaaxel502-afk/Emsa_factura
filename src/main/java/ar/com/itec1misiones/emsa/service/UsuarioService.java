package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.entity.Operador;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.OperadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de operadores: son los unicos usuarios que pueden ingresar al sistema.
 * Los titulares de clientes se guardan como {@link Usuario}, pero no se administran
 * desde esta pantalla ni participan del login.
 */
@Service
public class UsuarioService {

    private final OperadorRepository operadores;

    public UsuarioService(OperadorRepository operadores) {
        this.operadores = operadores;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return operadores.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            return findAll();
        }
        return operadores.findByNombreContainingIgnoreCaseOrderByIdUsuarioAsc(texto.trim())
                .stream().map(this::toDTO).toList();
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(usuario.getIdUsuario(), usuario.getNombre());
    }

    @Transactional
    public Operador create(String nombre, String contrasena) {
        validarNombre(nombre, null);
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria.");
        }
        Operador operador = new Operador();
        operador.setNombre(nombre.trim());
        operador.setContrasena(contrasena);
        return operadores.save(operador);
    }

    @Transactional
    public Operador update(Long id, String nombre, String contrasena) {
        Operador operador = operadores.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el operador con id " + id));
        validarNombre(nombre, id);
        operador.setNombre(nombre.trim());
        if (contrasena != null && !contrasena.isBlank()) {
            operador.setContrasena(contrasena);
        }
        return operadores.save(operador);
    }

    @Transactional
    public void delete(Long id) {
        if (!operadores.existsById(id)) {
            throw new IllegalArgumentException("No existe el operador con id " + id);
        }
        operadores.deleteById(id);
    }

    private void validarNombre(String nombre, Long excluyendoId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        boolean duplicado = operadores.findAll().stream()
                .anyMatch(u -> (excluyendoId == null || !u.getIdUsuario().equals(excluyendoId))
                        && u.getNombre() != null && u.getNombre().equalsIgnoreCase(nombre.trim()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un operador con ese nombre.");
        }
    }
}
