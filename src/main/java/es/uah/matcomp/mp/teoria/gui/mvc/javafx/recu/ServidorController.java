package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;
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
            // Actualiza los contadores de recursos
            ComidaRecursos.setText(String.valueOf(centro.getRecurso("Comida").get()));
            MaderaRecursos.setText(String.valueOf(centro.getRecurso("Madera").get()));
            OroRecursos.setText(String.valueOf(centro.getRecurso("Oro").get()));

            // Actualiza los almacenes mostrando la cantidad actual / capacidad máxima
            Granero.setText("Granero: " + centro.getGranero().getCantidadActual() + "/" + centro.getGranero().getCapacidadMaxima());
            Aserradero.setText("Aserradero: " + centro.getAserradero().getCantidadActual() + "/" + centro.getAserradero().getCapacidadMaxima());
            Tesoreria.setText("Tesorería: " + centro.getTesoreria().getCantidadActual() + "/" + centro.getTesoreria().getCapacidadMaxima());

            // Actualiza el estado de las unidades
            CasaPrincipal.setText("Casa Principal: " + centro.contarAldeanos() + " aldeanos");
            Cuartel.setText("Cuartel: " + centro.contarGuerreros() + " guerreros");
            PlazaCentral.setText("Plaza Central: activo");
            AreaRecuperacion.setText("Área Recuperación");

            // Actualiza los indicadores de bárbaros: en preparación y en campamento
            ZonaPreparacion.setText("Preparación: " + centro.getZonaPreparacion().getBarbarosEnPreparacion());
            CampamentoBarbaro.setText("Campamento: " + centro.contarBarbarosCampamento());
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
