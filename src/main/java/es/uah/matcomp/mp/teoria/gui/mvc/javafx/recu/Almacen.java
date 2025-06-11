package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

public class Almacen implements Zona {
    private final String tipo;
    private int cantidadActual;
    private int capacidadMaxima;
    private final CentroUrbano centro;

    private final Semaphore semDeposito = new Semaphore(3, true);
    private final Lock lock = new ReentrantLock(true);
    private final Condition espacioDisponible = lock.newCondition();
    private final Lock lockGuerreros = new ReentrantLock(true);
    private final Condition puedeEntrarGuerrero = lockGuerreros.newCondition();
    private final List<Guerrero> guerrerosDentro = new ArrayList<>();
    private final int MAX_GUERREROS = 3;
    private final List<Aldeano> aldeanosDepositando = new ArrayList<>();
    private final List<Aldeano> aldeanosEsperando = new ArrayList<>();

    public Almacen(String tipo, int capacidad, CentroUrbano centro) {
        this.tipo = tipo;
        this.capacidadMaxima = capacidad;
        this.cantidadActual = 0;
        this.centro = centro;
    }
    public void depositar(Aldeano aldeano, int cantidad) throws InterruptedException {
        int restante = cantidad;
        lock.lock();
        try {
            while (restante > 0) {
                // Si no hay espacio, esperar bloqueando
                while (cantidadActual == capacidadMaxima) {
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                        Log.log("El almacén de " + tipo + " está lleno. El aldeano " + aldeano.getIdAldeano() + " espera para depositar.");
                    }
                    espacioDisponible.await(); // espera hasta que haya espacio
                }

                // Hay espacio, listo para depositar
                aldeanosEsperando.remove(aldeano);
                if (!aldeanosDepositando.contains(aldeano)) {
                    aldeanosDepositando.add(aldeano);
                }

                int espacio = capacidadMaxima - cantidadActual;
                int aDepositar = Math.min(espacio, restante);

                cantidadActual += aDepositar;
                restante -= aDepositar;

                centro.sumarRecurso(tipo, aDepositar);
                Log.log("El aldeano " + aldeano.getIdAldeano() + " ha depositado " + aDepositar + " de " + tipo + ". Total almacenado: " + cantidadActual);

                if (restante > 0) {
                    Log.log("El aldeano " + aldeano.getIdAldeano() + " se queda esperando con " + restante + " de " + tipo + " por falta de espacio.");
                    aldeanosDepositando.remove(aldeano);
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                    }
                } else {
                    // Terminó de depositar
                    aldeanosDepositando.remove(aldeano);
                }

                // Notificar a otros posibles aldeanos que puedan ahora depositar
                espacioDisponible.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    public int getCantidadActual() {
        return cantidadActual;
    }

    public int getCapacidadMaxima(){
        return capacidadMaxima;
    }

    public void aumentarCapacidad(int cantidad) {
        lock.lock();
        try {
            capacidadMaxima += cantidad;
            Log.log("Capacidad de " + tipo + " aumentada a " + capacidadMaxima);
            espacioDisponible.signalAll(); // Avisamos a los aldeanos por si ahora hay hueco
        } finally {
            lock.unlock();
        }
    }
    public synchronized void saquear() {
        int porcentaje = FuncionesComunes.randomBetween(10, 30);
        int robado = (cantidadActual * porcentaje) / 100;
        cantidadActual -= robado;
        centro.restarRecurso(tipo, robado);

        Log.log("¡SAQUEO en " + tipo + "! Se han robado " + robado + " unidades. Restante: " + cantidadActual);

        int comida = centro.getRecurso("COMIDA").get();
        int madera = centro.getRecurso("MADERA").get();
        int oro = centro.getRecurso("ORO").get();
        Log.log("ESTADO RECURSOS => Comida: " + comida + ", Madera: " + madera + ", Oro: " + oro);
    }

    public void añadirInicial(int cantidad) {
        lock.lock();
        try {
            cantidadActual = Math.min(cantidad, capacidadMaxima);
            Log.log("Almacén de " + tipo + " inicializado con " + cantidadActual);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean entrarGuerrero(Guerrero g) throws InterruptedException {
        lockGuerreros.lock();
        try {
            while (guerrerosDentro.size() >= MAX_GUERREROS) {
                puedeEntrarGuerrero.await();
            }
            guerrerosDentro.add(g);
            return true;
        } finally {
            lockGuerreros.unlock();
        }
    }

    @Override
    public void salirGuerrero(Guerrero g) {
        lockGuerreros.lock();
        try {
            guerrerosDentro.remove(g);
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }
    }

    @Override
    public String getNombreZona() {
        return "Almacén de " + tipo;
    }

    @Override
    public boolean enfrentarABarbaro(Barbaro b) throws InterruptedException {
        synchronized (guerrerosDentro) {
            if (guerrerosDentro.isEmpty()) return false;

            Guerrero g = guerrerosDentro.remove(0);
            Log.log(b.getIdBarbaro() + " se enfrenta a " + g.getIdGuerrero());

            boolean barbaroGana = new Random().nextBoolean();
            if (barbaroGana) {
                Log.log(b.getIdBarbaro() + " derrota a " + g.getIdGuerrero());
                return true;
            } else {
                Log.log(b.getIdBarbaro() + " pierde contra " + g.getIdGuerrero());
                g.enviarARecuperacion();
                return false;
            }
        }
    }
    public String obtenerEstadoAldeanos() {
        synchronized (this) {
            StringBuilder dentro = new StringBuilder();
            for (Aldeano a : aldeanosDepositando) {
                dentro.append(a.getIdAldeano()).append(", ");
            }
            if (dentro.length() > 0) dentro.setLength(dentro.length() - 2);

            StringBuilder esperando = new StringBuilder();
            for (Aldeano a : aldeanosEsperando) {
                esperando.append(a.getIdAldeano()).append(", ");
            }
            if (esperando.length() > 0) esperando.setLength(esperando.length() - 2);

            return "Depositando: " + dentro + "\n\nEsperando: " + esperando;
        }
    }

}
