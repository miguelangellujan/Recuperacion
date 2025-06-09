package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CentroUrbano {
    private final AtomicInteger comida = new AtomicInteger(50);
    private final AtomicInteger madera = new AtomicInteger(30);
    private final AtomicInteger oro = new AtomicInteger(20);

    private final AtomicInteger idAldeano = new AtomicInteger(1);
    private final AtomicInteger idGuerrero = new AtomicInteger(1);
    private final AtomicInteger idBarbaro = new AtomicInteger(1);

    public final AreaRecurso granja = new AreaRecurso("COMIDA");
    public final AreaRecurso bosque = new AreaRecurso("MADERA");
    public final AreaRecurso mina = new AreaRecurso("ORO");

    public final Almacen granero = new Almacen("COMIDA", 200, this);
    public final Almacen aserradero = new Almacen("MADERA", 150, this);
    public final Almacen tesoreria = new Almacen("ORO", 50, this);

    public final AreaRecuperacion areaRecuperacion = new AreaRecuperacion();
    public final ZonaPreparacionBarbaros zonaPreparacion = new ZonaPreparacionBarbaros(this);

    private final CasaPrincipal casaPrincipal = new CasaPrincipal();
    private final PlazaCentral plazaCentral = new PlazaCentral();
    private final Cuartel cuartel = new Cuartel();

    private final List<Aldeano> aldeanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Guerrero> guerreros = Collections.synchronizedList(new ArrayList<>());
    private final List<Barbaro> barbaros = Collections.synchronizedList(new ArrayList<>());

    private final GestorMejoras mejoras = new GestorMejoras();
    private final AtomicBoolean emergenciaActiva = new AtomicBoolean(false);

    private boolean enEjecucion = true;
    private Paso paso = new Paso();

    public CentroUrbano() {
        granero.añadirInicial(comida.get());
        aserradero.añadirInicial(madera.get());
        tesoreria.añadirInicial(oro.get());
        for (int i = 0; i < 2; i++) {
            String id = String.format("A%03d", idAldeano.getAndIncrement());
            Aldeano a = new Aldeano(id, this);
            a.setEmergencia(emergenciaActiva.get());
            aldeanos.add(a);
            Log.log("Se ha creado el aldeano " + id + " (inicio sin coste)");
            a.start();
        }
    }

    public synchronized void crearAldeano() {
        if (comida.get() >= 50) {
            comida.addAndGet(-50);
            String id = String.format("A%03d", idAldeano.getAndIncrement());
            Aldeano a = new Aldeano(id, this);
            a.setEmergencia(emergenciaActiva.get());
            aldeanos.add(a);
            a.start();
            Log.log("Se ha creado el aldeano " + id);
        } else {
            Log.log("No hay comida suficiente para crear aldeano.");
        }
    }

    public synchronized void crearGuerrero() {
        if (comida.get() >= 50 && madera.get() >= 50 && oro.get() >= 80) {
            comida.addAndGet(-50);
            madera.addAndGet(-50);
            oro.addAndGet(-80);
            String id = String.format("G%03d", idGuerrero.getAndIncrement());
            Guerrero g = new Guerrero(id, this);
            guerreros.add(g);
            g.start();
            Log.log("Se ha entrenado el guerrero " + id);
        } else {
            Log.log("No hay recursos suficientes para crear guerrero.");
        }
    }

    public void crearBarbaro() {
        String id = String.format("B%03d", idBarbaro.getAndIncrement());
        Barbaro b = new Barbaro(id, this);
        barbaros.add(b);
        b.start();
        Log.log("Se ha generado el bárbaro " + id);
    }

    public String seleccionarRecursoAleatorio() {
        String[] tipos = {"COMIDA", "MADERA", "ORO"};
        return tipos[new Random().nextInt(tipos.length)];
    }

    public AreaRecurso getArea(String tipo) {
        return switch (tipo) {
            case "COMIDA" -> granja;
            case "MADERA" -> bosque;
            case "ORO" -> mina;
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
    }

    public Almacen getAlmacen(String tipo) {
        return switch (tipo) {
            case "COMIDA" -> granero;
            case "MADERA" -> aserradero;
            case "ORO" -> tesoreria;
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
    }

    public void activarEmergencia() {
        boolean nuevoEstado = !emergenciaActiva.get();
        emergenciaActiva.set(nuevoEstado);
        if (nuevoEstado) {
            Log.log("¡Emergencia activada! Los aldeanos regresan a CASA PRINCIPAL.");
        } else {
            Log.log("¡Emergencia desactivada! Los aldeanos retoman su trabajo.");
        }
        for (Aldeano a : aldeanos) {
            a.setEmergencia(nuevoEstado);
            synchronized (a) {
                a.notify();
            }
        }
    }

    public void entrenar(Guerrero g) throws InterruptedException {
        cuartel.entrenar(g);
    }

    public Zona obtenerZonaAleatoria() {
        List<Zona> zonas = List.of(granja, bosque, mina, granero, aserradero, tesoreria);
        return zonas.get(new Random().nextInt(zonas.size()));
    }

    public Zona seleccionarObjetivo() {
        if (Math.random() < 0.6) {
            List<Zona> almacenes = List.of(granero, aserradero, tesoreria);
            return almacenes.get(new Random().nextInt(almacenes.size()));
        } else {
            List<Zona> areas = List.of(granja, bosque, mina);
            return areas.get(new Random().nextInt(areas.size()));
        }
    }

    public Zona obtenerZonaAleatoriaParaPatrulla() {
        return obtenerZonaAleatoria();
    }

    public void realizarAtaque(Barbaro b, Zona zona) {
        try {
            zona.enfrentarABarbaro(b);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public AreaRecuperacion getAreaRecuperacion() {
        return areaRecuperacion;
    }

    public boolean isEmergenciaActiva() {
        return emergenciaActiva.get();
    }

    public GestorMejoras getGestorMejoras() {
        return mejoras;
    }

    public AtomicInteger getRecurso(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "COMIDA" -> comida;
            case "MADERA" -> madera;
            case "ORO" -> oro;
            default -> throw new IllegalArgumentException("Recurso inválido: " + tipo);
        };
    }

    public void sumarRecurso(String tipo, int cantidad) {
        switch (tipo) {
            case "COMIDA" -> comida.updateAndGet(current -> Math.min(current + cantidad, granero.getCapacidadMaxima()));
            case "MADERA" -> madera.updateAndGet(current -> Math.min(current + cantidad, aserradero.getCapacidadMaxima()));
            case "ORO" -> oro.updateAndGet(current -> Math.min(current + cantidad, tesoreria.getCapacidadMaxima()));
            default -> throw new IllegalArgumentException("Tipo de recurso inválido: " + tipo);
        }
    }

    public void restarRecurso(String tipo, int cantidad) {
        switch (tipo) {
            case "COMIDA" -> comida.updateAndGet(current -> Math.max(current - cantidad, 0));
            case "MADERA" -> madera.updateAndGet(current -> Math.max(current - cantidad, 0));
            case "ORO" -> oro.updateAndGet(current -> Math.max(current - cantidad, 0));
            default -> throw new IllegalArgumentException("Tipo de recurso inválido: " + tipo);
        }
    }

    public String obtenerIdsAldeanos() {
        synchronized (aldeanos) {
            return aldeanos.stream().map(Aldeano::getIdAldeano).reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String obtenerIdsGuerreros() {
        synchronized (guerreros) {
            return guerreros.stream().map(Guerrero::getIdGuerrero).reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String obtenerIdsBarbaros() {
        synchronized (barbaros) {
            return barbaros.stream().map(Barbaro::getIdBarbaro).reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String obtenerIdsCasaPrincipal() {
        return casaPrincipal.obtenerIds();
    }

    public String obtenerIdsPlazaCentral() {
        return plazaCentral.obtenerIds();
    }

    public ZonaPreparacionBarbaros getZonaPreparacion() {
        return zonaPreparacion;
    }

    public CasaPrincipal getCasaPrincipal() {
        return casaPrincipal;
    }

    public PlazaCentral getPlazaCentral() {
        return plazaCentral;
    }

    public Cuartel getCuartel() {
        return cuartel;
    }

    public int contarAldeanos() {
        return aldeanos.size();
    }

    public int contarGuerreros() {
        return guerreros.size();
    }

    public int contarBarbarosCampamento() {
        return barbaros.size();
    }

    public Almacen getGranero() {
        return granero;
    }

    public Almacen getAserradero() {
        return aserradero;
    }

    public Almacen getTesoreria() {
        return tesoreria;
    }

    // Boton Pausa
    public synchronized void ejecucion(){
        enEjecucion = !enEjecucion;

        if (enEjecucion){
            paso.abrir();
            System.out.println("Simulación Reanudada");
        } else {
            paso.cerrar();
            System.out.println("Simulación Detenida");
        }
    }

    public boolean estadoEjecucion(){
        return enEjecucion;
    }

    public static class CasaPrincipal {
        private final List<String> aldeanosEnCasa = Collections.synchronizedList(new ArrayList<>());

        public void registrarEntrada(String idAldeano) {
            aldeanosEnCasa.add(idAldeano);
            Log.log("El aldeano " + idAldeano + " ha ingresado a la Casa Principal.");
        }

        public void salir(String idAldeano) {
            aldeanosEnCasa.remove(idAldeano);
            Log.log("El aldeano " + idAldeano + " ha salido de la Casa Principal.");
        }

        public String obtenerIds() {
            synchronized (aldeanosEnCasa) {
                return aldeanosEnCasa.isEmpty() ? "Ninguno" : String.join(", ", aldeanosEnCasa);
            }
        }
    }

    public static class PlazaCentral {
        private final List<String> aldeanosEnPlaza = Collections.synchronizedList(new ArrayList<>());

        public void planificar(String idAldeano) {
            aldeanosEnPlaza.add(idAldeano);
            Log.log("El aldeano " + idAldeano + " está planificando en la Plaza Central.");
        }

        public void salir(String idAldeano) {
            aldeanosEnPlaza.remove(idAldeano);
            Log.log("El aldeano " + idAldeano + " ha salido de la Plaza Central.");
        }

        public String obtenerIds() {
            synchronized (aldeanosEnPlaza) {
                return aldeanosEnPlaza.isEmpty() ? "Ninguno" : String.join(", ", aldeanosEnPlaza);
            }
        }
    }

    public static class Cuartel {
        public void entrenar(Guerrero g) throws InterruptedException {
            Log.log("El guerrero " + g.getIdGuerrero() + " se entrena en el Cuartel.");
            Thread.sleep(5000 + (int) (Math.random() * 3000)); // Entre 5 y 8 segundos
            Log.log("El guerrero " + g.getIdGuerrero() + " ha finalizado su entrenamiento.");
        }
    }
}
