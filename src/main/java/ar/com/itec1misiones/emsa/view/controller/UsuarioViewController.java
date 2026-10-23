package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.dto.UsuarioDTO;
import ar.com.itec1misiones.emsa.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioViewController {

    @FXML private TableView<UsuarioDTO> usuariosTable;
    @FXML private TableColumn<UsuarioDTO, Long> colId;
    @FXML private TableColumn<UsuarioDTO, String> colNombre;
    @FXML private TextField nombreField;
    @FXML private PasswordField contrasenaField;
    @FXML private TextField buscarField;
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
                        mensaje("", false);
                    }
                });
        recargar();
    }

    @FXML
    public void onNuevo() {
        usuariosTable.getSelectionModel().clearSelection();
        nombreField.clear();
        contrasenaField.clear();
        mensaje("", false);
        nombreField.requestFocus();
    }

    @FXML
    public void onBuscar() {
        try {
            List<UsuarioDTO> encontrados = usuarioService.buscarPorNombre(buscarField.getText());
            recargar(encontrados);
            if (encontrados.isEmpty()) {
                mensaje("No se encontraron usuarios con ese nombre.", true);
            } else {
                mensaje(encontrados.size() + " usuario(s) encontrado(s).", false);
            }
        } catch (Exception e) {
            mensaje("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onLimpiarBusqueda() {
        buscarField.clear();
        recargar();
    }

    @FXML
    public void onGuardar() {
        try {
            UsuarioDTO seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                usuarioService.create(nombreField.getText(), contrasenaField.getText());
                mensaje("Usuario creado.", false);
            } else {
                usuarioService.update(seleccionado.getIdUsuario(), nombreField.getText(),
                        contrasenaField.getText());
                mensaje("Usuario actualizado.", false);
            }
            onNuevo();
            recargar();
        } catch (Exception e) {
            mensaje("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onEliminar() {
        UsuarioDTO seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Seleccione un usuario de la tabla.", true);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al usuario \"" + seleccionado.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Eliminar usuario");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .ifPresent(boton -> {
                    try {
                        usuarioService.delete(seleccionado.getIdUsuario());
                        mensaje("Usuario eliminado.", false);
                        onNuevo();
                        recargar();
                    } catch (Exception e) {
                        mensaje("Error: " + e.getMessage(), true);
                    }
                });
    }

    private void recargar() {
        recargar(usuarioService.findAll());
    }

    private void recargar(List<UsuarioDTO> usuarios) {
        usuariosTable.setItems(FXCollections.observableArrayList(usuarios));
    }

    private void mensaje(String texto, boolean error) {
        mensajeLabel.setText(texto);
        mensajeLabel.getStyleClass().removeAll("message-ok", "message-error");
        if (!texto.isEmpty()) {
            mensajeLabel.getStyleClass().add(error ? "message-error" : "message-ok");
        }
    }
}
