package itec.emsa.emsa.view.controller;

import itec.emsa.emsa.JavaFxApp;
import itec.emsa.emsa.service.LoginService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class MenuController {

    @FXML private Label usuarioLabel;

    private final LoginService loginService;

    public MenuController(LoginService loginService) {
        this.loginService = loginService;
    }

    @FXML
    public void initialize() {
        String usuario = loginService.getUsuarioActual();
        usuarioLabel.setText("Usuario: " + (usuario != null ? usuario : "-"));
    }

    @FXML
    public void irAClientes() {
        try {
            JavaFxApp.showScene("/view/clientes.fxml", "EMSA - Clientes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAFacturas() {
        try {
            JavaFxApp.showScene("/view/facturas.fxml", "EMSA - Facturas");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cerrarSesion() {
        loginService.cerrarSesion();
        try {
            JavaFxApp.showScene("/view/login.fxml", "EMSA - Iniciar sesión", 420, 340);
            JavaFxApp.getPrimaryStage().setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
