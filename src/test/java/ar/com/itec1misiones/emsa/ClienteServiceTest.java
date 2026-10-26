package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.entity.TipoIVA;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import ar.com.itec1misiones.emsa.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock private ClienteRepository clientes;
    @Mock private MedidorRepository medidores;
    @InjectMocks private ClienteServiceImpl clienteServiceImpl;

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

        List<ClienteDTO> resultado = clienteServiceImpl.buscarPorNombre("juan");

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

        ClienteDTO resultado = clienteServiceImpl.buscarPorNroSerieMedidor("SER-0001");

        assertEquals("Juan Giron", resultado.getNombreCompleto());
        assertEquals(21.0, resultado.getTipoIVATasa());
    }

    @Test
    void fallaSiElMedidorNoExiste() {
        when(medidores.findByNroSerieIgnoreCase("SER-9999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> clienteServiceImpl.buscarPorNroSerieMedidor("SER-9999"));
    }

    @Test
    void fallaSiElMedidorNoTieneCliente() {
        Medidor medidor = new Medidor();
        medidor.setIdMedidor(2L);
        medidor.setNroSerie("SER-0003");
        when(medidores.findByNroSerieIgnoreCase("SER-0003")).thenReturn(Optional.of(medidor));

        assertThrows(IllegalArgumentException.class,
                () -> clienteServiceImpl.buscarPorNroSerieMedidor("SER-0003"));
    }
}
