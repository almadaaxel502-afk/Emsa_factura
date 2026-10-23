package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

@Component
public class UsuarioViewController {

    @FXML private TableView<UsuarioDTO> usuariosTable;
    @FXML private TableColumn<UsuarioDTO, Long> colId;
    @FXML private TableColumn<UsuarioDTO, String> colNombre;
    @FXML private TextField nombreField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label mensajeLabel;

    private final UsuarioService usuarioService;

    public UsuarioViewController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        usuariosTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    if (actual != null) {
                        nombreField.setText(actual.getNombre());
                        contrasenaField.clear();
                        mensajeLabel.setText("");
                    }
                });
        recargar();
    }

    @FXML
    public void onNuevo() {
        usuariosTable.getSelectionModel().clearSelection();
        nombreField.clear();
        contrasenaField.clear();
        mensajeLabel.setText("");
        nombreField.requestFocus();
    }

    @FXML
    public void onGuardar() {
        try {
            UsuarioDTO seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                usuarioService.create(nombreField.getText(), contrasenaField.getText());
                mensajeLabel.setText("Usuario creado.");
            } else {
                usuarioService.update(seleccionado.getIdUsuario(), nombreField.getText(),
                        contrasenaField.getText());
                mensajeLabel.setText("Usuario actualizado.");
            }
            onNuevo();
            recargar();
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        try {
            UsuarioDTO seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                mensajeLabel.setText("Seleccione un usuario de la tabla.");
                return;
            }
            usuarioService.delete(seleccionado.getIdUsuario());
            mensajeLabel.setText("Usuario eliminado.");
            onNuevo();
            recargar();
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    private void recargar() {
        usuariosTable.setItems(FXCollections.observableArrayList(usuarioService.findAll()));
    }
}
