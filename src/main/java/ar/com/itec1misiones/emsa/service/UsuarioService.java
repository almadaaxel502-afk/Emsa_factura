package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<Usuario> findAll() {
        return usuarios.findAll();
    }

    public Usuario save(Usuario usuario) {
        return usuarios.save(usuario);
    }

    @Transactional
    public Usuario create(String nombre) {
        validarNombre(nombre, null);
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        return usuarios.save(u);
    }

    @Transactional
    public Usuario update(Long id, String nombre) {
        Usuario u = usuarios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con id " + id));
        validarNombre(nombre, id);
        u.setNombre(nombre.trim());
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
