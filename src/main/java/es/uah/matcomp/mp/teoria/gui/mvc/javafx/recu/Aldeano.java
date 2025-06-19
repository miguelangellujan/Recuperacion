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
                Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
                centro.getPlazaCentral().salir(id);

                // Selecciona recurso
                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Entra en el área de recursos
                Log.log(id + " intenta entrar en " + centro.getArea(tipo).toString(tipo));
                area.entrar(this);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Verifica si fue atacado
                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + tipo);
                    area.salir(this);
                    centro.getAreaRecuperacion().entrar(this); // ← bloquea correctamente y luego sigue
                    continue;
                }

                // Recolecta
                int cantidad = FuncionesComunes.Tiempoaleatorio(10, 20);
                int nivelHerramientas = Math.min(centro.getGestorMejoras().getNivelHerramientas(), 3);
                int recolectar = cantidad + (5 * nivelHerramientas);

                centro.esperarSiPausado();
                Thread.sleep(FuncionesComunes.Tiempoaleatorio(5000, 10000));
                centro.esperarSiPausado();
                Log.log(id + " recolecta " + recolectar + " unidades de " + tipo);
                area.salir(this);

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Va a la plaza antes de depositar
                Log.log(id + " va a la PLAZA CENTRAL antes de depositar");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
                centro.getPlazaCentral().salir(id);

                // Deposita
                almacen.depositar(this, recolectar);

                synchronized (area) {
                    area.notifyAll();
                }

                centro.esperarSiPausado();
                checkYEsperarEmergencia();

                // Vuelve a la PLAZA CENTRAL
                Log.log(id + " vuelve a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
                centro.getPlazaCentral().salir(id);

            } catch (InterruptedException e) {
                if (centro.isEmergenciaActiva()) {
                    Log.log(id + " fue interrumpido y se dirige a la CASA PRINCIPAL por emergencia.");
                    try {
                        esperarFinEmergencia(); // Espera bloqueante a que termine la emergencia
                    } catch (InterruptedException ex) {
                        // Si se interrumpe durante la espera, puede ser ignorado
                    }
                } else {
                    Log.log(id + " fue interrumpido y se dirige al ÁREA DE RECUPERACIÓN por combate.");
                    centro.getAreaRecuperacion().entrar(this);
                }
            }
        }
    }
}