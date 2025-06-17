package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Aldeano extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private volatile boolean activo = true;
    private boolean emergencia = false;
    private boolean esperandoEnEmergencia = false;

    public Aldeano(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdAldeano() {
        return id;
    }

    private void checkYEsperarEmergencia() throws InterruptedException {
        if (centro.isEmergenciaActiva()) {
            throw new InterruptedException("Emergencia activa, interrumpir para ir a Casa Principal");
        }
    }

    public void setEmergencia(boolean estado) {
        emergencia = estado;
        if (estado) this.interrupt();
    }

    private void esperarFinEmergencia() throws InterruptedException {
        Thread.interrupted();

        moverACasaPrincipal();

        synchronized (this) {
            while (centro.isEmergenciaActiva()) {
                wait(500);
            }
        }
        moverAPlazaCentral();
    }

    public void moverACasaPrincipal() {
        synchronized (this) {
            if (!esperandoEnEmergencia) {
                esperandoEnEmergencia = true;
                centro.getArea("COMIDA").salir(this);
                centro.getArea("MADERA").salir(this);
                centro.getArea("ORO").salir(this);
                centro.getPlazaCentral().salir(id);
                centro.getAlmacen("COMIDA").salir(this);
                centro.getAlmacen("MADERA").salir(this);
                centro.getAlmacen("ORO").salir(this);
                centro.getCasaPrincipal().registrarEntrada(id);
                Log.log(id + " se ha movido a la Casa Principal por emergencia.");
            }
        }
    }

    public void moverAPlazaCentral() {
        synchronized (this) {
            if (esperandoEnEmergencia && centro.getCasaPrincipal().estaRegistrado(id)) {
                esperandoEnEmergencia = false;
                centro.getCasaPrincipal().salir(id);

                if (!centro.getPlazaCentral().estaRegistrado(id)) {
                    centro.getPlazaCentral().planificar(id);
                }
                Log.log(id + " ha salido de la Casa Principal y retoma el ciclo en la Plaza Central.");
            }
        }
    }

    @Override
    public void run() {
        while (activo) {
            try {
                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Va a la PLAZA CENTRAL
                Log.log(id + " va a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                centro.esperarSiPausado();
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.esperarSiPausado();
                centro.getPlazaCentral().salir(id);

                // Selecciona recurso y obtiene referencias
                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Entra en el área de recursos
                Log.log(id + " intenta entrar en " + centro.getArea(tipo));
                area.entrar(this);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Calcula cantidad recolectada
                int cantidad = FuncionesComunes.randomBetween(10, 20);
                int nivelHerramientas = Math.min(centro.getGestorMejoras().getNivelHerramientas(), 3);
                int recolectar = cantidad + (5 * nivelHerramientas);

                // Recolecta
                centro.esperarSiPausado();
                checkYEsperarEmergencia();
                Thread.sleep(FuncionesComunes.randomBetween(5000, 10000));

                // Verifica si fue atacado
                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + centro.getArea(tipo));
                    area.salir(this);
                    centro.esperarSiPausado();
                    centro.getAreaRecuperacion().enviarAldeano(this, 12000, 15000);
                    continue;
                }
                centro.esperarSiPausado();
                Log.log(id + " recolecta " + recolectar + " unidades de " + tipo);
                centro.esperarSiPausado();

                // Sale del área
                area.salir(this);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Va a la PLAZA CENTRAL antes de depositar
                Log.log(id + " va a la PLAZA CENTRAL antes de depositar");
                centro.getPlazaCentral().planificar(id);
                centro.esperarSiPausado();
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.esperarSiPausado();
                centro.getPlazaCentral().salir(id);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();
                centro.esperarSiPausado();
                // Deposita en el almacén correspondiente
                almacen.depositar(this, recolectar);
                checkYEsperarEmergencia();

                synchronized (area) {
                    area.notifyAll(); // Para desbloquear otros aldeanos si es necesario
                }

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Vuelve a la PLAZA CENTRAL al terminar el ciclo
                Log.log(id + " vuelve a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                checkYEsperarEmergencia();
                centro.esperarSiPausado();
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.esperarSiPausado();
                checkYEsperarEmergencia();
                centro.getPlazaCentral().salir(id);

            } catch (InterruptedException e) {
                if (activo && centro.isEmergenciaActiva()) {
                    try {
                        esperarFinEmergencia();
                    } catch (InterruptedException ie) {
                        activo = false;
                    }
                } else {
                    Log.log(id + " ha sido interrumpido y termina.");
                    activo = false;
                }
                Log.log(id + " fue interrumpido inesperadamente, pero sigue activo.");
            }
        }
    }}