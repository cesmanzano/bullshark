package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bullshark.beans.Cotizacion;
import bullshark.beans.EmaPosicion;


public class CalculoMediasMovilesExponenciales {
	private static Connection connection=null;
	private static String valorExclusivo="";
	private static List<Integer> periodos = new ArrayList<Integer>();
	
    public static void main(String[] args) {
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
         calculoMediasMovilesExponenciales(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoMediasMovilesExponenciales(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoMediasMovilesExponenciales() ;
    }

    public static boolean calculoMediasMovilesExponenciales(Connection connection) {

    	CalculoMediasMovilesExponenciales.connection = connection;
    	return calculoMediasMovilesExponenciales() ;
    }

    private static boolean calculoMediasMovilesExponenciales() {
                          
                     
        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    

/*

Valor – Debe existir en nuestro fichero de valores 

Plazo –  D=Diario/S=Semanal/M=Mensual 

Nivel – 1/2/3/4/5 - Indica el periodo del cálculo. (Más adelante tendremos un fichero de configuración, en él podremos ir cambiando estos valores, 
para cada nivel tendremos un número de sesiones a considerar, ahora Nivel: 1=20, 2=50, 3=100 y 4=200, si hay que hacer algún ajuste lo haremos en programa) 

Fecha/AñoSemana/AñoMes  – Fecha de la cotización 

Resultado del cálculo 

Posición.  (+3, +2, +1, 0, -1, -2, -3) El cero no se dará nunca. Cuando el valor de la Media Móvil esté por encima de la cotización valdrá +1, 
cuando esté por debajo valdrá -1 (Más adelante veremos como mejorar este dato, de momento nos vale con saber si está por encima o por debajo de la cotización ) 

 
 */
        

	/*
	* Leer maestro de valores y dependiendo del plazo:
	* 
	* si es D diario  --> leerá de cotiza_dia
	* si es S semanal --> leerá de cotiza_semana
	* si es M mensual --> leerá de cotiza_mes
	* 
	* leerá dependiendo del nivel
	* 
	* si nivel es 1-> 20 registros
	* si nivel es 2-> 50 registros
	* si nivel es 3-> 100 registros
	* si nivel es 4-> 200 registros
	* 
	* se suman los cierres de esos registros leídos y se divide por nivel
	* 
	*/
        
        String sql_select_configuracion = "SELECT dato FROM configuracion WHERE tipo='EMA' ORDER BY tipo, nivel";
        PreparedStatement ps_configuracion;
       
		try {
			ps_configuracion = connection.prepareStatement(sql_select_configuracion);
			ResultSet rs_configuracion = ps_configuracion.executeQuery();
		    while (rs_configuracion.next()) {
		    	periodos.add(rs_configuracion.getInt("dato"));
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}

        
        bucle("D");
        
        bucle("S");
        
        bucle("M");
		
		System.out.println("✔ Fin del proceso");
		return true;
    }
    
    private static void bucle(String plazo) {
    
		    	
	    	String valor=null;
		
			/*
			 * maestro_valores        	 
			 */
				
	        String sql_select_maestro_valores = "SELECT valor FROM maestro_valores ORDER BY valor";
	        PreparedStatement ps_maestro_valores;
			try {
				ps_maestro_valores = connection.prepareStatement(sql_select_maestro_valores);
				ResultSet rs_valores = ps_maestro_valores.executeQuery();
			    while (rs_valores.next()) {
			    	
			    	valor = rs_valores.getString("valor");
			    	System.out.println(valor+" "+plazo+" ");
			    	
			    	if(valorExclusivo.equals("") || (!valorExclusivo.equals("") && valorExclusivo.equals(valor))) {
			    	
			    		CalculoMediasMovilesExponenciales.calcula(valor, plazo);
			    		
			    	}
			    }//end while valores
			} catch (SQLException e) {
				e.printStackTrace();
			}
						
    }
    
    
    
    
    
    /*
     * valor=BBVA, Santander, Apple, etc
     * plazo=D:diario, S:semanal, M:mensual
     * nivel=número de registros
     */
    private static void calcula(String valor, String plazo) {
    	  	
    	
	   	//delete primero 
    	String sql_delete_medias_moviles = "DELETE FROM medias_moviles_exponenciales WHERE valor = ? AND plazo = ?";
    	PreparedStatement ps__delete_medias_moviles;
		try {
			ps__delete_medias_moviles = connection.prepareStatement(sql_delete_medias_moviles);
	    	ps__delete_medias_moviles.setString(1, valor);
	    	ps__delete_medias_moviles.setString(2, plazo);
	    	ps__delete_medias_moviles.executeUpdate();
	    	connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    	
    	
    	
    	
    	
    	//plazo="S";
    	
    	String sql="";
    	  switch (plazo) {
          case "D":
        	//Diario
        	  sql = "SELECT valor, fecha, cerrar FROM cotiza_dia WHERE valor = ? ORDER BY valor, fecha";
              break;
          case "S":
        	//Semanal
        	  sql = "SELECT valor, fecha, cerrar FROM cotiza_semana WHERE valor = ? ORDER BY valor, fecha";
              break;
          case "M":
        	//Mensual
        	  sql = "SELECT valor, fecha, cerrar FROM cotiza_mes WHERE valor = ? ORDER BY valor, fecha";
              break;
    	  }
    	  
    	  PreparedStatement ps_cotiza;
    	  try {
    		  ps_cotiza = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    		  ps_cotiza.setString(1, valor);
    		  
			 ResultSet rs = ps_cotiza.executeQuery();
			 
			 
			 List<Cotizacion> listaCotizaciones = new ArrayList<>();

		        while (rs.next()) {
		            listaCotizaciones.add(new Cotizacion(
		                rs.getString("fecha"),
		                rs.getDouble("cerrar")
		            ));
		        }
			 
		        System.out.println("Tamaño de lista de cotizaciones de " + valor +" plazo " + plazo +" : "  + listaCotizaciones.size());
if(listaCotizaciones.size()>0) {
		        Map<Integer, List<EmaPosicion>> todas = calcularEMAs(listaCotizaciones, periodos);

		        insertarEMAsEnBD(valor, listaCotizaciones, todas, plazo);
}
			 
  		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
	private static Map<Integer, List<EmaPosicion>> calcularEMAs(List<Cotizacion> listaCotizaciones, List<Integer> periodos) {

	    Map<Integer, List<EmaPosicion>> resultado = new HashMap<>();
	    Map<Integer, Double> emaAnterior = new HashMap<>();
	    Map<Integer, Double> alpha = new HashMap<>();

	    // Inicializar estructuras
	    for (int p : periodos) {
	        resultado.put(p, new ArrayList<EmaPosicion>());	        
	        emaAnterior.put(p, listaCotizaciones.get(0).cierre); // Semilla
	        alpha.put(p, 2.0 / (p + 1));
	        EmaPosicion emaPosicion=new EmaPosicion(listaCotizaciones.get(0).cierre, 0);
	        resultado.get(p).add(emaPosicion);
	    }

	    // Calcular todas las EMAs en un solo recorrido
	    for (int i = 1; i < listaCotizaciones.size(); i++) {
	        double precio = listaCotizaciones.get(i).cierre;

	        for (int p : periodos) {
	            double prev = emaAnterior.get(p);
	            double a = alpha.get(p);

	            double ema = (precio - prev) * a + prev;

	            emaAnterior.put(p, ema);
	            
	            //calcular posicion 
	            int posicion=-1;
	            if(ema>precio) {
	            	posicion=1;
	            }
	            EmaPosicion emaPosicion=new EmaPosicion(ema, posicion);
	            resultado.get(p).add(emaPosicion);
	        }
	    }
	    return resultado;
	}

	private static void insertarEMAsEnBD(String valor, List<Cotizacion> datos, Map<Integer, List<EmaPosicion>> todas, String plazo) throws SQLException {

	    //Map<Integer, List<Double>> todas = calcularEMAs(datos, periodos);


		
	    String sql = "INSERT INTO medias_moviles_exponenciales " +
	            "(valor, plazo, nivel, fecha, cotizacion, ema, posicion, señal, acierto, margen) " +
	            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    PreparedStatement ps = connection.prepareStatement(sql);

	    for (int i = 0; i < datos.size(); i++) {
	        Cotizacion c = datos.get(i);

	        for (int nivel : periodos) {

	            //double ema = todas.get(nivel).get(i);
	            EmaPosicion emaPosicion=todas.get(nivel).get(i);
	            

	            ps.setString(1, valor);          				// valor
	            ps.setString(2, plazo);        					// plazo
	            ps.setDouble(3, nivel);       	 				// nivel 
	            ps.setString(4, c.fecha);        				// fecha
	            ps.setDouble(5, c.cierre);          			// cierre
	            ps.setDouble(6, emaPosicion.getEma());          // ema

	            // Por ahora valores nulos
	            ps.setInt(7, emaPosicion.getPosicion());  		// posición
	            ps.setString(8, null);           				// señal
	            ps.setObject(9, null);           				// acierto
	            ps.setObject(10, null);           				// margen

	            ps.addBatch();
	        }
	    }

	    ps.executeBatch();
	    connection.commit();
	}

}