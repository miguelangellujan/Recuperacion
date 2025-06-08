package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class Log {
    private static PrintWriter writer;

    static {
        try {
            writer = new PrintWriter(new FileWriter("centro_urbano.tx", true));
        } catch (IOException e) {
            System.out.println("No se pudo abrir el archivo de log.");
        }
        Runtime.getRuntime().addShutdownHook(new CerrarLogger());
    }

    // para añadir monitor método synchronized para proteger acceso
    public static synchronized void log (String mensaje) {
        String texto = String.format("[%s] %s", new Date(), mensaje);
        System.out.println(texto);
        if (writer != null) {
            writer.println(texto);
            writer.flush();
        }
    }
    private static class CerrarLogger extends Thread {
        public void run() {
            if (writer != null) {
                writer.close();
            }
        }
    }
}