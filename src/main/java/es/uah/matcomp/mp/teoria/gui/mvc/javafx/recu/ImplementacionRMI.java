package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImplementacionRMI extends UnicastRemoteObject implements InterfazRMI {
    private CentroUrbano centro;

    private boolean emergenciaActiva = false;

    private final AtomicBoolean pausado = new AtomicBoolean(false);

    public ImplementacionRMI(CentroUrbano centroUrbano) throws RemoteException {
        this.centro = centroUrbano;
    }

    // Centro Urbano
    @Override
    public int aldeanosCentroUrbano() throws RemoteException {
        String aldeanos = centro.getAldeanos();
        return aldeanos.equals("Ninguno") ? 0 : aldeanos.split(", ").length;
    }

    @Override
    public int guerrerosCentroUrbano() throws RemoteException {
        String guerreros = centro.getGuerreros();
        return guerreros.equals("Ninguno") ? 0 : guerreros.split(", ").length;
    }

    // Áreas de Recursos

    // Mina
    @Override
    public int aldeanosMina() throws RemoteException {
        String ids = centro.getAldeanosMina();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosMina() throws RemoteException {
        String ids = centro.getGuerrerosMina();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosMina() throws RemoteException {
        String ids = centro.getBarbarosMina();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Bosque
    @Override
    public int aldeanosBosque() throws RemoteException {
        String ids = centro.getAldeanosBosque();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosBosque() throws RemoteException {
        String ids = centro.getGuerrerosBosque();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosBosque() throws RemoteException {
        String ids = centro.getBarbarosBosque();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Granja
    @Override
    public int aldeanosGranja() throws RemoteException {
        String ids = centro.getAldeanosGranja();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosGranja() throws RemoteException {
        String ids = centro.getGuerrerosGranja();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosGranja() throws RemoteException {
        String ids = centro.getBarbarosGranja();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Almacenes

    // Tesorería
    @Override
    public int aldeanosTesoreria() throws RemoteException {
        String ids = centro.getAldeanosTesoreria();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosTesoreria() throws RemoteException {
        String ids = centro.getGuerrerosTesoreria();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosTesoreria() throws RemoteException {
        String ids = centro.getBarbarosTesoreria();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Aserradero
    @Override
    public int aldeanosAserradero() throws RemoteException {
        String ids = centro.getAldeanosAserradero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosAserradero() throws RemoteException {
        String ids = centro.getGuerrerosAserradero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosAserradero() throws RemoteException {
        String ids = centro.getBarbarosAserradero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Granero
    @Override
    public int aldeanosGranero() throws RemoteException {
        String ids = centro.getAldeanosGranero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int guerrerosGranero() throws RemoteException {
        String ids = centro.getGuerrerosGranero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    @Override
    public int barbarosGranero() throws RemoteException {
        String ids = centro.getBarbarosGranero();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    // Número de Bárbaros en el campamento y en la zona de preparación
    @Override
    public int barbarosZonaPreparacion() throws RemoteException {
        return centro.getZonaPreparacion().contarBarbarosPreparacion();
    }

    @Override
    public int barbarosCampamento() throws RemoteException {
        return centro.getBarbarosCampamento().length();
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
        return emergenciaActiva;
    }

    @Override
    public void activarEmergencia() throws RemoteException {
        emergenciaActiva = !emergenciaActiva;

        if (emergenciaActiva) {
            Log.log("¡Emergencia activada! Los aldeanos regresan a CASA PRINCIPAL.");

            centro.getGranero().liberarAldeanos();
            centro.getAserradero().liberarAldeanos();
            centro.getTesoreria().liberarAldeanos();

            for (Aldeano a : centro.getAldeanos2()) {
                a.setEmergencia(true);
                a.moverACasaPrincipal();
            }
        } else {
            Log.log("¡Emergencia desactivada! Los aldeanos retoman su trabajo.");

            for (Aldeano a : centro.getAldeanos2()) {
                if (centro.getCasaPrincipal().estaRegistrado(a.getIdAldeano())) {
                    centro.getCasaPrincipal().salir(a.getIdAldeano());
                    a.moverAPlazaCentral(); // Los devuelve a PlazaCentral después de la emergencia
                }

                a.setEmergencia(false);
                synchronized (a) {
                    a.notify();
                }
            }
        }
    }

    // Botón de Pausa
    @Override
    public boolean isPausado() throws RemoteException {
        return centro.isPausado();
    }

    @Override
    public void pausarEjecucion() throws RemoteException {
        boolean nuevoEstado = !centro.isPausado();
        centro.setPausa(nuevoEstado);
        Log.log(nuevoEstado ? "Sistema Pausado" : "Sistema Reanudado");
    }
}