package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Aldeano extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private volatile boolean activo = true;
    private boolean emergencia = false;
    private boolean esperandoEnEmergencia = false;

    // Constructor
    public Aldeano(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdAldeano() {
        return id;
    }

    // Emergencia
    private void checkYEsperarEmergencia() throws InterruptedException {
        if (centro.isEmergenciaActiva()) {
            throw new InterruptedException("Emergencia activa, interrumpir para ir a Casa Principal");
        }
    }

    public void setEmergencia(boolean estado) {
        emergencia = estado;
        if (estado) {
            // Interrumpe cualquier espera o sleep activo para forzar la comprobación de emergencia.
            this.interrupt();
        }
    }

    public void moverACasaPrincipal() {
        synchronized (this) {
            if(!esperandoEnEmergencia){
                esperandoEnEmergencia = true;

                centro.getArea("COMIDA").salir(this);
                centro.getArea("MADERA").salir(this);
                centro.getArea("ORO").salir(this);
                centro.getPlazaCentral().salir(id);
                centro.getAlmacen("COMIDA").salir(this);
                centro.getAlmacen("MADERA").salir(this);
                centro.getAlmacen("ORO").salir(this);
            }

            centro.getCasaPrincipal().registrarEntrada(id);
            Log.log(id + " se ha movido a la Clase Principal por emergencia.");
        }
    }

    private void esperarFinEmergencia() throws InterruptedException {
        esperandoEnEmergencia = true;

        moverACasaPrincipal();

        // Espera hasta que la emergencia se desactive.
        synchronized (this) {
            while (centro.isEmergenciaActiva()) {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    Log.log("Error en la emergencia: " + e.getMessage());
                }
            }
        }

        moverAPlazaCentral();
    }

    public void moverAPlazaCentral(){
        synchronized (this) {
            if (esperandoEnEmergencia && centro.getCasaPrincipal().estaRegistrado(id)) {
                esperandoEnEmergencia = false;

                centro.getCasaPrincipal().salir(id);

                if(!centro.getPlazaCentral().estaRegistrado(id)){
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
                // Antes de cada acción, comprobamos si hay emergencia
                checkYEsperarEmergencia();

                Log.log(id + " entra en CASA PRINCIPAL");
                centro.getCasaPrincipal().registrarEntrada(id);
                Thread.sleep(FuncionesComunes.randomBetween(2000, 4000));
                centro.getCasaPrincipal().salir(id);

                checkYEsperarEmergencia();

                Log.log(id + " va a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                checkYEsperarEmergencia();

                Log.log(id + " intenta entrar en " + tipo);
                area.entrar(this);

                checkYEsperarEmergencia();

                int cantidad = FuncionesComunes.randomBetween(10, 20);

                int nivelHerramientas = centro.getGestorMejoras().getNivelHerramientas();
                nivelHerramientas = Math.min(nivelHerramientas, 3); // Máximo 3 niveles
                int recolectar = cantidad + (5 * nivelHerramientas);

                Thread.sleep(FuncionesComunes.randomBetween(5000, 10000));  // recolección

                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + tipo);
                    area.salir(this);
                    centro.getAreaRecuperacion().enviarAldeano(this, 12000, 15000);
                    continue;
                }

                Log.log(id + " recolecta " + recolectar + " unidades de " + tipo);
                area.salir(this);

                checkYEsperarEmergencia();

                Log.log(id + " va a la PLAZA CENTRAL antes de depositar");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

                checkYEsperarEmergencia();

                almacen.depositar(this, recolectar);

                synchronized (area) {
                    area.notifyAll();
                }

                checkYEsperarEmergencia();

                Log.log(id + " vuelve a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

            } catch (InterruptedException e) {
                // Si la interrupción es debido a la emergencia...
                if (activo && centro.isEmergenciaActiva()) {
                    try {
                        esperarFinEmergencia();
                    } catch (InterruptedException ie) {
                        // Si otra interrupción ocurre en esta fase, finaliza el hilo.
                        activo = false;
                    }
                    // Tras finalizar la emergencia, retomamos el ciclo (desde PLAZA CENTRAL en la próxima iteración)
                    continue;
                } else {
                    Log.log(id + " ha sido interrumpido y termina.");
                    activo = false;
                }
            }
        }
    }
}
