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
    private final Condition colaDeposito = lock.newCondition();
    private final Queue<Aldeano> colaEspera = new LinkedList<>();
    private final Random rnd = new Random();
    private FuncionesComunes funcionesComunes;

    private final Lock lockGuerreros = new ReentrantLock(true);
    private final Condition puedeEntrarGuerrero = lockGuerreros.newCondition();
    private final List<Guerrero> guerrerosDentro = new ArrayList<>();
    private final int MAX_GUERREROS = 3;

    public Almacen(String tipo, int capacidad, CentroUrbano centro) {
        this.tipo = tipo;
        this.capacidadMaxima = capacidad;
        this.cantidadActual = 0;
        this.centro = centro;
    }
    public void depositar(String idAldeano, int cantidad) throws InterruptedException {
        int restante = cantidad;

        lock.lock();
        try {
            while (restante > 0) {
                while (cantidadActual == capacidadMaxima) {
                    Log.log("El almacén de " + tipo + " está lleno. " + "El aldeano " + idAldeano + " espera para depositar.");
                    espacioDisponible.await();
                }

                int espacio = capacidadMaxima - cantidadActual;
                int aDepositar = Math.min(espacio, restante);

                cantidadActual += aDepositar;
                restante -= aDepositar;

                centro.sumarRecurso(tipo, aDepositar);

                Log.log("El aldeano " + idAldeano + " ha depositado " + aDepositar + " de " + tipo +
                        ". Total almacenado: " + cantidadActual);

                espacioDisponible.signalAll();

                if (restante > 0 && cantidadActual == capacidadMaxima) {
                    Log.log("El aldeano " + idAldeano + " se queda esperando con " + restante + " de " + tipo + " por falta de espacio.");
                    espacioDisponible.await();
                }
            }
        } finally {
            lock.unlock();
        }
    }
    public void aumentarCapacidad(int cantidad) {
        capacidadMaxima += cantidad;
        Log.log("Capacidad de " + tipo + " aumentada a " + capacidadMaxima);
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public synchronized void saquear() {
        int porcentaje = funcionesComunes.randomBetween(10, 30);
        int robado = (cantidadActual * porcentaje) / 100;
        cantidadActual -= robado;
        centro.restarRecurso(tipo, robado);

        Log.log("¡SAQUEO en " + tipo + "! Se han robado " + robado + " unidades. Restante: " + cantidadActual);

        int comida = centro.getRecurso("COMIDA").get();
        int madera = centro.getRecurso("MADERA").get();
        int oro = centro.getRecurso("ORO").get();
        Log.log("ESTADO RECURSOS => Comida: " + comida + ", Madera: " + madera + ", Oro: " + oro);
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
}
