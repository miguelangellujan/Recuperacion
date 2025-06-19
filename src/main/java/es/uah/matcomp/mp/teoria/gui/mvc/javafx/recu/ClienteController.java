package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class ClienteController {
    @FXML
    private TextField AldeanosMina;
    @FXML
    private TextField GuerrerosMina;
    @FXML
    private TextField BarbarosMina;
    @FXML
    private TextField AldeanosBosque;
    @FXML
    private TextField GuerrerosBosque;
    @FXML
    private TextField BarbarosBosque;
    @FXML
    private TextField AldeanosGranja;
    @FXML
    private TextField GuerrerosGranja;
    @FXML
    private TextField BarbarosGranja;
    @FXML
    private TextField AldeanosTesoreria;
    @FXML
    private TextField GuerrerosTesoreria;
    @FXML
    private TextField BarbarosTesoreria;
    @FXML
    private TextField AldeanosAserradero;
    @FXML
    private TextField GuerrerosAserradero;
    @FXML
    private TextField BarbarosAserradero;
    @FXML
    private TextField AldeanosGranero;
    @FXML
    private TextField GuerrerosGranero;
    @FXML
    private TextField BarbarosGranero;
    @FXML
    private TextField ZonaPreparacionBarbaros;
    @FXML
    private TextField CampamentoBarbaros;
    @FXML
    private TextField AldeanosCentroUrbano;
    @FXML
    private TextField GuerrerosCentroUrbano;
    @FXML
    private Label lblComida;
    @FXML
    private Label lblMadera;
    @FXML
    private Label lblOro;
    @FXML
    private Button btnDetener;
    @FXML
    private Button btnCampana;

    private Thread hilo;
    private InterfazRMI interfazRMI;
    private boolean enPausa = false;

    @FXML
    public void initialize(){
        try {
            interfazRMI = (InterfazRMI) Naming.lookup("AgeOfThreadsService");
            inicializarActualizacion();


        } catch (IOException e){
            Log.log("Error en el cliente RMI: " + e.getMessage());
        } catch (Exception e){
            Log.log("Error en el cliente RMI: " + e.getMessage());
        }
    }

    private void inicializarActualizacion(){
        hilo = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()){
                    Platform.runLater(this :: actualizarInterfaz);
                    Thread.sleep(100);
                }
            } catch (Exception e){
                Log.log("Error al actualizar la interfaz del cliente: " + e.getMessage());
            }
        });
        hilo.start();
    }

    private void actualizarInterfaz(){
        try {
            AldeanosCentroUrbano.setText(String.valueOf(interfazRMI.aldeanosCentroUrbano()));
            GuerrerosCentroUrbano.setText(String.valueOf(interfazRMI.guerrerosCentroUrbano()));

            AldeanosMina.setText(String.valueOf(interfazRMI.aldeanosMina()));
            GuerrerosMina.setText(String.valueOf(interfazRMI.guerrerosMina()));
            BarbarosMina.setText(String.valueOf(interfazRMI.barbarosMina()));

            AldeanosBosque.setText(String.valueOf(interfazRMI.aldeanosBosque()));
            GuerrerosBosque.setText(String.valueOf(interfazRMI.guerrerosBosque()));
            BarbarosBosque.setText(String.valueOf(interfazRMI.barbarosBosque()));

            AldeanosGranja.setText(String.valueOf(interfazRMI.aldeanosGranja()));
            GuerrerosGranja.setText(String.valueOf(interfazRMI.guerrerosGranja()));
            BarbarosGranja.setText(String.valueOf(interfazRMI.barbarosGranja()));

            AldeanosTesoreria.setText(String.valueOf(interfazRMI.aldeanosTesoreria()));
            GuerrerosTesoreria.setText(String.valueOf(interfazRMI.guerrerosTesoreria()));
            BarbarosTesoreria.setText(String.valueOf(interfazRMI.barbarosTesoreria()));

            AldeanosAserradero.setText(String.valueOf(interfazRMI.aldeanosAserradero()));
            GuerrerosAserradero.setText(String.valueOf(interfazRMI.guerrerosAserradero()));
            BarbarosAserradero.setText(String.valueOf(interfazRMI.barbarosAserradero()));

            AldeanosGranero.setText(String.valueOf(interfazRMI.aldeanosGranero()));
            GuerrerosGranero.setText(String.valueOf(interfazRMI.guerrerosGranero()));
            BarbarosGranero.setText(String.valueOf(interfazRMI.barbarosGranero()));

            ZonaPreparacionBarbaros.setText(String.valueOf(interfazRMI.barbarosZonaPreparacion()));
            CampamentoBarbaros.setText(String.valueOf(interfazRMI.barbarosCampamento()));

            lblComida.setText("Comida: " + interfazRMI.getComida() + " / " + interfazRMI.getCapacidadMaxComida());
            lblMadera.setText("Madera: " + interfazRMI.getMadera() + " / " + interfazRMI.getCapacidadMaxMadera());
            lblOro.setText("Oro: " + interfazRMI.getOro() + " / " + interfazRMI.getCapacidadMaxOro());
        } catch (Exception e){
            Log.log("Error al actualizar la interfaz del cliente: " + e.getMessage());
        }
    }

    // Funciones botón de pausa
    @FXML
    private void ejecucion(){
        try {
            interfazRMI.pausarEjecucion();
            boolean estadoPausa = interfazRMI.isPausado();
            enPausa = estadoPausa;

            btnDetener.setText(enPausa ? "Reanudar" : "Detener");

            Log.log(enPausa ? "Ejecución pausada desde el cliente. " : "Ejecución reanudada desde el cliente.");
        } catch (RemoteException e) {
            Log.log("Error al pausar/ reanudar la ejecución: " + e.getMessage());
        }
    }

    // Función botón de la emergencia
    @FXML
    private void activarCampana() {
        try {
            // Llamamos a activarEmergencia, que alterna el estado y notifica aldeanos
            interfazRMI.activarEmergencia();

            // Ahora obtenemos el estado actual desde CentroUrbano
            boolean estadoActual = interfazRMI.isEmergenciaActiva();

            // Actualizamos texto botón
            Platform.runLater(() -> btnCampana.setText(estadoActual ? "Detener Emergencia" : "Activar Emergencia"));
        } catch (RemoteException e) {
            Log.log("Error al activar la campana: " + e.getMessage());
        }
    }
}
