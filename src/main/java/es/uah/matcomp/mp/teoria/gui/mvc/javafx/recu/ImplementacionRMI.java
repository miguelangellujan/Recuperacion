package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.fxml.FXML;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImplementacionRMI extends UnicastRemoteObject implements InterfazRMI {
    private CentroUrbano centro;

    private final AtomicBoolean emergenciaActiva = new AtomicBoolean(false);

    private final AtomicBoolean pausado = new AtomicBoolean(false);

    public ImplementacionRMI(CentroUrbano centroUrbano) throws RemoteException {
        this.centro = centroUrbano;
    }

    // Número de Bárbaros en el campamento y en la zona de preparación
    @Override
    public int barbarosZonaPreparacion() throws RemoteException {
        return centro.getZonaPreparacion().contarBarbarosPreparacion();
    }

    @Override
    public int barbarosCampamento() throws RemoteException {
        return centro.contarBarbarosEnCampamento();
    }

    // Estado de los recursos almacenados (cantidad actual / capacidad máxima)
    @Override
    public int getComida() throws RemoteException {
        return centro.getRecurso("COMIDA").get();
    }

    @Override
    public int getCapacidadMaxComida() throws RemoteException {
        return centro.getGranero().getCapacidadMaxima();
    }

    @Override
    public int getMadera() throws RemoteException {
        return centro.getRecurso("MADERA").get();
    }

    @Override
    public int getCapacidadMaxMadera() throws RemoteException {
        return centro.getAserradero().getCapacidadMaxima();
    }

    @Override
    public int getOro() throws RemoteException {
        return centro.getRecurso("ORO").get();
    }

    @Override
    public int getCapacidadMaxOro() throws RemoteException {
        return centro.getTesoreria().getCapacidadMaxima();
    }

    // Botón de Emergencia
    @Override
    public boolean isEmergenciaActiva() throws RemoteException {
        return emergenciaActiva.get();
    }

    @Override
    public void activarEmergencia() throws RemoteException {
        boolean nuevoEstado = !emergenciaActiva.get();
        emergenciaActiva.set(nuevoEstado);

        if (nuevoEstado) {
            Log.log("¡Emergencia activada! Los aldeanos regresan a CASA PRINCIPAL.");

            centro.getGranero().liberarAldeanos();
            centro.getAserradero().liberarAldeanos();
            centro.getTesoreria().liberarAldeanos();

            for(Aldeano a : centro.getAldeanos()){
                a.setEmergencia(true);
                a.moverACasaPrincipal();
            }
        } else {
            Log.log("¡Emergencia desactivada! Los aldeanos retoman su trabajo.");

            for(Aldeano a : centro.getAldeanos()){
                a.setEmergencia(false);
                synchronized (a){
                    a.notify();
                }
            }
        }
    }

    // Botón de Pausa
    @Override
    public boolean isPausado() throws RemoteException {
        return pausado.get();
    }

    @Override
    public void pausarEjecucion() throws RemoteException {
        boolean nuevoEstado = !pausado.get();
        pausado.set(nuevoEstado);

        if (nuevoEstado) {
            Log.log("Sistema Pausado");
        } else {
            Log.log("Sistema Reanudado");
        }
    }
}
