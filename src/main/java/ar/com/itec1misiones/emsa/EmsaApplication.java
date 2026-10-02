package ar.com.itec1misiones.emsa;
import ar.com.itec1misiones.emsa.view.EmsaFxApp;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class EmsaApplication {
    public static void main(String[] args) { Application.launch(EmsaFxApp.class, args); }
}
