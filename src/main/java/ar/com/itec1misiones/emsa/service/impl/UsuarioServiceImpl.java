package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de usuarios (clase padre de Operador). El nombre identifica al usuario
 * y la contrasena se usa para el ingreso al sistema.
 */
@Service
public class UsuarioServiceImpl {

    private final UsuarioRepository usuarios;

    public UsuarioServiceImpl(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<UsuarioDTO> findAll() {
        return usuarios.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return findAll();
        }
        return usuarios.findByNombreContainingIgnoreCaseOrderByIdUsuarioAsc(nombre.trim())
                .stream().map(this::toDTO).toList();
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(usuario.getIdUsuario(), usuario.getNombre());
    }

    public Usuario save(Usuario usuario) {
        return usuarios.save(usuario);
    }

    @Transactional
    public Usuario create(String nombre, String contrasena) {
        validarNombre(nombre, null);
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setContrasena(contrasena != null && !contrasena.isBlank() ? contrasena : null);
        return usuarios.save(u);
    }

    @Transactional
    public Usuario update(Long id, String nombre, String contrasena) {
        Usuario u = usuarios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con id " + id));
        validarNombre(nombre, id);
        u.setNombre(nombre.trim());
        if (contrasena != null && !contrasena.isBlank()) {
            u.setContrasena(contrasena);
        }
        return usuarios.save(u);
    }

    @Transactional
    public void delete(Long id) {
        if (!usuarios.existsById(id)) {
            throw new IllegalArgumentException("No existe el usuario con id " + id);
        }
        usuarios.deleteById(id);
    }

    private void validarNombre(String nombre, Long excluyendoId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        boolean duplicado = usuarios.findAll().stream()
                .anyMatch(u -> (excluyendoId == null || !u.getIdUsuario().equals(excluyendoId))
                        && u.getNombre() != null && u.getNombre().equalsIgnoreCase(nombre.trim()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
    }
}
