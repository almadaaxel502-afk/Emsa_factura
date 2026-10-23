package ar.com.itec1misiones.emsa.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Vista de un cliente para la pantalla y la API. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClienteDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String identificacionNacional;
    private String direccion;
    private Long tipoIVAId;
    private String tipoIVADescripcion;
    private double tipoIVATasa;
    private List<String> medidores = new ArrayList<>();

    @Override
    public String toString() {
        return nombreCompleto != null && !nombreCompleto.isBlank()
                ? nombreCompleto
                : (nombre != null ? nombre : "");
    }
}
