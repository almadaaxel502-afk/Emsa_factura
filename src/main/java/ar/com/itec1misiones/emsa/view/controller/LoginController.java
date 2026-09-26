package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.service.LoginService;
import ar.com.itec1misiones.emsa.view.EmsaFxApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    private TextField usuarioField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label mensajeLabel;

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @FXML
    public void onLogin() {
        boolean ok = loginService.autenticar(usuarioField.getText(), passwordField.getText());
        if (ok) {
            EmsaFxApp.showScene("view/inicio.fxml", "EMSA - Facturación");
        } else {
            mensajeLabel.setText("Usuario o contraseña inválidos.");
        }
    }
}
