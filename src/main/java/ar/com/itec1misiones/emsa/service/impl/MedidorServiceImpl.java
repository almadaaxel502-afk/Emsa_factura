package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.MedidorDTO;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Logica de negocio de medidores. */
@Service
public class MedidorServiceImpl {

    private final MedidorRepository medidores;

    public MedidorServiceImpl(MedidorRepository medidores) {
        this.medidores = medidores;
    }

    @Transactional(readOnly = true)
    public Optional<Medidor> buscarPorNroSerie(String nroSerie) {
        return medidores.findByNroSerieIgnoreCase(nroSerie == null ? "" : nroSerie.trim());
    }

    /** Busca el medidor por numero de serie; lanza error de negocio si no existe. */
    @Transactional(readOnly = true)
    public Medidor obtenerPorNroSerie(String nroSerie) {
        return buscarPorNroSerie(nroSerie)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un medidor con el número de serie '" + nroSerie + "'."));
    }

    @Transactional(readOnly = true)
    public List<MedidorDTO> listarPorCliente(Long idCliente) {
        return medidores.findByCliente(idCliente).stream().map(this::toDTO).toList();
    }

    public MedidorDTO toDTO(Medidor medidor) {
        MedidorDTO dto = new MedidorDTO();
        dto.setId(medidor.getIdMedidor());
        dto.setNroSerie(medidor.getNroSerie());
        dto.setNumeroConexion(medidor.getNumeroConexion());
        dto.setDireccion(medidor.getDireccion());
        if (medidor.getZona() != null) {
            dto.setZona(medidor.getZona().getCodigo() + " - " + medidor.getZona().getDescripcion());
        }
        if (medidor.getCategoria() != null) {
            dto.setCategoria(medidor.getCategoria().getCodigo() + " - " + medidor.getCategoria().getDescripcion());
        }
        if (medidor.getDispositivo() != null) {
            dto.setDispositivo(medidor.getDispositivo().getDescripcion());
        }
        if (medidor.getCliente() != null) {
            dto.setClienteId(medidor.getCliente().getIdCliente());
            dto.setClienteNombre(medidor.getCliente().getNombreCompleto());
        }
        return dto;
    }
}
