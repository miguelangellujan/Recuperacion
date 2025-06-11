package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRMI extends Remote {

    boolean isPausado() throws RemoteException;
    void pausarEjecucion() throws RemoteException;

    boolean isEmergenciaActiva() throws RemoteException;
    void activarEmergencia() throws RemoteException;
}
