package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.entity.TipoIVA;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import ar.com.itec1misiones.emsa.repository.TipoIVARepository;
import ar.com.itec1misiones.emsa.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock private ClienteRepository clientes;
    @Mock private MedidorRepository medidores;
    @Mock private TipoIVARepository tiposIVA;
    @InjectMocks private ClienteService clienteService;

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Giron");
        cliente.setIdentificacionNacional("20-12345678");
        cliente.setDireccion("Av. Siempreviva 742");
        cliente.setTipoIVA(new TipoIVA(1L, "Consumidor final", new BigDecimal("21")));
        return cliente;
    }

    @Test
    void buscaClientePorNombre() {
        when(clientes.buscarPorNombre("juan")).thenReturn(List.of(cliente()));

        List<ClienteDTO> resultado = clienteService.buscarPorNombre("juan");

        assertEquals(1, resultado.size());
        assertEquals("Juan Giron", resultado.get(0).getNombreCompleto());
    }

    @Test
    void buscaClientePorNumeroDeSerieDelMedidor() {
        Cliente cliente = cliente();
        Medidor medidor = new Medidor();
        medidor.setIdMedidor(2L);
        medidor.setNroSerie("SER-0001");
        medidor.setCliente(cliente);
        when(medidores.findByNroSerieIgnoreCase("SER-0001")).thenReturn(Optional.of(medidor));

        ClienteDTO resultado = clienteService.buscarPorNroSerieMedidor("SER-0001");

        assertEquals("Juan Giron", resultado.getNombreCompleto());
        assertEquals(21.0, resultado.getTipoIVATasa());
    }

    @Test
    void fallaSiElMedidorNoExiste() {
        when(medidores.findByNroSerieIgnoreCase("SER-9999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> clienteService.buscarPorNroSerieMedidor("SER-9999"));
    }

    @Test
    void fallaSiElMedidorNoTieneCliente() {
        Medidor medidor = new Medidor();
        medidor.setIdMedidor(2L);
        medidor.setNroSerie("SER-0003");
        when(medidores.findByNroSerieIgnoreCase("SER-0003")).thenReturn(Optional.of(medidor));

        assertThrows(IllegalArgumentException.class,
                () -> clienteService.buscarPorNroSerieMedidor("SER-0003"));
    }

    @Test
    void creaUnClienteConTipoIVA() {
        TipoIVA consumidorFinal = new TipoIVA(1L, "Consumidor final", new BigDecimal("21"));
        when(tiposIVA.findById(1L)).thenReturn(Optional.of(consumidorFinal));
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> {
            Cliente guardado = i.getArgument(0);
            guardado.setIdCliente(9L);
            return guardado;
        });

        ClienteDTO creado = clienteService.create("Ana", "Duarte", "27-98765432",
                "Av. Corrientes 1234", 1L);

        assertEquals(9L, creado.getId());
        assertEquals("Ana Duarte", creado.getNombreCompleto());
        assertEquals("27-98765432", creado.getIdentificacionNacional());
        assertEquals("Consumidor final", creado.getTipoIVADescripcion());
        assertEquals(21.0, creado.getTipoIVATasa());
    }

    @Test
    void rechazaCrearClienteSinNombre() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.create(" ", "Duarte", null, null, null));

        assertTrue(error.getMessage().contains("obligatorio"));
    }

    @Test
    void rechazaCrearClienteConIdentificacionDuplicada() {
        when(clientes.existsByIdentificacionNacionalIgnoreCase("20-12345678")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.create("Pedro", "Gomez", "20-12345678", null, null));

        assertTrue(error.getMessage().contains("Ya existe"));
    }

    @Test
    void actualizaUnClienteExistente() {
        Cliente existente = cliente();
        TipoIVA comercio = new TipoIVA(2L, "Comercio registrado", new BigDecimal("10.5"));
        when(clientes.findById(1L)).thenReturn(Optional.of(existente));
        when(tiposIVA.findById(2L)).thenReturn(Optional.of(comercio));
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        ClienteDTO actualizado = clienteService.update(1L, "Juan", "Perez",
                "20-12345678", "Av. Siempreviva 100", 2L);

        assertEquals("Juan Perez", actualizado.getNombreCompleto());
        assertEquals("Av. Siempreviva 100", actualizado.getDireccion());
        assertEquals("Comercio registrado", actualizado.getTipoIVADescripcion());
    }

    @Test
    void noEliminaClientesConMedidoresAsignados() {
        Cliente cliente = cliente();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(medidores.findByCliente(1L)).thenReturn(List.of(new Medidor()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.delete(1L));

        assertTrue(error.getMessage().contains("medidores"));
    }

    @Test
    void eliminaClientesSinMedidores() {
        Cliente cliente = cliente();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(medidores.findByCliente(1L)).thenReturn(List.of());

        clienteService.delete(1L);

        verify(clientes).delete(cliente);
    }
}
