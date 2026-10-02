package itec.emsa.emsa.repository;

import itec.emsa.emsa.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}