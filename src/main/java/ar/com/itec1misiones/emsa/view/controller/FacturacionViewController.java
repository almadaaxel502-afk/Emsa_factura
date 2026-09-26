package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FacturacionViewController {

    @FXML
    private ComboBox<Usuario> usuarioCombo;
    @FXML
    private TextField kwhField;
    @FXML
    private Label mensajeLabel;
    @FXML
    private TableView<FacturaDTO> facturasTable;
    @FXML
    private TableColumn<FacturaDTO, Integer> colId;
    @FXML
    private TableColumn<FacturaDTO, Integer> colUsuario;
    @FXML
    private TableColumn<FacturaDTO, Double> colKwh;
    @FXML
    private TableColumn<FacturaDTO, Double> colTotal;

    private final UsuarioRepository usuarios;
    private final FacturacionService facturacion;

    public FacturacionViewController(UsuarioRepository usuarios, FacturacionService facturacion) {
        this.usuarios = usuarios;
        this.facturacion = facturacion;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colKwh.setCellValueFactory(new PropertyValueFactory<>("kwhTotales"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        usuarioCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario u) {
                return u == null ? "" : u.getId() + " - " + u.getNombre();
            }

            @Override
            public Usuario fromString(String s) {
                return null;
            }
        });
        recargar();
    }

    @FXML
    public void onLiquidar() {
        Usuario u = usuarioCombo.getValue();
        if (u == null) {
            mensaje("Seleccione un usuario.");
            return;
        }
        double kwh;
        try {
            kwh = Double.parseDouble(kwhField.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mensaje("kWh inválido.");
            return;
        }
        try {
            FacturaDTO f = facturacion.liquidar(u.getId(), kwh);
            mensaje("Factura #" + f.getId() + " liquidada: $" + f.getTotal());
            recargar();
        } catch (Exception e) {
            mensaje("Error: " + e.getMessage());
        }
    }

    private void recargar() {
        List<Usuario> lista = usuarios.findAll();
        usuarioCombo.setItems(FXCollections.observableArrayList(lista));
        if (!lista.isEmpty() && usuarioCombo.getValue() == null) {
            usuarioCombo.setValue(lista.get(0));
        }
        facturasTable.setItems(FXCollections.observableArrayList(facturacion.findAll()));
    }

    private void mensaje(String texto) {
        mensajeLabel.setText(texto);
    }
}
