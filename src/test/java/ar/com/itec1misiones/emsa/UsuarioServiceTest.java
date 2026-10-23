package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import ar.com.itec1misiones.emsa.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarios;
    @InjectMocks private UsuarioService usuarioService;

    @Test
    void devuelveUsuariosComoDTO() {
        Usuario demo = new Usuario();
        demo.setIdUsuario(3L);
        demo.setNombre("demo");
        when(usuarios.findAll()).thenReturn(List.of(demo));

        List<UsuarioDTO> lista = usuarioService.findAll();

        assertEquals(1, lista.size());
        assertEquals(3L, lista.get(0).getIdUsuario());
        assertEquals("demo", lista.get(0).getNombre());
    }
}
