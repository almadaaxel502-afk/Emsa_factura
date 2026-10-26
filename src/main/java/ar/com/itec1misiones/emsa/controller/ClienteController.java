package ar.com.itec1misiones.emsa.controller;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.service.ClienteService;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ClienteController {
private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }
    public List<ClienteDTO> findAll(){
        return clienteService.findAll();
    }
    public List<ClienteDTO> buscarPorNombre(String nombre){
        return clienteService.buscarPorNombre(nombre);
    }
    public ClienteDTO buscarPorNroSerieMedidor(String nroSerie){
        return clienteService.buscarPorNroSerieMedidor(nroSerie);
    }



    // Aquí puedes agregar métodos para manejar las solicitudes HTTP relacionadas con los clientes
}
