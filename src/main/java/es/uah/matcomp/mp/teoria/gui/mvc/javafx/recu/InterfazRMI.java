package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRMI extends Remote {

    int barbarosZonaPreparacion() throws RemoteException;
    int barbarosCampamento() throws RemoteException;

    int getComida() throws RemoteException;
    int getCapacidadMaxComida() throws RemoteException;
    int getMadera() throws RemoteException;
    int getCapacidadMaxMadera() throws RemoteException;
    int getOro() throws RemoteException;
    int getCapacidadMaxOro() throws RemoteException;

    boolean isPausado() throws RemoteException;
    void pausarEjecucion() throws RemoteException;

    boolean isEmergenciaActiva() throws RemoteException;
    void activarEmergencia() throws RemoteException;
}
