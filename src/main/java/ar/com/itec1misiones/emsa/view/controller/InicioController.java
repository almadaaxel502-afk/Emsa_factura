package ar.com.itec1misiones.emsa.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InicioController {

    @FXML
    private BorderPane root;

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
        cargarCentro("/view/facturacion.fxml");
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
