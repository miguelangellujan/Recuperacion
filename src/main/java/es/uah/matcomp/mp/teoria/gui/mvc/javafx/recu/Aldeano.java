package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Aldeano extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private boolean activo = true;
    private boolean esperandoEnEmergencia = false;
    private FuncionesComunes funcionesComunes;

    public Aldeano(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdAldeano() {
        return id;
    }

    public void setEmergencia(boolean estado) {
        // Puedes optar por actualizar una variable propia, por ejemplo:
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
                // Comprobar si hay emergencia
                if (centro.isEmergenciaActiva()) {
                    Log.log(id + " regresa por emergencia a CASA PRINCIPAL");
                    esperandoEnEmergencia = true;
                    synchronized (this) {
                        wait(); // espera hasta que pase la emergencia
                    }
                    Log.log(id + " reanuda ciclo desde PLAZA CENTRAL");
                    esperandoEnEmergencia = false;
                }

                // Paso 1: Casa Principal
                Log.log(id + " entra en CASA PRINCIPAL");
                Thread.sleep(funcionesComunes.randomBetween(2000, 4000));

                // Paso 2: Plaza Central
                Log.log(id + " va a la PLAZA CENTRAL");
                Thread.sleep(funcionesComunes.randomBetween(1000, 2000));

                // Paso 3: Selección aleatoria de recurso
                String tipo = centro.seleccionarRecursoAleatorio();
                AreaRecurso area = centro.getArea(tipo);
                Almacen almacen = centro.getAlmacen(tipo);

                // Paso 4: Intentar entrar al área (esperando si está llena o atacada)
                Log.log(id + " intenta entrar en " + tipo);
                area.entrar(this); // bloquea hasta que pueda entrar

                // Paso 5: Recolectar recurso
                int cantidad = funcionesComunes.randomBetween(10, 20);
                Thread.sleep(funcionesComunes.randomBetween(5000, 10000));

                // Paso 6: Verificar si fue atacado
                if (area.fueAtacadoDurante(this)) {
                    Log.log(id + " fue atacado mientras recolectaba en " + tipo);
                    area.salir(this);
                    centro.getAreaRecuperacion().enviarAldeano(this, 12000, 15000);
                    continue; // volver al ciclo desde el principio
                }

                // Paso 7: Depositar recursos en el almacén
                Log.log(id + " recolecta " + cantidad + " unidades de " + tipo);
                area.salir(this);
                almacen.depositar(tipo, cantidad);

                // Paso 8: Notificar a aldeanos que puedan estar esperando
                synchronized (area) {
                    area.notifyAll();
                }

                // Paso 9: Regresa a la plaza central (implícito al repetir el ciclo)
                Log.log(id + " vuelve a la PLAZA CENTRAL");
            }

        } catch (InterruptedException e) {
            Log.log(id + " ha sido interrumpido.");
        }
    }
}