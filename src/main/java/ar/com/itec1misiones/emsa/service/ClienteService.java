package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de clientes: busqueda por nombre y por numero de serie
 * del medidor. Es la unica puerta de entrada del controller a los datos.
 */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final MedidorRepository medidores;

    public ClienteService(ClienteRepository clientes, MedidorRepository medidores) {
        this.clientes = clientes;
        this.medidores = medidores;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> findAll() {
        return clientes.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> buscarPorNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            return findAll();
        }
        return clientes.buscarPorNombre(texto.trim()).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Long id) {
        return clientes.findById(id);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con id " + id));
    }

    /**
     * Resuelve el cliente a partir del numero de serie del medidor: es el caso
     * en el que el operador solo tiene el medidor en la mano.
     */
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorNroSerieMedidor(String nroSerie) {
        Medidor medidor = medidores.findByNroSerieIgnoreCase(nroSerie == null ? "" : nroSerie.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un medidor con el número de serie '" + nroSerie + "'."));
        Cliente cliente = medidor.getCliente();
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El medidor " + medidor.getNroSerie() + " no tiene cliente asignado.");
        }
        return toDTO(cliente);
    }

    public ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getIdCliente());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setNombreCompleto(cliente.getNombreCompleto());
        dto.setIdentificacionNacional(cliente.getIdentificacionNacional());
        dto.setDireccion(cliente.getDireccion());
        if (cliente.getTipoIVA() != null) {
            dto.setTipoIVAId(cliente.getTipoIVA().getIdTipoIVA());
            dto.setTipoIVADescripcion(cliente.getTipoIVA().getDescripcion());
            dto.setTipoIVATasa(cliente.getTipoIVA().getTasa() != null
                    ? cliente.getTipoIVA().getTasa().doubleValue() : 0);
        }
        if (cliente.getMedidores() != null) {
            cliente.getMedidores().stream()
                    .map(Medidor::getNroSerie)
                    .forEach(dto.getMedidores()::add);
        }
        return dto;
    }
}
