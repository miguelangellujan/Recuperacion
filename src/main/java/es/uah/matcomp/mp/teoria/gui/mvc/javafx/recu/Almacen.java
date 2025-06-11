package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Almacen implements Zona {
    private final String tipo;
    private int cantidadActual;
    private int capacidadMaxima;
    private final CentroUrbano centro;

    private final AtomicInteger semDeposito = new AtomicInteger(3);
    private Object lock = new Object();
    private final List<Aldeano> aldeanosDepositando = new ArrayList<>();
    private final List<Aldeano> aldeanosEsperando = new ArrayList<>();

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

    public int getCapacidadMaxima(){
        return capacidadMaxima;
    }

    // Funciones Interface Zona
    @Override
    public boolean entrarGuerrero(Guerrero g) throws InterruptedException {
        // En el almacén no se necesita controlar la entrada de guerreros de forma específica.
        // Se retorna true para cumplir con el contrato de la interfaz Zona.
        return true;
    }

    @Override
    public void salirGuerrero(Guerrero g) {
        // Para el almacén, no se requiere ninguna acción especial al salir un guerrero.
        // Este método se implementa como un stub vacío para satisfacer la interfaz.
    }

    @Override
    public String getNombreZona() {
        return "Almacén de " + tipo;
    }

    @Override
    public boolean enfrentarABarbaro(Barbaro b) throws InterruptedException {
        // En el contexto del almacén, el método de combate no se utiliza directamente.
        // Se retorna true para indicar que el almacén "permite" que el ataque continúe
        // (en la práctica, el ataque se gestiona mediante el método saquear()).
        return true;
    }

    public void depositar(Aldeano aldeano, int cantidad) throws InterruptedException {
        int restante = cantidad;
        synchronized (lock){
            while (restante > 0) {
                // Si no hay espacio, esperar bloqueando
                while (cantidadActual == capacidadMaxima) {
                    if (!aldeanosEsperando.contains(aldeano)) {
                        aldeanosEsperando.add(aldeano);
                        Log.log("El almacén de " + tipo + " está lleno. El aldeano " + aldeano.getIdAldeano() + " espera para depositar.");
                    }
                    lock.wait(); // espera hasta que haya espacio
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
                lock.notifyAll();
            }
        }
    }

    public void aumentarCapacidad(int cantidad) {
        synchronized (lock){
            capacidadMaxima += cantidad;
            Log.log("Capacidad de " + tipo + " aumentada a " + capacidadMaxima);
            lock.notifyAll(); // Avisamos a los aldeanos por si ahora hay hueco
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
        synchronized (lock){
            cantidadActual = Math.min(cantidad, capacidadMaxima);
            Log.log("Almacén de " + tipo + " inicializado con " + cantidadActual);
        }
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

            return "Depositando: " + dentro + "\n\nEsperando: " + esperando;
        }
    }

    // Sacar a los aldeanos del almacén cuando se pulsa el botón de alarma
    public void liberarAldeanos(){
        synchronized (lock){
            aldeanosDepositando.clear();
            aldeanosEsperando.clear();
        }
    }

    public void salir(Aldeano aldeano) {
        synchronized (lock) {
            aldeanosDepositando.remove(aldeano);
            aldeanosEsperando.remove(aldeano);
            Log.log(aldeano.getIdAldeano() + " ha salido del almacén de " + tipo);
        }
    }
}
