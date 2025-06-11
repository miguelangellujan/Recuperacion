package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServidorController {
    @FXML
    private Label lblComidaRecursos;
    @FXML
    private Label lblMaderaRecursos;
    @FXML
    private Label lblOroRecursos;
    @FXML
    private Label lblComidaGuerreros;
    @FXML
    private Label lblMaderaGuerreros;
    @FXML
    private Label lblOroGuerreros;
    @FXML
    private Label lblComidaAldeanos;
    @FXML
    private Label lblMaderaHerramientas;
    @FXML
    private Label lblOroHerramientas;
    @FXML
    private Label lblComidaArmas;
    @FXML
    private Label lblOroArmas;
    @FXML
    private Label lblOroAlmacenM;
    @FXML
    private Label lblMaderaAlmacenM;
    @FXML
    private Label lblZonaPreparacion;
    @FXML
    private Label lblCampamentoBarbaro;
    @FXML
    private Label lblMina;
    @FXML
    private Label lblBosque;
    @FXML
    private Label lblGranja;
    @FXML
    private Label lblTesoreria;
    @FXML
    private Label lblAserradero;
    @FXML
    private Label lblGranero;
    @FXML
    private Label lblCuartel;
    @FXML
    private Label lblPlazaCentral;
    @FXML
    private Label lblAreaRecuperacion;
    @FXML
    private Label lblCasaPrincipal;
    @FXML
    private Button btnDetener;
    @FXML
    private Button btnCampana;

    private CentroUrbano centro = new CentroUrbano();// crea 2 aldeanos iniciales
    private final List<Thread> hilosActivos = new ArrayList<>();
    private boolean enPausa = false;

    @FXML
    public void initialize() {
        iniciarHilos();
        btnDetener.setOnAction(event -> ejecucion());
        btnCampana.setOnAction(event -> activarCampana());
    }

    private void iniciarHilos(){
        Thread hiloAldeano = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                if(!enPausa){
                    crearAldeano();
                }
            }
        });
        hilosActivos.add(hiloAldeano);
        hiloAldeano.start();

        Thread hiloBarbaro = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                if(!enPausa){
                    crearBarbaro();
                }
            }
        });
        hilosActivos.add(hiloBarbaro);
        hiloBarbaro.start();

        Thread hiloInterfaz = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                if(!enPausa){
                    actualizarInterfaz();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    Log.log("Error actualizando la interfaz: " + e.getMessage());
                }
            }
        });
        hilosActivos.add(hiloInterfaz);
        hiloInterfaz.start();
    }
    
    private void actualizarInterfaz() {

        Platform.runLater(() -> {
            // Actualización de recursos
            lblComidaRecursos.setText("Comida: " + centro.getRecurso("COMIDA").get());
            lblMaderaRecursos.setText("Madera: " + centro.getRecurso("MADERA").get());
            lblOroRecursos.setText("Oro: " + centro.getRecurso("ORO").get());

            lblGranja.setText(centro.getArea("COMIDA").obtenerEstadoAldeanos());
            lblMina.setText(centro.getArea("ORO").obtenerEstadoAldeanos());
            lblBosque.setText(centro.getArea("MADERA").obtenerEstadoAldeanos());

            // Estado del almacén de comida
            lblGranero.setText(centro.getAlmacen("COMIDA").obtenerEstadoAldeanos());
            // Estado del almacén de madera
            lblAserradero.setText(centro.getAlmacen("MADERA").obtenerEstadoAldeanos());
            // Estado del almacén de oro
            lblTesoreria.setText(centro.getAlmacen("ORO").obtenerEstadoAldeanos());

            // Estado del centro urbano
            lblCasaPrincipal.setText(centro.getCasaPrincipal().obtenerIds());
            lblCuartel.setText(centro.obtenerIdsGuerreros());
            lblPlazaCentral.setText(centro.getPlazaCentral().obtenerIds());
            lblAreaRecuperacion.setText(centro.getAreaRecuperacion().obtenerIdsEnRecuperacion());


            // Estado de los bárbaros
            lblZonaPreparacion.setText(centro.getZonaPreparacion().obtenerIdsEnPreparacion());
            lblCampamentoBarbaro.setText(centro.obtenerIdsBarbaros());
        });
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

    @FXML
    private void ejecucion(){
        enPausa = !enPausa;

        if(enPausa){
            for (Thread hiloActivo : hilosActivos){
                hiloActivo.interrupt();
            }
        } else {
            iniciarHilos();
        }

        btnDetener.setText(enPausa ? "Reanudar" : "Detener");
    }

    @FXML
    private void activarCampana() {
        // Ahora obtenemos el estado actual desde CentroUrbano (deberías tener un método isEmergenciaActiva)
        boolean estadoActual = !centro.isEmergenciaActiva();

        // Llamamos a activarEmergencia, que alterna el estado y notifica aldeanos
        centro.activarEmergencia();

        // Actualizamos texto botón
        btnCampana.setText(estadoActual ? "Detener Emergencia" : "Activar Emergencia");
    }
}
