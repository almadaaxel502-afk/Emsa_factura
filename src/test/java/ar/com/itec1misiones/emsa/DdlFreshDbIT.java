package ar.com.itec1misiones.emsa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica que Hibernate crea solo el schema en una base vacía (ddl-auto). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3307/emsa_fresh?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"
})
public class DdlFreshDbIT {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void creaLas13TablasAutomaticamente() {
        List<String> tablas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'emsa_fresh'",
                String.class);
        for (String esperada : List.of("usuarios", "tipos_iva", "clientes", "zonas",
                "categorias", "medidores", "dispositivos", "tarifas", "facturas",
                "items_consumo", "items_impuesto", "impuestos", "lecturas")) {
            assertTrue(tablas.contains(esperada), "Falta tabla: " + esperada);
        }
    }
}
