package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfazRMI extends Remote {

    int aldeanosCentroUrbano() throws RemoteException;
    int guerrerosCentroUrbano() throws RemoteException;

    int aldeanosMina() throws RemoteException;
    int guerrerosMina()throws RemoteException;
    int barbarosMina()throws RemoteException;
    int aldeanosBosque() throws RemoteException;
    int guerrerosBosque()throws RemoteException;
    int barbarosBosque()throws RemoteException;
    int aldeanosGranja() throws RemoteException;
    int guerrerosGranja()throws RemoteException;
    int barbarosGranja()throws RemoteException;

    int aldeanosTesoreria() throws RemoteException;
    int guerrerosTesoreria()throws RemoteException;
    int barbarosTesoreria() throws RemoteException;
    int aldeanosAserradero() throws RemoteException;
    int guerrerosAserradero()throws RemoteException;
    int barbarosAserradero()throws RemoteException;
    int aldeanosGranero() throws RemoteException;
    int guerrerosGranero()throws RemoteException;
    int barbarosGranero() throws RemoteException;

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
