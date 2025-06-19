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

    private final AreaRecurso granja = new AreaRecurso("COMIDA", this);
    private final AreaRecurso bosque = new AreaRecurso("MADERA", this);
    private final AreaRecurso mina = new AreaRecurso("ORO", this);

    private final Almacen granero = new Almacen("COMIDA", 200, this);
    private final Almacen aserradero = new Almacen("MADERA", 150, this);
    private final Almacen tesoreria = new Almacen("ORO", 50, this);

    private final AreaRecuperacion areaRecuperacion = new AreaRecuperacion();
    private final ZonaPreparacionBarbaros zonaPreparacion = new ZonaPreparacionBarbaros(this);
    private final ZonaCampamentoBarbaros zonaCampamento = new ZonaCampamentoBarbaros();

    private final CasaPrincipal casaPrincipal = new CasaPrincipal();
    private final PlazaCentral plazaCentral = new PlazaCentral();
    private final Cuartel cuartel = new Cuartel();

    private final List<Aldeano> aldeanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Guerrero> guerreros = Collections.synchronizedList(new ArrayList<>());
    private final List<Barbaro> barbaros = Collections.synchronizedList(new ArrayList<>());

    private final GestorMejoras gestorMejoras;

    private final AtomicBoolean emergenciaActiva = new AtomicBoolean(false);

    private final AtomicBoolean pausado = new AtomicBoolean(false);
    private final Object pausaLock = new Object();
    private final Object emergenciaLock = new Object();


    // Constructor
    public CentroUrbano() {
        gestorMejoras = new GestorMejoras(this);
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

    // Getters

    public AreaRecuperacion getAreaRecuperacion() {
        return areaRecuperacion;
    }

    public GestorMejoras getGestorMejoras() {
        return gestorMejoras;
    }

    public int getNivelHerramientas() {
        return gestorMejoras.getNivelHerramientas();
    }

    public int getNivelArmas() {
        return gestorMejoras.getNivelArmas();
    }

    public int getNivelAlmacenes() {
        return gestorMejoras.getNivelAlmacenes();
    }

    public void getMejoraHerramientas() {
        gestorMejoras.aplicarMejoraHerramientas();
    }

    public void getMejoraArmas() {
        gestorMejoras.aplicarMejoraArmas();
    }

    public void getMejoraAlmacenes() {
        gestorMejoras.aplicarMejoraAlmacenes();
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

    public Object getPausaLock() {
        return pausaLock;
    }

    public List<Almacen> getAlmacenes() {
        return Arrays.asList(granero, aserradero, tesoreria);
    }

    // Crear Individuos
    public void crearAldeano() {
        synchronized (pausaLock) {
            while (isPausado()) {
                try {
                    pausaLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.log("Interrumpido esperando para crear aldeano: " + e.getMessage());
                    return;
                }
            }
        }

        Almacen almacenComida = getGranero();

        synchronized (this) {
            if (almacenComida.getCantidadActual() >= 50) {
                almacenComida.consumir(50);  // sin try-catch si no lanza excepción

                comida.set(almacenComida.getCantidadActual());

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
    }
    public void crearGuerrero() {
        synchronized (pausaLock) {
            while (isPausado()) {
                try {
                    pausaLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.log("Interrumpido esperando para crear guerrero: " + e.getMessage());
                    return;
                }
            }
        }

        Almacen almacenComida = getGranero();
        Almacen almacenOro = getTesoreria();

        synchronized (this) {
            if (almacenComida.getCantidadActual() >= 80 && almacenOro.getCantidadActual() >= 60) {
                almacenComida.consumir(80);  // asumiendo que consumir no lanza InterruptedException
                almacenOro.consumir(60);

                comida.set(almacenComida.getCantidadActual());
                oro.set(almacenOro.getCantidadActual());

                String id = String.format("G%03d", idGuerrero.getAndIncrement());
                Guerrero g = new Guerrero(id, this);
                guerreros.add(g);
                g.start();
                Log.log("Se ha entrenado el guerrero " + id);
            } else {
                Log.log("No hay recursos suficientes para crear guerrero.");
            }
        }
    }
    public void crearBarbaro() {
        synchronized (pausaLock) {
            while (isPausado()) {
                try {
                    pausaLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.log("Interrumpido esperando para crear bárbaro: " + e.getMessage());
                    return;
                }
            }
        }
        synchronized (this) {
            String id = String.format("B%03d", idBarbaro.getAndIncrement());
            Barbaro b = new Barbaro(id, this);
            barbaros.add(b);
            b.start();
            Log.log("Se ha generado el bárbaro " + id);
        }
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
        int valor = switch (tipo.toUpperCase()) {
            case "COMIDA" -> granero.getCantidadActual();
            case "MADERA" -> aserradero.getCantidadActual();
            case "ORO" -> tesoreria.getCantidadActual();
            default -> throw new IllegalArgumentException("Recurso inválido: " + tipo);
        };
        return new AtomicInteger(valor);
    }
    // Get Individuos
    public String getAldeanos() {
        Set<String> todos = new HashSet<>();

        // Casa Principal
        String[] enCasa = casaPrincipal.obtenerIds().split(", ");
        for (String id : enCasa) {
            if (!id.equals("Ninguno")) {
                todos.add(id);
            }
        }

        // Plaza Central
        String[] enPlaza = plazaCentral.obtenerIds().split(", ");
        for (String id : enPlaza) {
            if (!id.equals("Ninguno")) {
                todos.add(id);
            }
        }

        // Área de Recuperación
        todos.addAll(areaRecuperacion.getAldeanosEnRecuperacion());

        if (todos.isEmpty()) return "Ninguno";

        return String.join(", ", todos);
    }

    public String getGuerreros() {
        synchronized (guerreros) {
            return guerreros.stream().
                    map(Guerrero::getIdGuerrero).
                    reduce((a, b) -> a + ", " + b).
                    orElse("Ninguno");
        }
    }
    public String getAldeanosMina() {
        synchronized (mina) {
            return mina.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosMina() {
        synchronized (mina) {
            return mina.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosMina() {
        synchronized (mina) {
            return mina.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Ninguno");
        }
    }

    public String getAldeanosBosque() {
        synchronized (bosque) {
            return bosque.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosBosque() {
        synchronized (bosque) {
            return bosque.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosBosque() {
        synchronized (bosque) {
            return bosque.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Ninguno");
        }
    }

    public String getAldeanosGranja() {
        synchronized (granja) {
            return granja.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosGranja() {
        synchronized (granja) {
            return granja.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosGranja() {
        synchronized (granja) {
            return granja.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Ninguno");
        }
    }

    public String getAldeanosTesoreria() {
        synchronized (tesoreria) {
            return tesoreria.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosTesoreria() {
        synchronized (tesoreria) {
            return tesoreria.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosTesoreria() {
        synchronized (tesoreria) {
            return tesoreria.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getAldeanosAserradero() {
        synchronized (aserradero) {
            return aserradero.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosAserradero() {
        synchronized (aserradero) {
            return aserradero.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosAserradero() {
        synchronized (aserradero) {
            return aserradero.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getAldeanosGranero() {
        synchronized (granero) {
            return granero.getAldeanos().stream()
                    .map(Aldeano::getIdAldeano)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getGuerrerosGranero() {
        synchronized (granero) {
            return granero.getGuerreros().stream()
                    .map(Guerrero::getIdGuerrero)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }

    public String getBarbarosGranero() {
        synchronized (granero) {
            return granero.getBarbaros().stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
        }
    }
    public ZonaCampamentoBarbaros getZonaCampamento() {
        return zonaCampamento;
    }

    // Funciones para ataque

    public Zona obtenerZonaAleatoria() {
        List<Zona> zonas = List.of(granja, bosque, mina, granero, aserradero, tesoreria);
        return zonas.get(new Random().nextInt(zonas.size()));
    }
    public Zona obtenerZonaAleatoriaParaPatrulla() {
        return obtenerZonaAleatoria();
    }

    // Pausa
    public void setPausa(boolean enPausa) {
        pausado.set(enPausa);
        if (!enPausa) {
            synchronized (pausaLock) {
                pausaLock.notifyAll();
            }
        }
    }
    public void esperarSiPausado() throws InterruptedException {
        synchronized (pausaLock) {
            while (pausado.get()) {
                pausaLock.wait();
            }
        }
    }
    public boolean isPausado() {
        return pausado.get();
    }

    public String getGuerrerosEnCuartel() {
        return cuartel.obtenerGuerrerosEntrenando();
    }
    // Emergencia
    public boolean isEmergenciaActiva() {
        return emergenciaActiva.get();
    }

    public void activarEmergencia() {
        boolean nuevoEstado = !emergenciaActiva.get();
        emergenciaActiva.set(nuevoEstado);

        if (nuevoEstado) {
            Log.log("¡Emergencia activada! Los aldeanos regresan a CASA PRINCIPAL.");

            for(Aldeano a : aldeanos){
                a.setEmergencia(true);
                synchronized (a) {
                    a.notify();
                }
            }
        } else {
            Log.log("¡Emergencia desactivada! Los aldeanos retoman su trabajo.");

            for(Aldeano a : aldeanos){
                a.setEmergencia(false);
                synchronized (a) {
                    a.notify();
                }
            }
        }
    }

    // Clases
    public class CasaPrincipal {
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

    public class PlazaCentral {
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

    public class Cuartel {
        private final List<Guerrero> enEntrenamiento = Collections.synchronizedList(new ArrayList<>());

        public void entrar(Guerrero g) {
            synchronized (enEntrenamiento) {
                if (!enEntrenamiento.contains(g)) {
                    enEntrenamiento.add(g);
                    Log.log("El guerrero " + g.getIdGuerrero() + " entra al Cuartel.");
                }
            }
        }

        public void entrenar(Guerrero g) throws InterruptedException {
            Log.log("El guerrero " + g.getIdGuerrero() + " se entrena en el Cuartel.");
            Thread.sleep(5000 + (int) (Math.random() * 3000)); // Entre 5 y 8 segundos
            Log.log("El guerrero " + g.getIdGuerrero() + " ha finalizado su entrenamiento.");
            salir(g); // salir al finalizar
        }

        public void salir(Guerrero g) {
            synchronized (enEntrenamiento) {
                enEntrenamiento.remove(g);
                Log.log("El guerrero " + g.getIdGuerrero() + " ha salido del Cuartel.");
            }
        }

        public String obtenerGuerrerosEntrenando() {
            synchronized (enEntrenamiento) {
                return enEntrenamiento.stream()
                        .map(Guerrero::getIdGuerrero)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Ninguno");
            }
        }
    }
}