package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.entity.Operador;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.OperadorRepository;
import ar.com.itec1misiones.emsa.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock private OperadorRepository operadores;
    @InjectMocks private UsuarioService usuarioService;

    @Test
    void devuelveOperadoresComoDTO() {
        Operador demo = new Operador();
        demo.setIdUsuario(3L);
        demo.setNombre("demo");
        when(operadores.findAll()).thenReturn(List.of(demo));

        List<UsuarioDTO> lista = usuarioService.findAll();

        assertEquals(1, lista.size());
        assertEquals(3L, lista.get(0).getIdUsuario());
        assertEquals("demo", lista.get(0).getNombre());
    }

    @Test
    void buscaOperadoresPorNombreYDevuelveDTOs() {
        Operador demo = new Operador();
        demo.setIdUsuario(3L);
        demo.setNombre("demo");
        when(operadores.findByNombreContainingIgnoreCaseOrderByIdUsuarioAsc("de"))
                .thenReturn(List.of(demo));

        List<UsuarioDTO> lista = usuarioService.buscarPorNombre(" de ");

        assertEquals(1, lista.size());
        assertEquals("demo", lista.get(0).getNombre());
    }

    @Test
    void creaUnOperadorParaQuePuedaIniciarSesion() {
        when(operadores.findAll()).thenReturn(List.of());
        when(operadores.save(any(Operador.class))).thenAnswer(i -> i.getArgument(0));

        Usuario creado = usuarioService.create("nuevo", "secreta");

        assertTrue(creado instanceof Operador);
        assertEquals("nuevo", creado.getNombre());
        assertEquals("secreta", creado.getContrasena());
    }

    @Test
    void rechazaCrearOperadorSinContrasena() {
        when(operadores.findAll()).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.create("nuevo", " "));

        assertTrue(error.getMessage().contains("contrasena"));
    }

    @Test
    void rechazaCrearOperadorConNombreDuplicado() {
        Operador existente = new Operador();
        existente.setIdUsuario(1L);
        existente.setNombre("demo");
        when(operadores.findAll()).thenReturn(List.of(existente));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.create("DEMO", "secreta"));

        assertTrue(error.getMessage().contains("Ya existe"));
    }
}
