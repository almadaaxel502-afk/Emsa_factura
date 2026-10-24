package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integracion contra MySQL: el CRUD de clientes de la pantalla impacta en la base.
 */
@SpringBootTest
@Transactional
public class ClienteCRUDIT {

    @Autowired ClienteService clienteService;
    @Autowired ClienteRepository clientes;

    @Test
    void creaActualizaYBorraUnCliente() {
        ClienteDTO creado = clienteService.create("Roberto", "Fernandez", "30-55554444",
                "Calle Falsa 123", null);

        assertNotNull(creado.getId());
        assertTrue(clientes.findById(creado.getId()).isPresent());

        ClienteDTO actualizado = clienteService.update(creado.getId(), "Roberto Carlos",
                "Fernandez", "30-55554444", "Calle Falsa 456", null);

        assertEquals("Roberto Carlos Fernandez", actualizado.getNombreCompleto());
        assertEquals("Calle Falsa 456", actualizado.getDireccion());

        clienteService.delete(creado.getId());

        assertTrue(clientes.findById(creado.getId()).isEmpty());
    }
}
