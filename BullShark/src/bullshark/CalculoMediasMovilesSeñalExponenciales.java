package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CalculoMediasMovilesSeñalExponenciales {
	private static Connection connection=null;
	private static String valorExclusivo="";
	
    public static void main(String[] args) {
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
         calculoMediasMovilesSeñal(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoMediasMovilesSeñal(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoMediasMoviles() ;
    }

    public static boolean calculoMediasMoviles(Connection connection) {

    	CalculoMediasMovilesSeñalExponenciales.connection = connection;
    	return calculoMediasMoviles() ;
    }

    private static boolean calculoMediasMoviles() {
                          
                     
        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    

/*


 //Señal. Leer medias móviles del día anterior y ver si la posición es igual o no a esta que estamos calculando
		 // si la posición del día anterior es igual a la de hoy se pone 0
		 // si cambia de positivo a negativo es -1
		 // si cambia de negativo a positivo es 1
		 // si cambia se recoge la posición y si no cambia es 0
		
 
 */
        
        
    	/*
		 * maestro_valores        	 
		 */
			
	        String sql_select_maestro_valores = "SELECT valor FROM maestro_valores ORDER BY valor";
	        PreparedStatement ps_maestro_valores;
			try {
				ps_maestro_valores = connection.prepareStatement(sql_select_maestro_valores);
				ResultSet rs_valores = ps_maestro_valores.executeQuery();
				String valor=null;
				
		        String sql_select_medias = "SELECT valor, plazo, nivel, fecha, cotizacion, ema, posicion FROM medias_moviles_exponenciales "
		        		+ "WHERE valor = ?"
		        		+ "ORDER BY valor, plazo, nivel, fecha, ema, posicion";
	
		        String sql_update = "UPDATE medias_moviles_exponenciales SET señal = ? WHERE valor =? and plazo=? and nivel=? and fecha = ?";
		        
			    while (rs_valores.next()) {
			    	
			    	valor = rs_valores.getString("valor");
			    	if(valorExclusivo.equals("") || (!valorExclusivo.equals("") && valorExclusivo.equals(valor))) {			    	
				        PreparedStatement ps_medias;
				        try {
				        	ps_medias = connection.prepareStatement(sql_select_medias);
				        	ps_medias.setString(1, valor);
				    		  
							ResultSet rs_medias = ps_medias.executeQuery();
							int posicion=0;
							int posicionAnterior=0;
							int señal=0;
							String plazo="";
							String plazoAnterior="";
							int nivel=0;
							int nivelAnterior=0;
							String fecha="";
							 while (rs_medias.next()) {
								 posicion = rs_medias.getInt("posicion");
								 plazo = rs_medias.getString("plazo");
								 nivel = rs_medias.getInt("nivel");
								 fecha = rs_medias.getString("fecha");
							
								 System.out.println("valor: " + valor + " fecha: " + fecha + " plazo: " + plazo + " nivel: " + nivel + " posicion: " + posicion);
								 
								 //cambia de plazo y ponemos a 0 la señal
								 if(plazoAnterior.equals("") || !plazoAnterior.equals(plazo)) {
									 señal=0;
								 }else {
									 //cambia de nivel y ponemos a 0 la señal
									 if(nivelAnterior==0 || nivelAnterior!=nivel) {
										 señal=0;
									 }else {
										 if(posicion!=0) {
											 if(posicion!=posicionAnterior) {
												señal=posicion; 
											 }else {
												 señal=0;
											 }
										 }
									 }
								 }
								 System.out.println("posicionAnterior: "+posicionAnterior+" posicion: "+posicion+ " señal: "+señal);
								 
								 
								 PreparedStatement ps_sql_update = connection.prepareStatement(sql_update);
								 ps_sql_update.setInt(1, señal);
								 ps_sql_update.setString(2, valor);
								 ps_sql_update.setString(3, plazo);
								 ps_sql_update.setInt(4, nivel);
								 ps_sql_update.setString(5, fecha);
								 ps_sql_update.executeUpdate();
								 connection.commit();
								 
								 plazoAnterior=plazo; 
								 posicionAnterior=posicion;
								 nivelAnterior=nivel;	
							 }//end while medias
						} catch (SQLException e) {
							e.printStackTrace();
						}
	
			    	}//fin if valorExclusivo			    	
	 
			    }//end while valores
			} catch (SQLException e) {
				e.printStackTrace();
			}

		
		
		System.out.println("✔ Fin del proceso");
		return true;
    }
    
}