package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Random;

public class ServidorController {
    @FXML
    private TextField ComidaRecursos;
    @FXML
    private TextField MaderaRecursos;
    @FXML
    private TextField OroRecursos;
    @FXML
    private TextField ComidaGuerreros;
    @FXML
    private TextField MaderaGuerreros;
    @FXML
    private TextField OroGuerreros;
    @FXML
    private TextField ComidaAldeanos;
    @FXML
    private TextField MaderaHerramientas;
    @FXML
    private TextField OroHerramientas;
    @FXML
    private TextField ComidaArmas;
    @FXML
    private TextField OroArmas;
    @FXML
    private TextField OroAlmacenM;
    @FXML
    private TextField MaderaAlmacenM;
    @FXML
    private TextField ZonaPreparacion;
    @FXML
    private TextField CampamentoBarbaro;
    @FXML
    private TextField Mina;
    @FXML
    private TextField Bosque;
    @FXML
    private TextField Granja;
    @FXML
    private TextField Tesoreria;
    @FXML
    private TextField Aserradero;
    @FXML
    private TextField Granero;
    @FXML
    private TextField Cuartel;
    @FXML
    private TextField PlazaCentral;
    @FXML
    private TextField AreaRecuperacion;
    @FXML
    private TextField CasaPrincipal;

    private CentroUrbano centro = new CentroUrbano(); // crea 2 aldeanos iniciales

    @FXML
    public void initialize() {
        // Hilo para crear aldeanos automáticamente cada 20 segundos (si hay comida)
        new Thread(() -> crearAldeano()).start();

        // Hilo para generación automática de bárbaros cada 15–30 s
        new Thread(() -> crearBarbaro()).start();

        // Hilo opcional para mostrar recursos cada 10 s (modo consola)
    }

    @FXML
    protected void crearAldeano(){
        try {
            Thread.sleep(20000); // cada 20 segundos
            centro.crearAldeano();
        } catch (InterruptedException e) {
            Log.log("Generador de aldeanos interrumpido.");
        }
    }

    @FXML
    protected void crearBarbaro(){
        Random rnd = new Random();
        for (int i = 0; i < 999; i++) {
            try {
                int espera = 15000 + rnd.nextInt(15000); // entre 15–30 s
                Thread.sleep(espera);
                centro.crearBarbaro();

            } catch (InterruptedException e) {
                Log.log("Generador de bárbaros interrumpido.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
