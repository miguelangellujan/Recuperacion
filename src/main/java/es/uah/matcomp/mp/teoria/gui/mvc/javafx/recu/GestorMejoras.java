package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class GestorMejoras {

    private int nivelHerramientas = 0;
    private int nivelArmas = 0;
    private int nivelAlmacenes = 0;
    private final CentroUrbano centro;

    // Constructor
    public GestorMejoras(CentroUrbano centro) {
        this.centro = centro;
    }

    public int getNivelHerramientas() {return nivelHerramientas;}

    public int getNivelArmas() {return nivelArmas;}

    public int getNivelAlmacenes() {return nivelAlmacenes;}

    //Funciones asociadas a la interfaz

    public synchronized void aplicarMejoraHerramientas() {
        int costoMadera = 120;
        int costoOro = 80;

        if (nivelHerramientas >= 3) {
            Log.log("Se alcanzó el nivel máximo de mejora en herramientas.");
            return;
        }

        Almacen madera = centro.getAserradero();
        Almacen oro = centro.getTesoreria();

        synchronized (madera) {
            synchronized (oro) {
                if (madera.getCantidadActual() >= costoMadera && oro.getCantidadActual() >= costoOro) {
                    madera.consumir(costoMadera);
                    oro.consumir(costoOro);
                    nivelHerramientas++;
                    Log.log("Mejora de herramientas aplicada al nivel " + nivelHerramientas);
                } else {
                    Log.log("No hay recursos suficientes para mejorar herramientas.");
                }
            }
        }
    }

    public synchronized void aplicarMejoraArmas() {
        int costoComida = 150;
        int costoOro = 100;

        if (nivelArmas >= 5) {
            Log.log("Se alcanzó el nivel máximo de mejora en armas.");
            return;
        }

        Almacen comida = centro.getGranero();
        Almacen oro = centro.getTesoreria();

        synchronized (comida) {
            synchronized (oro) {
                if (comida.getCantidadActual() >= costoComida && oro.getCantidadActual() >= costoOro) {
                    comida.consumir(costoComida);
                    oro.consumir(costoOro);
                    nivelArmas++;
                    Log.log("Mejora de armas aplicada al nivel " + nivelArmas);
                } else {
                    Log.log("No hay recursos suficientes para mejorar armas.");
                }
            }
        }
    }

    public synchronized void aplicarMejoraAlmacenes() {
        int costoMadera = 150;
        int costoOro = 50;

        if (nivelAlmacenes >= 3) {
            Log.log("Se alcanzó el nivel máximo de mejora en almacenes.");
            return;
        }

        Almacen madera = centro.getAserradero();
        Almacen oro = centro.getTesoreria();

        synchronized (madera) {
            synchronized (oro) {
                if (madera.getCantidadActual() >= costoMadera && oro.getCantidadActual() >= costoOro) {
                    madera.consumir(costoMadera);
                    oro.consumir(costoOro);
                    nivelAlmacenes++;

                    for (Almacen almacen : centro.getAlmacenes()) {
                        almacen.aumentarCapacidad(100);
                        Log.log("Capacidad aumentada en " + almacen.getNombreZona() + " al nivel " + nivelAlmacenes);
                    }

                    Log.log("Mejora de almacenes aplicada al nivel " + nivelAlmacenes);
                } else {
                    Log.log("No hay recursos suficientes para mejorar almacenes.");
                }
            }
        }
    }
}