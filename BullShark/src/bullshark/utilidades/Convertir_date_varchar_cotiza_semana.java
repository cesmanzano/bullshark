package bullshark.utilidades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Convertir_date_varchar_cotiza_semana {
	private static Connection connection=null;
	public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
        String USER = "bolsa"; 
        String PASSWORD = "bolsa";

    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
			 
			 
			 	String sql_select_cotiza_semana =	"SELECT valor, año, semana FROM cotiza_semana ORDER BY valor, año, semana";
		 		String sql_update = "UPDATE cotiza_semana SET fecha = ? WHERE valor =? and año=? and semana=?";
		    		
		            PreparedStatement ps_cotiza_semana;
		    		try {
		    			ps_cotiza_semana = connection.prepareStatement(sql_select_cotiza_semana);
		    			
		    			ResultSet rs = ps_cotiza_semana.executeQuery();
		    			
		    			String valor = null;
		    			int anno = 0;
		    			int semana = 0;
		    			String semanaString = null;
	    			    String fechaString= null;
	    			    
		    	        while (rs.next()) {
		    	        	
		    	        	valor = rs.getString("valor");
		    	        	
		    	        	anno = rs.getInt("año");
		    	        	
		    	        	semana = rs.getInt("semana");
		    	        	
		    	        	semanaString=String.valueOf(semana);
		    	        	
		    	        	 if(semanaString.length()<2) {
		    	        		 semanaString="0"+semanaString;
		   	        	  	}
		    	        	 fechaString=""+anno+semana+"00";
		  	        	  
							 PreparedStatement ps_sql_update = connection.prepareStatement(sql_update);
							 ps_sql_update.setString(1, fechaString);
							 ps_sql_update.setString(2, valor);
							 ps_sql_update.setInt(3, anno);
							 ps_sql_update.setInt(4, semana);
							 ps_sql_update.executeUpdate();
		   
		    	        }
		    		} catch (SQLException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
			 
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}

        
        
	}

}
