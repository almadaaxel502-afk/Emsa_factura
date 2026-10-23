package ar.com.itec1misiones.emsa.view.component;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Logo institucional de Energia de Misiones dibujado con formas JavaFX:
 * circulo naranja con tres barras horizontales en su interior.
 */
public class EmsaLogo extends StackPane {

    private static final Color NARANJA = Color.web("#E87722");

    public EmsaLogo() {
        this(46);
    }

    public EmsaLogo(double tamano) {
        Circle circulo = new Circle(tamano / 2);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(NARANJA);
        circulo.setStrokeWidth(Math.max(2, tamano * 0.10));

        VBox barras = new VBox(tamano * 0.09);
        barras.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Rectangle barra = new Rectangle(tamano * 0.44, tamano * 0.11);
            barra.setArcWidth(tamano * 0.11);
            barra.setArcHeight(tamano * 0.11);
            barra.setFill(NARANJA);
            barras.getChildren().add(barra);
        }

        getChildren().addAll(circulo, barras);
        setMinSize(tamano, tamano);
        setPrefSize(tamano, tamano);
        setMaxSize(tamano, tamano);
    }
}
