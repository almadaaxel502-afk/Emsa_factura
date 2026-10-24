package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.LecturaDTO;
import ar.com.itec1misiones.emsa.entity.Lectura;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.repository.LecturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de lecturas: el operador carga la lectura del medidor y
 * el service calcula el consumo del periodo (diferencia con la lectura anterior).
 */
@Service
public class LecturaService {

    private final LecturaRepository lecturas;
    private final MedidorService medidorService;

    public LecturaService(LecturaRepository lecturas, MedidorService medidorService) {
        this.lecturas = lecturas;
        this.medidorService = medidorService;
    }

    @Transactional
    public Lectura registrarLectura(String nroSerie, LocalDate fecha, BigDecimal valor) {
        Medidor medidor = medidorService.obtenerPorNroSerie(nroSerie);
        if (fecha == null) {
            throw new IllegalArgumentException("Indique la fecha de la lectura.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La lectura debe ser mayor o igual a cero.");
        }
        List<Lectura> historial = lecturas.findByMedidor(medidor.getIdMedidor());
        if (!historial.isEmpty()) {
            Lectura ultima = historial.get(0);
            LocalDate fechaUltima = ultima.getFecha();
            if (fechaUltima != null && !fecha.isAfter(fechaUltima)) {
                throw new IllegalArgumentException("La fecha de la lectura (" + fecha
                        + ") debe ser posterior a la última registrada (" + fechaUltima + ").");
            }
            int comparacion = valor.compareTo(ultima.getValor());
            if (comparacion == 0) {
                throw new IllegalArgumentException("Ya existe una lectura registrada con el valor "
                        + valor.toPlainString() + ".");
            }
            if (comparacion < 0) {
                throw new IllegalArgumentException("La lectura (" + valor.toPlainString()
                        + ") no puede ser menor que la última registrada ("
                        + ultima.getValor().toPlainString() + ").");
            }
        }
        Lectura lectura = new Lectura();
        lectura.setMedidor(medidor);
        lectura.setFecha(fecha);
        lectura.setValor(valor);
        return lecturas.save(lectura);
    }

    /** Registra la lectura y devuelve su DTO (punto de entrada de la vista). */
    @Transactional
    public LecturaDTO registrarLecturaDto(String nroSerie, LocalDate fecha, BigDecimal valor) {
        return toDTO(registrarLectura(nroSerie, fecha, valor));
    }

    /** Mapea la entidad Lectura a su DTO de presentacion. */
    public LecturaDTO toDTO(Lectura lectura) {
        LecturaDTO dto = new LecturaDTO();
        dto.setId(lectura.getIdLectura());
        dto.setFecha(lectura.getFecha());
        dto.setValor(lectura.getValor() != null ? lectura.getValor().doubleValue() : 0);
        if (lectura.getMedidor() != null) {
            dto.setMedidorId(lectura.getMedidor().getIdMedidor());
            dto.setMedidorNroSerie(lectura.getMedidor().getNroSerie());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<Lectura> listarPorMedidor(Long idMedidor) {
        return lecturas.findByMedidor(idMedidor);
    }

    @Transactional(readOnly = true)
    public Optional<Lectura> ultimaLectura(Long idMedidor) {
        List<Lectura> historial = lecturas.findByMedidor(idMedidor);
        return historial.isEmpty() ? Optional.empty() : Optional.of(historial.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<Lectura> lecturaAnterior(Long idMedidor) {
        List<Lectura> historial = lecturas.findByMedidor(idMedidor);
        return historial.size() < 2 ? Optional.empty() : Optional.of(historial.get(1));
    }

    /**
     * Consumo del periodo de la lectura dada: lectura actual menos la anterior.
     * Si no hay lectura anterior (primer registro) se factura la lectura completa.
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularConsumo(Lectura lectura) {
        List<Lectura> historial = lecturas.findByMedidor(lectura.getMedidor().getIdMedidor());
        for (int i = 0; i < historial.size(); i++) {
            Lectura candidata = historial.get(i);
            if (candidata.getIdLectura() != null
                    && candidata.getIdLectura().equals(lectura.getIdLectura())) {
                Lectura anterior = (i + 1 < historial.size()) ? historial.get(i + 1) : null;
                return anterior == null
                        ? lectura.getValor()
                        : lectura.getValor().subtract(anterior.getValor());
            }
        }
        return lectura.getValor();
    }

    /** Consumo del ultimo periodo registrado del medidor (0 si no hay lecturas). */
    @Transactional(readOnly = true)
    public BigDecimal consumoUltimoPeriodo(Long idMedidor) {
        return ultimaLectura(idMedidor)
                .map(this::calcularConsumo)
                .orElse(BigDecimal.ZERO);
    }
}
