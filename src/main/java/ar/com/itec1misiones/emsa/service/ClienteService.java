package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.entity.TipoIVA;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import ar.com.itec1misiones.emsa.repository.TipoIVARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de clientes: alta, edicion y baja desde la pantalla de
 * clientes, mas busqueda por nombre y por numero de serie del medidor.
 * Es la unica puerta de entrada del controller a los datos.
 */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final MedidorRepository medidores;
    private final TipoIVARepository tiposIVA;

    public ClienteService(ClienteRepository clientes, MedidorRepository medidores,
                          TipoIVARepository tiposIVA) {
        this.clientes = clientes;
        this.medidores = medidores;
        this.tiposIVA = tiposIVA;
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

    /** Alta de cliente desde la pantalla de clientes. */
    @Transactional
    public ClienteDTO create(String nombre, String apellido, String identificacionNacional,
                             String direccion, Long tipoIVAId) {
        validar(nombre, identificacionNacional, null);
        Cliente cliente = new Cliente();
        aplicar(cliente, nombre, apellido, identificacionNacional, direccion, tipoIVAId);
        return toDTO(clientes.save(cliente));
    }

    /** Edicion de un cliente existente. */
    @Transactional
    public ClienteDTO update(Long id, String nombre, String apellido, String identificacionNacional,
                             String direccion, Long tipoIVAId) {
        Cliente cliente = clientes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con id " + id));
        validar(nombre, identificacionNacional, id);
        aplicar(cliente, nombre, apellido, identificacionNacional, direccion, tipoIVAId);
        return toDTO(clientes.save(cliente));
    }

    /** Baja de cliente; no se puede eliminar si tiene medidores asignados. */
    @Transactional
    public void delete(Long id) {
        Cliente cliente = clientes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con id " + id));
        if (!medidores.findByCliente(id).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar al cliente "
                    + cliente.getNombreCompleto() + ": tiene medidores asignados.");
        }
        clientes.delete(cliente);
    }

    private void aplicar(Cliente cliente, String nombre, String apellido,
                         String identificacionNacional, String direccion, Long tipoIVAId) {
        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido != null && !apellido.isBlank() ? apellido.trim() : null);
        cliente.setIdentificacionNacional(
                identificacionNacional != null && !identificacionNacional.isBlank()
                        ? identificacionNacional.trim() : null);
        cliente.setDireccion(direccion != null && !direccion.isBlank() ? direccion.trim() : null);
        cliente.setTipoIVA(tipoIVAId == null ? null : tiposIVA.findById(tipoIVAId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el tipo de IVA con id " + tipoIVAId)));
    }

    private void validar(String nombre, String identificacionNacional, Long excluyendoId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (identificacionNacional == null || identificacionNacional.trim().isEmpty()) {
            return;
        }
        String identificacion = identificacionNacional.trim();
        boolean duplicado = excluyendoId == null
                ? clientes.existsByIdentificacionNacionalIgnoreCase(identificacion)
                : clientes.existsByIdentificacionNacionalIgnoreCaseAndIdClienteNot(
                        identificacion, excluyendoId);
        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con la identificación " + identificacion + ".");
        }
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
