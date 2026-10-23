package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/** Vista de un medidor. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MedidorDTO {

    private Long id;
    private String nroSerie;
    private String numeroConexion;
    private String direccion;
    private String zona;
    private String categoria;
    private String dispositivo;
    private Long clienteId;
    private String clienteNombre;

    @Override
    public String toString() {
        return nroSerie != null ? nroSerie : "";
    }
}
