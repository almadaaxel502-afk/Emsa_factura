package itec.emsa.emsa.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación manual para la UI JavaFX.
 * Delega en el AuthenticationManager de Spring Security.
 */
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Intenta autenticar al usuario con las credenciales dadas.
     *
     * @return true si la autenticación fue exitosa, false si las credenciales son incorrectas
     * @throws AuthenticationException para errores distintos a credenciales inválidas
     */
    public boolean autenticar(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * Cierra la sesión actual limpiando el SecurityContext.
     */
    public void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Retorna el username del usuario autenticado actualmente, o null si no hay sesión.
     */
    public String getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }
}
