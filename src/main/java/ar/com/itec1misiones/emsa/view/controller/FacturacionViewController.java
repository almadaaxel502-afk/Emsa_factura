package ar.com.itec1misiones.emsa.view.controller;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;
@Component
public class FacturacionViewController {
  @FXML private ComboBox<Cliente> clienteCombo;
  @FXML private TextField kwhField;
  @FXML private Label mensajeLabel;
  @FXML private TableView<FacturaDTO> facturasTable;
  @FXML private TableColumn<FacturaDTO, Integer> colId;
  @FXML private TableColumn<FacturaDTO, Integer> colUsuario;
  @FXML private TableColumn<FacturaDTO, Double> colKwh;
  @FXML private TableColumn<FacturaDTO, Double> colTotal;
  private final ClienteRepository clientes;
  private final FacturacionService facturacion;
  private ObservableList<Cliente> todosLosClientes;
  public FacturacionViewController(ClienteRepository clientes, FacturacionService facturacion) { this.clientes = clientes; this.facturacion = facturacion; }
  @FXML public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
    colKwh.setCellValueFactory(new PropertyValueFactory<>("kwhTotales"));
    colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    todosLosClientes = FXCollections.observableArrayList(clientes.findAll());
    clienteCombo.setItems(FXCollections.observableArrayList(todosLosClientes));
    // Combo buscable: se escribe parte del nombre y la lista se filtra
    clienteCombo.setEditable(true);
    clienteCombo.getEditor().setPromptText("Buscar cliente por nombre...");
    clienteCombo.setConverter(new StringConverter<>() {
      @Override public String toString(Cliente c) { return c != null ? c.toString() : ""; }
      @Override public Cliente fromString(String texto) {
        return todosLosClientes.stream()
            .filter(c -> c.toString().equalsIgnoreCase(texto != null ? texto.trim() : ""))
            .findFirst().orElse(clienteCombo.getValue());
      }
    });
    clienteCombo.getEditor().textProperty().addListener((obs, anterior, actual) -> {
      String filtro = actual == null ? "" : actual.trim().toLowerCase();
      if (clienteCombo.getValue() != null && clienteCombo.getValue().toString().equalsIgnoreCase(filtro)) {
        return;
      }
      clienteCombo.setValue(null);
      clienteCombo.setItems(todosLosClientes.filtered(c ->
          filtro.isEmpty() || (c.getNombre() != null && c.getNombre().toLowerCase().contains(filtro))));
      clienteCombo.show();
    });
    facturasTable.setItems(FXCollections.observableArrayList(facturacion.findAll()));
  }
  @FXML public void onLiquidar() {
    try {
      Cliente c = clienteCombo.getValue();
      if (c == null) { mensajeLabel.setText("Seleccione un cliente."); return; }
      double kwh = Double.parseDouble(kwhField.getText());
      FacturaDTO f = facturacion.liquidar(c.getIdCliente(), kwh);
      facturasTable.getItems().add(f);
    } catch (Exception e) { mensajeLabel.setText("Error: " + e.getMessage()); }
  }
}

