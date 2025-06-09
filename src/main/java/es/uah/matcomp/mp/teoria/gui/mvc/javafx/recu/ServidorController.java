package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

    private CentroUrbano centro = new CentroUrbano(); // crea 2 aldeanos iniciales

    @FXML
    public void initialize() {
        /* Esto nose si lo tienes que hacer revisalo en el enunciado
        // Fijamos los costos de unidades y mejoras en los TextField correspondientes
        ComidaGuerreros.setText("50");
        MaderaGuerreros.setText("50");
        OroGuerreros.setText("80");
        ComidaAldeanos.setText("50");
        MaderaHerramientas.setText("120");
        OroHerramientas.setText("80");
        ComidaArmas.setText("150");
        OroArmas.setText("100");
        MaderaAlmacenM.setText("150");
        OroAlmacenM.setText("50");

        // Asignamos nombres fijos a las áreas de recursos
        Mina.setText("Mina");
        Bosque.setText("Bosque");
        Granja.setText("Granja");
         */

        // Hilo para crear aldeanos automáticamente cada 20 segundos (si hay comida)
        new Thread(() -> crearAldeano()).start();

        // Hilo para generación automática de bárbaros cada 15–30 s
        new Thread(() -> crearBarbaro()).start();

        // Hilo opcional para mostrar recursos cada 10 s (modo consola)

        // Hilo para actualizar la interfaz cada segundo
        new Thread(() -> {
            while(true) {
                actualizarInterfaz();
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.log("Error actualizando la interfaz");
                }
            }
        }).start();
    }

    private void actualizarInterfaz() {

        Platform.runLater(() -> {
            // Actualización de recursos
            lblGranja.setText("Granja: " + centro.getArea("COMIDA").getNombreZona() + "\n" + centro.getArea("COMIDA").obtenerEstadoAldeanos());
            lblMina.setText("Mina: " + centro.getArea("ORO").getNombreZona() + "\n" + centro.getArea("ORO").obtenerEstadoAldeanos());
            lblBosque.setText("Bosque: " + centro.getArea("MADERA").getNombreZona() + "\n" + centro.getArea("MADERA").obtenerEstadoAldeanos());

            // Estado del almacén de comida
            lblGranero.setText("Granero: " + centro.getAlmacen("COMIDA").getCantidadActual() + "/" + centro.getAlmacen("COMIDA").getCapacidadMaxima() + "\n" + centro.getAlmacen("COMIDA").obtenerEstadoAldeanos());
            // Estado del almacén de madera
            lblAserradero.setText("Aserradero: " + centro.getAlmacen("MADERA").getCantidadActual() + "/" + centro.getAlmacen("MADERA").getCapacidadMaxima() + "\n" + centro.getAlmacen("MADERA").obtenerEstadoAldeanos());
            // Estado del almacén de oro
            lblTesoreria.setText("Tesorería: " + centro.getAlmacen("ORO").getCantidadActual() + "/" + centro.getAlmacen("ORO").getCapacidadMaxima() + "\n" + centro.getAlmacen("ORO").obtenerEstadoAldeanos());

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
}
