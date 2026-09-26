package ar.com.itec1misiones.emsa.service;
import java.util.List;
public interface CRUDService<T> {
    List<T> findAll();
    T findOne(Integer id);
    void create(T dto);
    void update(T dto);
    void delete(T dto);
}
