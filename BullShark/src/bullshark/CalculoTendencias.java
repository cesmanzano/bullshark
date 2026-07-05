package bullshark;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import bullshark.beans.ResultadoTendencias;

public class CalculoTendencias {

	/**
	 * Esta clase:
	 * 	- recibe:   Valor / Fecha de Evaluacion / Cotizacion de Evaluacion
	 *  - extrae:   datos del fichero de rectas mediante una SQL y calcula si la cotización 
	 *    		    recibida está por encima o por debajo de la extrapolada. 
	 *  - devuelve: alza o baja / plazo en el que está rompiendo la tendencia (opcional) 
	 */

	static final String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
	static final String USER = "bolsa";
	static final String PASS = "bolsa";
	static Scanner sc = new Scanner(System.in);
		
	public static void calculo (String valor,             int fechaEvaluacion,    int cotizacionEvaluacion,
							String alzaBajaDiario, String alzaBajaSemanal, String alzaBajaMensual			
							) throws SQLException, IOException {

		int fechaInicioCotizacion = 0;  
		int fechaFinCotizacion = 0;
		int cotizacionInicio = 0;
		int cotizacionFin = 0;
		String plazo ="";
		
		ResultadoTendencias resultadoTendencias = new ResultadoTendencias();
		
		try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
		
	//Recupero rectas asociadas al valor recibido:
			String sqlRectasCotizacion = "SELECT * FROM rectas_cotizacion where valor = '" + valor + "'";
//			String sqlRectasCotizacion = "SELECT * FROM rectas_cotizacion where valor = 'Amazon'";
//			String sqlRectasCotizacion = "SELECT * FROM rectas_cotizacion";
//		String sqlRectasCotizacion = "SELECT fecha_inicio, fecha_fin, cotizacion_inicio, cotizacion_fin, plazo FROM rectasCotizacion where valor = 'Amazon'";
		
	//Vamos leyendo registros con el rsMaximos.next y con un select, trincamos lo que necesitemos:
		ResultSet rsDatosRecta = con.createStatement().executeQuery(sqlRectasCotizacion);
		 
		try (rsDatosRecta) {

	        while (rsDatosRecta.next()) {
	        	
	        	plazo = rsDatosRecta.getString("plazo"); 
	        	
        	//Tres tipos de plazo: D / S / M;
	        	switch (plazo) {
	        	case "D":
	        		int fechaInicioCotizacionDia = rsDatosRecta.getInt("fecha_inicio");  
	        		int fechaFinCotizacionDia    = rsDatosRecta.getInt("fecha_fin");  
	        		int cotizacionInicioDia      = rsDatosRecta.getInt("cotizacion_inicio");  
	        		int cotizacionFinDia         = rsDatosRecta.getInt("cotizacion_fin"); 
	        		
	        		fechaInicioCotizacion = fechaInicioCotizacionDia;  
	    			fechaFinCotizacion    = fechaFinCotizacionDia;  
	    			cotizacionInicio      = cotizacionInicioDia;  
	    			cotizacionFin         = cotizacionFinDia;
	        		
	    			if (evaluarCotizacion(fechaInicioCotizacion, fechaFinCotizacion, cotizacionInicio, cotizacionFin, fechaEvaluacion, cotizacionEvaluacion) == 1) {
	    				resultadoTendencias.alzaBajaDiario = "Alza";
	    			}else {
	    				resultadoTendencias.alzaBajaDiario = "Baja";	    				
	    			}
	    			alzaBajaDiario = resultadoTendencias.alzaBajaDiario;
	    			continue;
//	        		break;
	        		
	        	case "S":
	        		int fechaInicioCotizacionSemana = rsDatosRecta.getInt("fecha_inicio");  
	        		int fechaFinCotizacionSemana    = rsDatosRecta.getInt("fecha_fin");  
	        		int cotizacionInicioSemana      = rsDatosRecta.getInt("cotizacion_inicio");  
	        		int cotizacionFinSemana         = rsDatosRecta.getInt("cotizacion_fin"); 
	        		
	        		fechaInicioCotizacion = fechaInicioCotizacionSemana;  
	    			fechaFinCotizacion    = fechaFinCotizacionSemana;  
	    			cotizacionInicio      = cotizacionInicioSemana;  
	    			cotizacionFin         = cotizacionFinSemana;
	    			
	    			if (evaluarCotizacion(fechaInicioCotizacion, fechaFinCotizacion, cotizacionInicio, cotizacionFin, fechaEvaluacion, cotizacionEvaluacion) == 1) {
	    				resultadoTendencias.alzaBajaSemanal = "Alza";
	    			}else {
	    				resultadoTendencias.alzaBajaSemanal = "Baja";	    				
	    			}
	    			alzaBajaSemanal = resultadoTendencias.alzaBajaSemanal;
	    			continue;
//	        		break;
	        		
	        	case "M":
	        		int fechaInicioCotizacionMes = rsDatosRecta.getInt("fecha_inicio");  
	        		int fechaFinCotizacionMes    = rsDatosRecta.getInt("fecha_fin");  
	        		int cotizacionInicioMes      = rsDatosRecta.getInt("cotizacion_inicio");  
	        		int cotizacionFinMes         = rsDatosRecta.getInt("cotizacion_fin"); 
	        		
	        		fechaInicioCotizacion = fechaInicioCotizacionMes;  
	    			fechaFinCotizacion    = fechaFinCotizacionMes;  
	    			cotizacionInicio      = cotizacionInicioMes;  
	    			cotizacionFin         = cotizacionFinMes;
	    			
	    			if (evaluarCotizacion(fechaInicioCotizacion, fechaFinCotizacion, cotizacionInicio, cotizacionFin, fechaEvaluacion, cotizacionEvaluacion) == 1) {
	    				resultadoTendencias.alzaBajaMensual = "Alza";
	    			}else {
	    				resultadoTendencias.alzaBajaMensual = "Baja";	    				
	    			}
	    			alzaBajaMensual = resultadoTendencias.alzaBajaMensual;
	    			continue;
//	        		break;
	        }
			
	        }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	} catch (SQLException e) {
	    e.printStackTrace();
	}
		System.out.println("fechaInicioCotizacion: " + fechaInicioCotizacion);
		System.out.println("fechaFinCotizacion: "    + fechaFinCotizacion);
		System.out.println("cotizacionInicio: "      + cotizacionInicio);
		System.out.println("cotizacionFin: "         + cotizacionFin);
		System.out.println("alzaBajaDiario: "        + alzaBajaDiario);
		System.out.println("alzaBajaSemanal: "       + alzaBajaSemanal);
		System.out.println("alzaBajaMensual: "       + alzaBajaMensual);
}
		
	//*******************	
	// EVALUAR COTIZACION
	//*******************
	public static int evaluarCotizacion(int fechaInicioCotizacion, int fechaFinCotizacion, 
										int cotizacionInicio, int cotizacionFin, 
										int fechaEvaluacion, int cotizacionEvaluacion){
		
	//Calculamos recta de tendencia.
		// x = fechas
		// y = cotizaciones
		// y=(y2-y1)/(x2-x1)*(x-x1)+y1

		int pendiente = 0;
		int cotizacionExtrapolada = 0;
	
		if((cotizacionFin - cotizacionInicio) !=0) {
			pendiente  = (fechaFinCotizacion - fechaInicioCotizacion) / (cotizacionFin - cotizacionInicio);      
		}
		cotizacionExtrapolada = pendiente * (fechaEvaluacion - fechaInicioCotizacion) + cotizacionInicio;
	
	//Comparamos la cotización recibida con la calculada mediante la recta de tendencia.
		if(cotizacionEvaluacion >= cotizacionExtrapolada) {
			return 1; //Al alza.
		}else {
			return -1;//A la baja.
		}
	}
	
}
