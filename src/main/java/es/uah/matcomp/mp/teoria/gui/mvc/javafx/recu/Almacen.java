package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Almacen implements Zona {
    private final String tipo;
    private int cantidadActual;
    private int capacidadMaxima;
    private final CentroUrbano centro;
    private final Object lock = new Object();
    private final List<Aldeano> aldeanosDepositando = new ArrayList<>();
    private final List<Aldeano> aldeanosEsperando = new ArrayList<>();
    private final List<Guerrero> guerreros = new ArrayList<>();
    private final List<Barbaro> barbarosAtacando = new ArrayList<>();

    // Constructor
    public Almacen(String tipo, int capacidad, CentroUrbano centro) {
        this.tipo = tipo;
        this.capacidadMaxima = capacidad;
        this.cantidadActual = 0;
        this.centro = centro;
    }

    // Getter
    public int getCapacidadMaxima() {
        synchronized (lock) {
            return capacidadMaxima;
        }
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
        synchronized (guerreros) {
            List<Guerrero> defensoresDisponibles = new ArrayList<>();
            for (Guerrero g : guerreros) {
                if (!g.estaLuchando()) {
                    defensoresDisponibles.add(g);
                }
            }
            if (defensoresDisponibles.isEmpty()) {
                // No hay defensores, espera 1 segundo y sigue al saqueo
                Thread.sleep(1000);
                return true; //continuar al saqueo
            }
            // Elegir uno aleatorio disponible
            Guerrero defensor = defensoresDisponibles.get(new Random().nextInt(defensoresDisponibles.size()));
            defensor.setLuchando(true);
            Log.log(b.getIdBarbaro() + " se enfrenta a " + defensor.getIdGuerrero());

            Thread.sleep(FuncionesComunes.randomBetween(500, 1000));

            double probVictoriaBarbaro = 0.5 - (0.05 * Math.min(5, centro.getGestorMejoras().getNivelArmas()));
            boolean ganaBarbaro = Math.random() < probVictoriaBarbaro;

            if (ganaBarbaro) {
                Log.log(b.getIdBarbaro() + " derrota a " + defensor.getIdGuerrero());
                guerreros.remove(defensor);
                defensor.enviarARecuperacion();
                return true;
            } else {
                Log.log(defensor.getIdGuerrero() + " derrota a " + b.getIdBarbaro());
                defensor.setLuchando(false);
                return false;
            }
        }
    }
    public void depositar(Aldeano aldeano, int cantidad) throws InterruptedException {
        centro.esperarSiPausado();

        int restante = cantidad;

        while (restante > 0) {
            int tiempoDeposito = 0;
            int aDepositar = 0;

            synchronized (lock) {
                centro.esperarSiPausado();

                // Esperar si el almacén está lleno
                while (cantidadActual >= capacidadMaxima) {
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                        Log.log("El almacén de " + tipo + " está lleno. El aldeano " + aldeano.getIdAldeano() + " espera para depositar.");
                    }
                    lock.wait();
                    centro.esperarSiPausado();
                }

                // Calcular cuánto se puede depositar
                int espacio = capacidadMaxima - cantidadActual;
                aDepositar = Math.min(espacio, restante);

                // nunca permitir depositar 0 o negativo
                if (aDepositar <= 0) {
                    // No hay nada que hacer aún, volver a esperar
                    continue;
                }

                // Preparar depósito
                aldeanosEsperando.remove(aldeano);
                aldeanosDepositando.add(aldeano);
                tiempoDeposito = FuncionesComunes.randomBetween(2000, 3000);
            }

            // Simular el tiempo fuera del lock
            Thread.sleep(tiempoDeposito);
            centro.esperarSiPausado();

            synchronized (lock) {
                cantidadActual += aDepositar;
                restante -= aDepositar;
                centro.sumarRecurso(tipo, aDepositar);

                Log.log("El aldeano " + aldeano.getIdAldeano() + " ha depositado " + aDepositar + " de " + tipo + ". Total almacenado: " + cantidadActual);

                if (restante > 0) {
                    Log.log("El aldeano " + aldeano.getIdAldeano() + " se queda esperando con " + restante + " de " + tipo + " por falta de espacio.");
                    aldeanosDepositando.remove(aldeano);
                    aldeanosEsperando.add(aldeano);
                } else {
                    aldeanosDepositando.remove(aldeano);
                }

                lock.notifyAll();
            }
        }

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

        // Simulación del saqueo
        try {
            Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
        } catch (InterruptedException e) {
            Log.log("Error en el saqueo: " + e.getMessage());
        }

        int porcentaje = FuncionesComunes.randomBetween(10, 30);
        int robado;
        synchronized (lock) {
            robado = (cantidadActual * porcentaje) / 100;
            cantidadActual -= robado;
        }

        centro.restarRecurso(tipo, robado);

        Log.log("¡SAQUEO en " + tipo + "! Se han robado " + robado + " unidades por " + b.getIdBarbaro() + ". Restante: " + cantidadActual);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.log("Error en el saqueo: " + e.getMessage());
        }
        eliminarBarbaro(b);

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
            for (Guerrero g : guerreros) {
                guerr.append(g.getIdGuerrero()).append(", ");
            }
            if (guerr.length() > 0) guerr.setLength(guerr.length() - 2);

            StringBuilder barb = new StringBuilder();
            for (Barbaro b : barbarosAtacando) {
                barb.append(b.getIdBarbaro()).append(", ");
            }
            if (barb.length() > 0) barb.setLength(barb.length() - 2);

            return "Depositando: " + dentro + "\nEsperando: " + esperando + "\nGuerreros: " + guerr + "\nBárbaros: " + barb;
        }
    }
    // Sacar a los aldeanos del almacén cuando se pulsa el botón de alarma

    public synchronized void expulsarAldeanos() {
        List<Aldeano> copiaDepositando = new ArrayList<>(aldeanosDepositando);
        List<Aldeano> copiaEsperando = new ArrayList<>(aldeanosEsperando);

        for (Aldeano a : copiaDepositando) {
            a.interrupt();
        }
        for (Aldeano a : copiaEsperando) {
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

    public List<Barbaro> getBarbaros() {
        synchronized (lock) {
            return new ArrayList<>(barbarosAtacando);
        }
    }
}
