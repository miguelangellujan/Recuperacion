package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Aldeano extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private boolean activo = true;
    private boolean esperandoEnEmergencia = false;

    public Aldeano(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdAldeano() {
        return id;
    }

    public void setEmergencia(boolean estado) {
        esperandoEnEmergencia = estado;
    }

    public boolean isEsperandoEnEmergencia() {
        return esperandoEnEmergencia;
    }

    public void continuarTrasEmergencia() {
        synchronized (this) {
            notify();
        }
    }

    public CentroUrbano getCentro() {
        return centro;
    }

    @Override
    public void run() {
        try {
            while (activo) {
                // === COMPROBAR EMERGENCIA ===
                if (centro.isEmergenciaActiva()) {
                    Log.log(id + " regresa por emergencia a CASA PRINCIPAL");
                    centro.getCasaPrincipal().registrarEntrada(id);
                    esperandoEnEmergencia = true;
                    synchronized (this) {
                        wait();
                    }
                    esperandoEnEmergencia = false;
                    centro.getCasaPrincipal().salir(id);
                    Log.log(id + " reanuda ciclo desde PLAZA CENTRAL");
                    centro.getPlazaCentral().planificar(id);
                    Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                    centro.getPlazaCentral().salir(id);
                }

                // === PASO 1: CASA PRINCIPAL ===
                Log.log(id + " entra en CASA PRINCIPAL");
                centro.getCasaPrincipal().registrarEntrada(id);
                Thread.sleep(FuncionesComunes.randomBetween(2000, 4000));
                centro.getCasaPrincipal().salir(id);

                // === PASO 2: PLAZA CENTRAL ===
                Log.log(id + " va a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

                // === PASO 3: Selección de recurso ===
                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                // === PASO 4: Entrar en área ===
                Log.log(id + " intenta entrar en " + tipo);
                area.entrar(this);

                // === PASO 5: Recolectar ===
                int cantidad = FuncionesComunes.randomBetween(10, 20);
                Thread.sleep(FuncionesComunes.randomBetween(5000, 10000));

                // === PASO 6: Comprobar ataque ===
                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + tipo);
                    area.salir(this);
                    centro.getAreaRecuperacion().enviarAldeano(this, 12000, 15000);
                    continue;
                }

                Log.log(id + " recolecta " + cantidad + " unidades de " + tipo);
                area.salir(this);

                //Ir a Plaza Central antes de depositar
                Log.log(id + " va a la PLAZA CENTRAL antes de depositar");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);

// === PASO 8: Depositar en almacén ===
                almacen.depositar(this, cantidad);


                // === PASO 8: Notificar ===
                synchronized (area) {
                    area.notifyAll();
                }

                // === PASO 9: Volver a Plaza Central ===
                Log.log(id + " vuelve a la PLAZA CENTRAL");
                centro.getPlazaCentral().planificar(id);
                Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                centro.getPlazaCentral().salir(id);
            }

        } catch (InterruptedException e) {
            Log.log(id + " ha sido interrumpido.");
        }
    }
}
