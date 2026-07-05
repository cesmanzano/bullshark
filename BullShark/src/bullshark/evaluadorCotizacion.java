package bullshark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class evaluadorCotizacion {

    /**
     * Evalúa si la cotización dada está por encima (1) o por debajo (-1) de la cotización extrapolada según la recta de tendencia definida por
     * dos mínimos leídos del fichero.
     *
     * x = fechas (AAAAMMDD como int)
     * y = cotizaciones (int)
     *
     * Ecuación: y = m * (x - x1) + y1, donde m = (y2 - y1) / (x2 - x1)
     *
     * @param fecha  Fecha de evaluación (AAAAMMDD)
     * @param cotizacion  Cotización a evaluar
     * @param maximosMinimos Ruta del fichero con las dos mínimas
     * @return 1 si al alza (>= extrapolada), -1 si a la baja
     */

	
	
//	HAY QUE VER CÓMO ACCEDER AL VALOR DEL FICHERO DE MÁXIMOS Y MÍNIMOS PERO POR VALOR. 
//	Y SI NO, POR UNA SIMPLE COMPARACIÓN HASTA ENCONTRAR EL QUE BUSCAMOS
	
	
	
    public static int evaluarCotizacion(String valor, String fecha, double cotizacion, String maximosMinimos) {

        int primeraMinimaCotizacion = 0;
        int segundaMinimaCotizacion = 0;
        int primeraFechaMinimaCotizacion = 0;
        int segundaFechaMinimaCotizacion = 0;
        String ficheroLeer= "";

    	String ficheroCotizacionesMaximas = "C:\\Fernancio\\ProyectoBolsa\\datosCotizacionesMaximas.txt";
    	String ficheroCotizacionesMinimas = "C:\\Fernancio\\ProyectoBolsa\\datosCotizacionesMinimas.txt";
        
        if(maximosMinimos == "maximos") {
        	ficheroLeer = ficheroCotizacionesMaximas;
        }
        
        if(maximosMinimos == "minimos") {
        	ficheroLeer = ficheroCotizacionesMinimas;
        }
        
        // Lectura del fichero (se toma la PRIMERA línea válida)
        try (BufferedReader br = new BufferedReader(new FileReader(ficheroLeer))) {
            String registro;
            while ((registro = br.readLine()) != null) {
                registro = registro.trim();
                if (registro.isEmpty()) continue;

                String[] campos = registro.split(";");
                if (campos.length < 4) continue;

                valor						 = String.valueOf(campos[0].trim());
                primeraMinimaCotizacion      = Integer.parseInt(campos[1].trim());
                primeraFechaMinimaCotizacion = Integer.parseInt(campos[2].trim());
                segundaMinimaCotizacion      = Integer.parseInt(campos[3].trim());
                segundaFechaMinimaCotizacion = Integer.parseInt(campos[4].trim());
                break; // usamos solo la primera línea válida
            }
        } catch (IOException | NumberFormatException e) {
            throw new IllegalArgumentException(
                "No se pudo leer el fichero de cotizaciones mínimas: " + e.getMessage(), e
            );
        }

      // Calcular recta de tendencia
        // x = fechas, y = cotizaciones
        // y = m * (x - x1) + y1,  m = (y2 - y1) / (x2 - x1)
        int dx = segundaFechaMinimaCotizacion - primeraFechaMinimaCotizacion;   // x2 - x1
        int dy = segundaMinimaCotizacion      - primeraMinimaCotizacion;        // y2 - y1

        double cotizacionExtrapolada;

        if (dx == 0) {
            // Fechas iguales -> pendiente infinita. Fallback: comparar con y1.
            cotizacionExtrapolada = primeraMinimaCotizacion;
        } else {
            double pendiente = (double) dy / (double) dx;
            cotizacionExtrapolada = pendiente * (Double.parseDouble(fecha) - (double) primeraFechaMinimaCotizacion)
                                      + (double) primeraMinimaCotizacion;
        }

      //Comparación (redondeamos la extrapolada a entero para ser coherentes con cotizacion int)d
        	return (cotizacion >= Math.round(cotizacionExtrapolada)) ? 1 : -1;
    }

    /**
     * Sobrecarga opcional manteniendo tu firma original (dos parámetros),
     * usando una ruta por defecto (ajústala a tu entorno).
     */
//    public static int evaluarCotizacion(int fecha, int cotizacion) {
//        String ficheroPorDefecto = "C:\\Fernancio\\ton\\datosCotizacionesMinimas.txt";
//        return evaluarCotizacion(fecha, cotizacion, ficheroPorDefecto);
//    }
   
   
//Para llamarla:
//public class Principal {
//    public static void main(String[] args) {
//        int fecha = 20260301;
//        int cotizacion = 8;
//        String ruta = "C:\\Fernancio\\ton\\datosCotizacionesMinimas.txt";
//
//        int resultado = EvaluadorCotizacion.evaluarCotizacion(fecha, cotizacion, ruta);
//        System.out.println(resultado == 1 ? "Al alza" : "A la baja");
//    }
//}
   
   }