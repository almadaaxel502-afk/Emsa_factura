package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.dto.TipoIVADTO;
import ar.com.itec1misiones.emsa.service.ClienteService;
import ar.com.itec1misiones.emsa.service.TipoIVAService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pantalla de clientes: alta, edicion y baja de clientes.
 * Toda la logica de negocio vive en {@link ClienteService}.
 */
@Component
public class ClienteViewController {

    @FXML private TableView<ClienteDTO> clientesTable;
    @FXML private TableColumn<ClienteDTO, Long> colId;
    @FXML private TableColumn<ClienteDTO, String> colNombre;
    @FXML private TableColumn<ClienteDTO, String> colApellido;
    @FXML private TableColumn<ClienteDTO, String> colDni;
    @FXML private TableColumn<ClienteDTO, String> colDireccion;
    @FXML private TableColumn<ClienteDTO, String> colIva;

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField dniField;
    @FXML private TextField direccionField;
    @FXML private ComboBox<TipoIVADTO> tipoIVACombo;
    @FXML private TextField buscarField;
    @FXML private Label mensajeLabel;

    private final ClienteService clienteService;
    private final TipoIVAService tipoIVAService;

    public ClienteViewController(ClienteService clienteService, TipoIVAService tipoIVAService) {
        this.clienteService = clienteService;
        this.tipoIVAService = tipoIVAService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("identificacionNacional"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("tipoIVADescripcion"));

        clientesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    if (actual != null) {
                        nombreField.setText(actual.getNombre());
                        apellidoField.setText(actual.getApellido());
                        dniField.setText(actual.getIdentificacionNacional());
                        direccionField.setText(actual.getDireccion());
                        seleccionarTipoIVA(actual.getTipoIVAId());
                        mensaje("", false);
                    }
                });

        tipoIVACombo.setItems(FXCollections.observableArrayList(tipoIVAService.findAll()));
        recargar();
    }

    @FXML
    public void onNuevo() {
        clientesTable.getSelectionModel().clearSelection();
        nombreField.clear();
        apellidoField.clear();
        dniField.clear();
        direccionField.clear();
        tipoIVACombo.getSelectionModel().clearSelection();
        mensaje("", false);
        nombreField.requestFocus();
    }

    @FXML
    public void onBuscar() {
        try {
            List<ClienteDTO> encontrados = clienteService.buscarPorNombre(buscarField.getText());
            recargar(encontrados);
            if (encontrados.isEmpty()) {
                mensaje("No se encontraron clientes con ese dato.", true);
            } else {
                mensaje(encontrados.size() + " cliente(s) encontrado(s).", false);
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
            Long tipoIVAId = tipoIVACombo.getValue() != null ? tipoIVACombo.getValue().getId() : null;
            ClienteDTO seleccionado = clientesTable.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                clienteService.create(nombreField.getText(), apellidoField.getText(),
                        dniField.getText(), direccionField.getText(), tipoIVAId);
                mensaje("Cliente creado.", false);
            } else {
                clienteService.update(seleccionado.getId(), nombreField.getText(),
                        apellidoField.getText(), dniField.getText(), direccionField.getText(),
                        tipoIVAId);
                mensaje("Cliente actualizado.", false);
            }
            onNuevo();
            recargar();
        } catch (Exception e) {
            mensaje("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void onEliminar() {
        ClienteDTO seleccionado = clientesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Seleccione un cliente de la tabla.", true);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al cliente \"" + seleccionado.getNombreCompleto() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Eliminar cliente");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .ifPresent(boton -> {
                    try {
                        clienteService.delete(seleccionado.getId());
                        mensaje("Cliente eliminado.", false);
                        onNuevo();
                        recargar();
                    } catch (Exception e) {
                        mensaje("Error: " + e.getMessage(), true);
                    }
                });
    }

    private void seleccionarTipoIVA(Long tipoIVAId) {
        if (tipoIVAId == null) {
            tipoIVACombo.getSelectionModel().clearSelection();
            return;
        }
        tipoIVACombo.getItems().stream()
                .filter(tipo -> tipoIVAId.equals(tipo.getId()))
                .findFirst()
                .ifPresentOrElse(tipoIVACombo.getSelectionModel()::select,
                        () -> tipoIVACombo.getSelectionModel().clearSelection());
    }

    private void recargar() {
        recargar(clienteService.findAll());
    }

    private void recargar(List<ClienteDTO> clientes) {
        clientesTable.setItems(FXCollections.observableArrayList(clientes));
    }

    private void mensaje(String texto, boolean error) {
        mensajeLabel.setText(texto);
        mensajeLabel.getStyleClass().removeAll("message-ok", "message-error");
        if (!texto.isEmpty()) {
            mensajeLabel.getStyleClass().add(error ? "message-error" : "message-ok");
        }
    }
}
