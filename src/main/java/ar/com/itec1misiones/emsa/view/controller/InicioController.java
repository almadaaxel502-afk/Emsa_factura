package ar.com.itec1misiones.emsa.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InicioController {

    private static final String CLASE_ACTIVA = "nav-button-active";

    @FXML
    private BorderPane root;

    @FXML
    private Button btnFacturacion;

    @FXML
    private Button btnUsuarios;

    private final ApplicationContext context;

    public InicioController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        mostrarFacturacion();
    }

    @FXML
    public void mostrarFacturacion() {
        activar(btnFacturacion);
        cargarCentro("/view/facturacion.fxml");
    }

    @FXML
    public void mostrarUsuarios() {
        activar(btnUsuarios);
        cargarCentro("/view/usuarios.fxml");
    }

    private void activar(Button activo) {
        if (btnFacturacion != null) {
            btnFacturacion.getStyleClass().remove(CLASE_ACTIVA);
        }
        if (btnUsuarios != null) {
            btnUsuarios.getStyleClass().remove(CLASE_ACTIVA);
        }
        if (activo != null && !activo.getStyleClass().contains(CLASE_ACTIVA)) {
            activo.getStyleClass().add(CLASE_ACTIVA);
        }
    }

    private void cargarCentro(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setControllerFactory(context::getBean);
            Parent vista = loader.load();
            root.setCenter(vista);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar " + fxml, e);
        }
    }
}
