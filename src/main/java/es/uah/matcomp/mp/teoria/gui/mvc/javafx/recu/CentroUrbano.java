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

    private final AreaRecurso granja = new AreaRecurso("COMIDA");
    private final AreaRecurso bosque = new AreaRecurso("MADERA");
    private final AreaRecurso mina = new AreaRecurso("ORO");

    private final Almacen granero = new Almacen("COMIDA", 200, this);
    private final Almacen aserradero = new Almacen("MADERA", 150, this);
    private final Almacen tesoreria = new Almacen("ORO", 50, this);

    private final AreaRecuperacion areaRecuperacion = new AreaRecuperacion();
    private final ZonaPreparacionBarbaros zonaPreparacion = new ZonaPreparacionBarbaros(this);

    private final CasaPrincipal casaPrincipal = new CasaPrincipal();
    private final PlazaCentral plazaCentral = new PlazaCentral();
    private final Cuartel cuartel = new Cuartel();

    private final List<Aldeano> aldeanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Guerrero> guerreros = Collections.synchronizedList(new ArrayList<>());
    private final List<Barbaro> barbaros = Collections.synchronizedList(new ArrayList<>());

    private final GestorMejoras mejoras;
    private final AtomicBoolean emergenciaActiva = new AtomicBoolean(false);

    // Constructor
    public CentroUrbano() {
        mejoras = new GestorMejoras(this);
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

    // Geters para acceso a variables
    public List<Aldeano> getAldeanos() {
        return aldeanos;
    }

    public AreaRecuperacion getAreaRecuperacion() {
        return areaRecuperacion;
    }

    public GestorMejoras getGestorMejoras() {
        return mejoras;
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

    public Almacen getGranero() {
        return granero;
    }

    public Almacen getAserradero() {
        return aserradero;
    }

    public Almacen getTesoreria() {
        return tesoreria;
    }

    // Crear Individuos
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


    // Funciones relacionadas con los recursos
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

    // Obtener ids para la actualización de la interfaz
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
    //CENTRO URBANO
    public String obtenerIdsAldeanosEnCentroUrbano() {
        List<String> ids = new ArrayList<>();
        synchronized (casaPrincipal) {
            ids.addAll(casaPrincipal.getAldeanos().stream().map(Aldeano::getIdAldeano).toList());
        }
        synchronized (plazaCentral) {
            ids.addAll(plazaCentral.getAldeanos().stream().map(Aldeano::getIdAldeano).toList());
        }
        synchronized (areaRecuperacion) {
            ids.addAll(areaRecuperacion.getAldeanos().stream().map(Aldeano::getIdAldeano).toList());
        }
        return ids.isEmpty() ? "Ninguno" : String.join(", ", ids);
    }

    public int contarAldeanosEnCentroUrbano() {
        String ids = obtenerIdsAldeanosEnCentroUrbano();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    public String obtenerIdsGuerrerosEnCentroUrbano() {
        List<String> ids = new ArrayList<>();
        synchronized (cuartel) {
            ids.addAll(cuartel.getGuerreros().stream().map(Guerrero::getIdGuerrero).toList());
        }
        synchronized (areaRecuperacion) {
            ids.addAll(areaRecuperacion.getGuerreros().stream().map(Guerrero::getIdGuerrero).toList());
        }
        return ids.isEmpty() ? "Ninguno" : String.join(", ", ids);
    }

    public int contarGuerrerosEnCentroUrbano() {
        String ids = obtenerIdsGuerrerosEnCentroUrbano();
        return ids.equals("Ninguno") ? 0 : ids.split(", ").length;
    }

    //GRANJA
    public String obtenerIdsAldeanosEnGranja() {
        synchronized (granja) {
            return granja.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public int contarAldeanosEnGranja() {
        return obtenerIdsAldeanosEnGranja().equals("Ninguno") ? 0 : obtenerIdsAldeanosEnGranja().split(", ").length;
    }
    public int contarGuerrerosEnGranja() {
        return obtenerIdsGuerrerosEnGranja().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEnGranja().split(", ").length;
    }

    public String obtenerIdsGuerrerosEnGranja() {
        synchronized (granja) {
            return granja.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    //BOSQUE
    public String obtenerIdsAldeanosEnBosque() {
        synchronized (bosque) {
            return bosque.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarAldeanosEnBosque() {
        return obtenerIdsAldeanosEnBosque().equals("Ninguno") ? 0 : obtenerIdsAldeanosEnBosque().split(", ").length;
    }
    public String obtenerIdsGuerrerosEnBosque() {
        synchronized (bosque) {
            return bosque.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarGuerrerosEnBosque() {
        return obtenerIdsGuerrerosEnBosque().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEnBosque().split(", ").length;
    }
    //MINA
    public String obtenerIdsAldeanosEnMina() {
        synchronized (mina) {
            return mina.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarAldeanosEnMina() {
        return obtenerIdsAldeanosEnMina().equals("Ninguno") ? 0 : obtenerIdsAldeanosEnMina().split(", ").length;
    }

    public String obtenerIdsGuerrerosEnMina() {
        synchronized (mina) {
            return mina.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarGuerrerosEnMina() {
        return obtenerIdsGuerrerosEnMina().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEnMina().split(", ").length;
    }

    //TESORERÍA
    public String obtenerIdsAldeanosEntesoreria() {
        synchronized (tesoreria) {
            return tesoreria.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarAldeanosEntesoreria() {
        return obtenerIdsAldeanosEntesoreria().equals("Ninguno") ? 0 : obtenerIdsAldeanosEntesoreria().split(", ").length;
    }

    public String obtenerIdsGuerrerosEntesoreria() {
        synchronized (tesoreria) {
            return tesoreria.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarGuerrerosEntesoreria() {
        return obtenerIdsGuerrerosEntesoreria().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEntesoreria().split(", ").length;
    }
    //GRANERO
    public String obtenerIdsAldeanosEngranero() {
        synchronized (granero) {
            return granero.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarAldeanosEngranero() {
        return obtenerIdsAldeanosEngranero().equals("Ninguno") ? 0 : obtenerIdsAldeanosEngranero().split(", ").length;
    }

    public String obtenerIdsGuerrerosEngranero() {
        synchronized (granero) {
            return granero.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarGuerrerosEngranero() {
        return obtenerIdsGuerrerosEngranero().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEngranero().split(", ").length;
    }
    //ASERRADERO
    public String obtenerIdsAldeanosEnaserradero() {
        synchronized (aserradero) {
            return aserradero.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarAldeanosEnaserradero() {
        return obtenerIdsAldeanosEnaserradero().equals("Ninguno") ? 0 : obtenerIdsAldeanosEnaserradero().split(", ").length;
    }

    public String obtenerIdsGuerrerosEnaserradero() {
        synchronized (aserradero) {
            return aserradero.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarGuerrerosEnaserradero() {
        return obtenerIdsGuerrerosEnaserradero().equals("Ninguno") ? 0 : obtenerIdsGuerrerosEnaserradero().split(", ").length;
    }
    public String obtenerIdsBarbarosEnCampamento() {
        synchronized (barbaros) {
            List<Barbaro> esperando = zonaPreparacion.getBarbarosEsperando();

            return barbaros.stream()
                    .filter(b -> !esperando.contains(b) && !b.estaAtacando())
                    .map(Barbaro :: getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public int contarBarbarosEnCampamento(){
        return obtenerIdsBarbarosEnCampamento().length();
    }

    public String obtenerIdsCasaPrincipal() {
        return casaPrincipal.obtenerIds();
    }

    public String obtenerIdsPlazaCentral() {
        return plazaCentral.obtenerIds();
    }

    public int contarAldeanos() {
        return aldeanos.size();
    }

    public int contarGuerreros() {
        return guerreros.size();
    }

    public int contarBarbaros() {
        return barbaros.size();
    }

    // Funciones para el ataque
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

    // Emergencia
    public void setEmergencia(boolean estado) {
        for (Aldeano a : aldeanos) {
            a.setEmergencia(estado);  // Esto interrumpe a los aldeanos (mira la clase Aldeano)
            synchronized (a) {
                a.notify();  // Despierta a los aldeanos si están esperando
            }
        }
    }

    public boolean isEmergenciaActiva() {
        return emergenciaActiva.get();
    }

    public void activarEmergencia() {
        boolean nuevoEstado = !emergenciaActiva.get();
        emergenciaActiva.set(nuevoEstado);

        if (nuevoEstado) {
            Log.log("¡Emergencia activada! Los aldeanos regresan a CASA PRINCIPAL.");

            granero.liberarAldeanos();
            aserradero.liberarAldeanos();
            tesoreria.liberarAldeanos();

            for(Aldeano a : aldeanos){
                a.setEmergencia(true);
                a.moverACasaPrincipal();
            }
        } else {
            Log.log("¡Emergencia desactivada! Los aldeanos retoman su trabajo.");

            for(Aldeano a : aldeanos){
                a.setEmergencia(false);
                synchronized (a){
                    a.notify();
                }
            }
        }
    }

    // Clases
    public static class CasaPrincipal {
        private final List<String> aldeanosEnCasa = Collections.synchronizedList(new ArrayList<>());

        public void registrarEntrada(String idAldeano) {
            synchronized (aldeanosEnCasa){
                if(!aldeanosEnCasa.contains(idAldeano)){
                    aldeanosEnCasa.add(idAldeano);
                    Log.log("El aldeano " + idAldeano + " ha ingresado a la Casa Principal.");
                }
            }
        }

        public void salir(String idAldeano) {
            synchronized (aldeanosEnCasa){
                aldeanosEnCasa.remove(idAldeano);
                Log.log("El aldeano " + idAldeano + " ha salido de la Casa Principal.");
            }
        }

        public boolean estaRegistrado(String idAldeano) {
            synchronized (aldeanosEnCasa){
                return aldeanosEnCasa.contains(idAldeano);
            }
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
            synchronized (aldeanosEnPlaza){
                if(!aldeanosEnPlaza.contains(idAldeano)){
                    aldeanosEnPlaza.add(idAldeano);
                    Log.log("El aldeano " + idAldeano + " está planificando en la Plaza Central.");
                }
            }
        }

        public void salir(String idAldeano) {
            aldeanosEnPlaza.remove(idAldeano);
            Log.log("El aldeano " + idAldeano + " ha salido de la Plaza Central.");
        }

        public boolean estaRegistrado(String idAldeano){
            synchronized (aldeanosEnPlaza){
                return aldeanosEnPlaza.contains(idAldeano);
            }
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
