package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Aldeano extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private volatile boolean activo = true;
    private boolean emergencia = false;
    private volatile boolean esperandoEnEmergencia = false;

    public Aldeano(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdAldeano() {
        return id;
    }

    public boolean isEsperandoEnEmergencia() {
        return esperandoEnEmergencia;
    }

    public void setEmergencia(boolean estado) {
        emergencia = estado;
        if (estado) {
            // Interrumpe cualquier espera o sleep activo para forzar la comprobación de emergencia.
            this.interrupt();
        }
    }

    public void detener() {
        activo = false;
        this.interrupt(); // para salir de cualquier sleep o wait
    }

    public void continuarTrasEmergencia() {
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // Antes de cada acción, comprobamos si hay emergencia
                checkYEsperarEmergencia();

                // Empezamos el ciclo normal
                centro.getPaso().mirar();

                Log.log(id + " entra en CASA PRINCIPAL");
                centro.getCasaPrincipal().registrarEntrada(id);
                Thread.sleep(FuncionesComunes.randomBetween(2000, 4000));
                centro.getCasaPrincipal().salir(id);

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                Log.log(id + " va a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                Log.log(id + " intenta entrar en " + tipo);
                area.entrar(this);

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                int cantidad = FuncionesComunes.randomBetween(10, 20);
                Thread.sleep(FuncionesComunes.randomBetween(5000, 10000));  // recolección

                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + tipo);
                    area.salir(this);
                    centro.getAreaRecuperacion().enviarAldeano(this, 12000, 15000);
                    continue;
                }

                Log.log(id + " recolecta " + cantidad + " unidades de " + tipo);
                area.salir(this);

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                Log.log(id + " va a la PLAZA CENTRAL antes de depositar");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                almacen.depositar(this, cantidad);

                synchronized (area) {
                    area.notifyAll();
                }

                checkYEsperarEmergencia();

                centro.getPaso().mirar();
                Log.log(id + " vuelve a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

            } catch (InterruptedException e) {
                // Si la interrupción es debido a la emergencia...
                if (activo && centro.isEmergenciaActiva()) {
                    try {
                        irCasaPrincipalYEsperarFinEmergencia();
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

    /**
     * Método que comprueba si hay una emergencia activa y, de ser así,
     * lanza una excepción para desencadenar la rutina de emergencia.
     */
    private void checkYEsperarEmergencia() throws InterruptedException {
        if (centro.isEmergenciaActiva()) {
            throw new InterruptedException("Emergencia activa, interrumpir para ir a Casa Principal");
        }
    }

    /**
     * Simula el regreso del aldeano a la CASA PRINCIPAL con un retardo
     * aleatorio (entre 2 y 5 segundos) y espera de forma pasiva hasta que
     * se desactive la emergencia.
     */
    private void irCasaPrincipalYEsperarFinEmergencia() throws InterruptedException {
        esperandoEnEmergencia = true;

        // Simula el tiempo requerido para llegar a la CASA PRINCIPAL.
        int retardo = FuncionesComunes.randomBetween(2000, 5000);
        Thread.sleep(retardo);

        Log.log(id + " regresa por emergencia a CASA PRINCIPAL");
        centro.getCasaPrincipal().registrarEntrada(id);

        // Espera hasta que la emergencia se desactive.
        while (centro.isEmergenciaActiva()) {
            synchronized (this) {
                wait();
            }
        }

        centro.getCasaPrincipal().salir(id);
        esperandoEnEmergencia = false;
        Log.log(id + " emergencia desactivada, reanudando ciclo.");
    }
}
