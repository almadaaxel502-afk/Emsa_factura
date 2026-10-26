package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.service.impl.LoginServiceImpl;
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

    private final LoginServiceImpl loginServiceImpl;

    public LoginController(LoginServiceImpl loginServiceImpl) {
        this.loginServiceImpl = loginServiceImpl;
    }

    @FXML
    public void initialize() {
        usuarioField.requestFocus();
    }

    @FXML
    public void onLogin() {
        boolean ok = loginServiceImpl.autenticar(usuarioField.getText(), passwordField.getText());
        if (ok) {
            EmsaFxApp.showScene("view/inicio.fxml", "EMSA | Energía de Misiones - Facturación");
        } else {
            mensajeLabel.setText("Usuario o contraseña inválidos.");
        }
    }
}
