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
    private final Random random = new Random();

    public Almacen(String tipo, int capacidad, CentroUrbano centro) {
        this.tipo = tipo;
        this.capacidadMaxima = capacidad;
        this.cantidadActual = 0;
        this.centro = centro;
    }

    public int getCapacidadMaxima() {
        synchronized (lock) {
            return capacidadMaxima;
        }
    }
    public int getCantidadActual() {
        synchronized (lock) {
            return cantidadActual;
        }
    }

    // --- Interfaz Zona ---
    public boolean entrarGuerrero(Guerrero g) throws InterruptedException {
        guerreros.add(g);
        return true;
    }

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
                Thread.sleep(1000);
                return true;
            }

            Guerrero defensor = defensoresDisponibles.get(new Random().nextInt(defensoresDisponibles.size()));
            defensor.setLuchando(true);
            Log.log(b.getIdBarbaro() + " se enfrenta a " + defensor.getIdGuerrero());

            Thread.sleep(FuncionesComunes.Tiempoaleatorio(500, 1000));

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
        if (cantidad <= 0) {
            Log.log("Error: intento de depositar cantidad no positiva: " + cantidad);
            return;
        }

        centro.esperarSiPausado();

        int restante = cantidad;

        while (restante > 0) {
            int tiempoDeposito;
            int aDepositar;

            synchronized (lock) {
                centro.esperarSiPausado();

                // Recalcular espacio siempre dentro del synchronized justo antes de depositar
                int espacio = capacidadMaxima - cantidadActual;

                // Esperar mientras no haya espacio para depositar
                while (espacio == 0) {
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                        Log.log("El almacén de " + tipo + " está lleno. El aldeano " + aldeano.getIdAldeano() + " espera para depositar.");
                    }
                    lock.wait();
                    centro.esperarSiPausado();
                    espacio = capacidadMaxima - cantidadActual; // recalcular después del wait
                }

                // Ahora calcular cuánto se puede depositar sin pasarse del maximo
                aDepositar = Math.min(espacio, restante);

                // Esta defensa es por si acaso
                if (aDepositar <= 0) {
                    aDepositar = 0;
                }

                aldeanosEsperando.remove(aldeano);
                if (!aldeanosDepositando.contains(aldeano)) {
                    aldeanosDepositando.add(aldeano);
                }

                tiempoDeposito = FuncionesComunes.Tiempoaleatorio(2000, 3000);
            }

            Thread.sleep(tiempoDeposito);
            centro.esperarSiPausado();

            synchronized (lock) {
                // Volver a validar que no se pase (por si otro hilo cambió cantidadActual entre medias)
                int espacio = capacidadMaxima - cantidadActual;
                if (aDepositar > espacio) {
                    aDepositar = espacio;
                }

                cantidadActual += aDepositar;
                restante -= aDepositar;

                Log.log("El aldeano " + aldeano.getIdAldeano() + " ha depositado " + aDepositar + " de " + tipo + ". Total almacenado: " + cantidadActual);

                if (restante > 0) {
                    Log.log("El aldeano " + aldeano.getIdAldeano() + " se queda esperando con " + restante + " de " + tipo + " por falta de espacio.");
                    aldeanosDepositando.remove(aldeano);
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                    }
                } else {
                    aldeanosDepositando.remove(aldeano);
                }
                lock.notifyAll();
            }
        }

        synchronized (lock) {
            aldeanosDepositando.remove(aldeano);
            aldeanosEsperando.remove(aldeano);
            lock.notifyAll();
        }
    }
    public void consumir(int cantidad) {
        synchronized (lock) {
            if (cantidadActual >= cantidad) {
                cantidadActual -= cantidad;
            } else {
                throw new IllegalStateException("Intento de consumir más de lo disponible en " + tipo);
            }
        }
    }

    public void aumentarCapacidad(int cantidad) {
        synchronized (lock) {
            capacidadMaxima += cantidad;
            Log.log("Capacidad de " + tipo + " aumentada a " + capacidadMaxima);
            lock.notifyAll();
        }
    }
    public void saquear(Barbaro b) {
        agregarBarbaro(b);
        try {
            Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
        } catch (InterruptedException e) {
            Log.log("Error en el saqueo: " + e.getMessage());
        }

        int porcentaje = 10 + random.nextInt(21);
        int robado;

        synchronized (lock) {
            robado = (cantidadActual * porcentaje) / 100;
            cantidadActual -= robado;
        }

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
