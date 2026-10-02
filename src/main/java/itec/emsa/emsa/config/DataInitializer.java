package itec.emsa.emsa.config;

import itec.emsa.emsa.entity.Rol;
import itec.emsa.emsa.entity.Usuario;
import itec.emsa.emsa.repository.RolRepository;
import itec.emsa.emsa.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Se ejecuta al iniciar la app.
 * Crea el rol ROLE_ADMIN y el usuario admin/admin si no existen.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Crear rol ROLE_ADMIN si no existe
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseGet(() -> rolRepository.save(new Rol("ROLE_ADMIN")));

        // Crear usuario admin si no existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario("admin", passwordEncoder.encode("admin"));
            admin.setHabilitado(true);
            admin.getRoles().add(rolAdmin);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario admin creado con contraseña: admin");
        }
    }
}
