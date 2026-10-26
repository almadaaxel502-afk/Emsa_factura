package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<ClienteDTO> findAll();

    List<ClienteDTO> buscarPorNombre(String nombre);

    ClienteDTO buscarPorNroSerieMedidor(String nroSerie);

    ClienteDTO toDTO(Cliente cliente);
}
