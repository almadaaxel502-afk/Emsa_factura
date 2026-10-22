package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.entity.Lectura;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.repository.LecturaRepository;
import ar.com.itec1misiones.emsa.service.LecturaService;
import ar.com.itec1misiones.emsa.service.MedidorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LecturaServiceTest {

    @Mock private LecturaRepository lecturas;
    @Mock private MedidorService medidorService;
    @InjectMocks private LecturaService lecturaService;

    private Medidor medidor() {
        Medidor medidor = new Medidor();
        medidor.setIdMedidor(1L);
        medidor.setNroSerie("SER-0001");
        medidor.setNumeroConexion("CON-0001");
        medidor.setDireccion("Av. Siempreviva 742");
        return medidor;
    }

    private Lectura lectura(Long id, Medidor medidor, LocalDate fecha, String valor) {
        Lectura lectura = new Lectura();
        lectura.setIdLectura(id);
        lectura.setMedidor(medidor);
        lectura.setFecha(fecha);
        lectura.setValor(new BigDecimal(valor));
        return lectura;
    }

    @Test
    void registraLaLecturaDelOperador() {
        Medidor medidor = medidor();
        when(medidorService.obtenerPorNroSerie("SER-0001")).thenReturn(medidor);
        when(lecturas.findByMedidor(1L)).thenReturn(List.of());
        when(lecturas.save(any(Lectura.class))).thenAnswer(i -> i.getArgument(0));

        Lectura guardada = lecturaService.registrarLectura("SER-0001", LocalDate.now(), new BigDecimal("1290"));

        assertEquals(0, new BigDecimal("1290").compareTo(guardada.getValor()));
        assertEquals(medidor, guardada.getMedidor());
    }

    @Test
    void rechazaLecturaMenorALaUltimaRegistrada() {
        Medidor medidor = medidor();
        when(medidorService.obtenerPorNroSerie("SER-0001")).thenReturn(medidor);
        when(lecturas.findByMedidor(1L)).thenReturn(List.of(
                lectura(2L, medidor, LocalDate.now().minusDays(5), "1290")));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                lecturaService.registrarLectura("SER-0001", LocalDate.now(), new BigDecimal("1200")));
        assertTrue(e.getMessage().contains("no puede ser menor"));
    }

    @Test
    void rechazaLecturaRepetida() {
        Medidor medidor = medidor();
        when(medidorService.obtenerPorNroSerie("SER-0001")).thenReturn(medidor);
        when(lecturas.findByMedidor(1L)).thenReturn(List.of(
                lectura(2L, medidor, LocalDate.now().minusDays(5), "1290")));

        assertThrows(IllegalArgumentException.class, () ->
                lecturaService.registrarLectura("SER-0001", LocalDate.now(), new BigDecimal("1290")));
    }

    @Test
    void rechazaLecturaNegativa() {
        Medidor medidor = medidor();
        when(medidorService.obtenerPorNroSerie("SER-0001")).thenReturn(medidor);

        assertThrows(IllegalArgumentException.class, () ->
                lecturaService.registrarLectura("SER-0001", LocalDate.now(), new BigDecimal("-1")));
    }

    @Test
    void calculaElConsumoComoDiferenciaConLaLecturaAnterior() {
        Medidor medidor = medidor();
        Lectura actual = lectura(2L, medidor, LocalDate.now().minusDays(5), "1290");
        Lectura anterior = lectura(1L, medidor, LocalDate.now().minusDays(40), "1200");
        when(lecturas.findByMedidor(1L)).thenReturn(List.of(actual, anterior));

        BigDecimal consumo = lecturaService.calcularConsumo(actual);

        assertEquals(0, new BigDecimal("90").compareTo(consumo));
    }

    @Test
    void primerRegistroSeFacturaCompleto() {
        Medidor medidor = medidor();
        Lectura primera = lectura(1L, medidor, LocalDate.now().minusDays(40), "1200");
        when(lecturas.findByMedidor(1L)).thenReturn(List.of(primera));

        BigDecimal consumo = lecturaService.calcularConsumo(primera);

        assertEquals(0, new BigDecimal("1200").compareTo(consumo));
    }
}
