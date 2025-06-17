package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Almacen implements Zona {
    private final String tipo;
    private int cantidadActual;
    private int capacidadMaxima;
    private final CentroUrbano centro;
    private final AtomicInteger semDeposito = new AtomicInteger(3);
    private final Object lock = new Object();
    private final List<Aldeano> aldeanosDepositando = new ArrayList<>();
    private final List<Aldeano> aldeanosEsperando = new ArrayList<>();
    private final List<Guerrero> guerreros = new ArrayList<>();
    private final Set<Guerrero> guerrerosEnCombate = Collections.synchronizedSet(new HashSet<>());
    private final List<Barbaro> barbarosAtacando = new ArrayList<>();
    private AreaRecuperacion areaRecuperacion;

    // Constructor
    public Almacen(String tipo, int capacidad, CentroUrbano centro) {
        this.tipo = tipo;
        this.capacidadMaxima = capacidad;
        this.cantidadActual = 0;
        this.centro = centro;
    }
    // Getters
    public int getCantidadActual() {
        return cantidadActual;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    // Funciones Interface Zona
    @Override
    public boolean entrarGuerrero(Guerrero g) throws InterruptedException {
        guerreros.add(g);
        return true;
    }
    @Override
    public void salirGuerrero(Guerrero g) {
        guerreros.remove(g);
    }
    @Override
    public String getNombreZona() {
        return "Almacén de " + tipo;
    }

    @Override
    public boolean enfrentarABarbaro(Barbaro b) throws InterruptedException {
        // No hay defensores en un almacén. El bárbaro pasa directamente al saqueo.
        Thread.sleep(1000); // Espera de 1 segundo antes de saquear
        return false;
    }
    //Esto hay que hacerlo
    public void depositar(Aldeano aldeano, int cantidad) throws InterruptedException {
        centro.esperarSiPausado();

        int restante = cantidad;

        while (restante > 0) {
            int tiempoDeposito = 0;
            int aDepositar = 0;

            synchronized (lock) {
                // Esperar si el sistema está pausado
                centro.esperarSiPausado();

                // Esperar si el almacén está lleno
                while (cantidadActual == capacidadMaxima) {
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                        Log.log("El almacén de " + tipo + " está lleno. El aldeano " + aldeano.getIdAldeano() + " espera para depositar.");
                    }
                    lock.wait(); // espera hasta que haya espacio
                    centro.esperarSiPausado();
                }

                // Preparar para depositar
                aldeanosEsperando.remove(aldeano);
                aldeanosDepositando.add(aldeano);

                int espacio = capacidadMaxima - cantidadActual;
                aDepositar = Math.min(espacio, restante);
                tiempoDeposito = FuncionesComunes.randomBetween(2000, 3000); // Simular 2-3s

                lock.notifyAll(); // Avisar a otros hilos antes de soltar el lock
            }

            // Simulación del tiempo de depósito (fuera del lock)
            Thread.sleep(tiempoDeposito);
            centro.esperarSiPausado();

            synchronized (lock) {
                cantidadActual += aDepositar;
                restante -= aDepositar;
                centro.esperarSiPausado();
                centro.sumarRecurso(tipo, aDepositar);
                centro.esperarSiPausado();
                Log.log("El aldeano " + aldeano.getIdAldeano() + " ha depositado " + aDepositar + " de " + tipo + ". Total almacenado: " + cantidadActual);

                if (restante > 0) {
                    centro.esperarSiPausado();
                    Log.log("El aldeano " + aldeano.getIdAldeano() + " se queda esperando con " + restante + " de " + tipo + " por falta de espacio.");

                    // Moverlo de 'depositando' a 'esperando'
                    aldeanosDepositando.remove(aldeano);
                    aldeanosEsperando.add(aldeano);
                } else {
                    // Eliminamos del set de depositando
                    aldeanosDepositando.remove(aldeano);
                }

                lock.notifyAll(); // Despertar posibles aldeanos esperando
            }
        }

        // Limpieza final por si hubo interrupciones
        synchronized (lock) {
            aldeanosDepositando.remove(aldeano);
            aldeanosEsperando.remove(aldeano);
        }
    }

    public void aumentarCapacidad(int cantidad) {
        synchronized (lock) {
            capacidadMaxima += cantidad;
            Log.log("Capacidad de " + tipo + " aumentada a " + capacidadMaxima);
            lock.notifyAll(); // Avisamos a los aldeanos por si ahora hay hueco
        }
    }

    public void saquear(Barbaro b) {
        agregarBarbaro(b);

        int porcentaje = FuncionesComunes.randomBetween(10, 30);
        int robado;
        synchronized (lock) {
            robado = (cantidadActual * porcentaje) / 100;
            cantidadActual -= robado;
        }

        centro.restarRecurso(tipo, robado);

        Log.log("¡SAQUEO en " + tipo + "! Se han robado " + robado + " unidades por " + b.getIdBarbaro() + ". Restante: " + cantidadActual);

        eliminarBarbaro(b);

        int comida = centro.getRecurso("COMIDA").get();
        int madera = centro.getRecurso("MADERA").get();
        int oro = centro.getRecurso("ORO").get();
        Log.log("ESTADO RECURSOS => Comida: " + comida + ", Madera: " + madera + ", Oro: " + oro);
    }

    public void añadirInicial(int cantidad) {
        synchronized (lock) {
            cantidadActual = Math.min(cantidad, capacidadMaxima);
            Log.log("Almacén de " + tipo + " inicializado con " + cantidadActual);
        }
    }

    public List<Aldeano> getAldeanos() {
        synchronized (lock) {
            List<Aldeano> todosAldeanos = new ArrayList<>();
            todosAldeanos.addAll(aldeanosDepositando);
            todosAldeanos.addAll(aldeanosEsperando);
            return todosAldeanos;
        }
    }

    public List<Guerrero> getGuerreros() {
        return guerreros;
    }

    public String obtenerEstadoAldeanos() {
        synchronized (lock) {
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

            StringBuilder guerr = new StringBuilder();
            for (Guerrero g : getGuerreros()) {
                guerr.append(g.getIdGuerrero()).append(", ");
            }
            if (guerr.length() > 0) guerr.setLength(guerr.length() - 2);

            StringBuilder barb = new StringBuilder();
            for (Barbaro b : getBarbaros()) {
                barb.append(b.getIdBarbaro()).append(", ");
            }
            if (barb.length() > 0) barb.setLength(barb.length() - 2);

            return "Depositando: " + dentro + "\nEsperando: " + esperando + "\nGuerreros: " + guerr + "\nBárbaros: " + barb;
        }
    }

    // Sacar a los aldeanos del almacén cuando se pulsa el botón de alarma
    public void liberarAldeanos() {
        synchronized (lock) {
            aldeanosDepositando.clear();
            aldeanosEsperando.clear();
        }
    }
    public synchronized void expulsarAldeanos() {
        for (Aldeano a : aldeanosDepositando) {
            areaRecuperacion.enviarAldeano(a,12000,15000);
            a.interrupt();
        }
        for (Aldeano a : aldeanosEsperando) {
            areaRecuperacion.enviarAldeano(a,12000,15000);
            a.interrupt();
        }
        aldeanosDepositando.clear();
        aldeanosEsperando.clear();
    }

    public void salir(Aldeano aldeano) {
        synchronized (lock) {
            aldeanosDepositando.remove(aldeano);
            aldeanosEsperando.remove(aldeano);
            Log.log(aldeano.getIdAldeano() + " ha salido del almacén de " + tipo);
        }
    }

    public synchronized void agregarBarbaro(Barbaro b) {
        if (!barbarosAtacando.contains(b)) {
            barbarosAtacando.add(b);
            Log.log("Barbaro " + b.getIdBarbaro() + " ha empezado a saquear el almacén de " + tipo);
        }
    }

    public synchronized void eliminarBarbaro(Barbaro b) {
        if (barbarosAtacando.remove(b)) {
            Log.log("Barbaro " + b.getIdBarbaro() + " ha terminado de saquear el almacén de " + tipo);
        }
    }

    public synchronized List<Barbaro> getBarbaros() {
        return new ArrayList<>(barbarosAtacando);
    }
}
