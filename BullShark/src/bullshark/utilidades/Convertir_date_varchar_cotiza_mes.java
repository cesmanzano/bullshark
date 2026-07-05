package bullshark.utilidades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Convertir_date_varchar_cotiza_mes {
	private static Connection connection=null;
	public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
        String USER = "bolsa"; 
        String PASSWORD = "bolsa";

    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
			 
			 
			 	String sql_select_cotiza_mes =	"SELECT valor, año, mes FROM cotiza_mes ORDER BY valor, año, mes";
		 		String sql_update = "UPDATE cotiza_mes SET fecha = ? WHERE valor =? and año=? and mes=?";
		    		
		            PreparedStatement ps_cotiza_mes;
		    		try {
		    			ps_cotiza_mes = connection.prepareStatement(sql_select_cotiza_mes);
		    			
		    			ResultSet rs = ps_cotiza_mes.executeQuery();
		    			
		    			String valor = null;
		    			int anno = 0;
		    			int mes = 0;
		    			
	    			    String fechaString= null;
	    			    String mesString= null; 
	    			    
		    	        while (rs.next()) {
		    	        	
		    	        	valor = rs.getString("valor");
		    	        	
		    	        	anno = rs.getInt("año");
		    	        	
		    	        	mes = rs.getInt("mes");
		    	        	
		    	        	mesString=String.valueOf(mes);
		    	        	
		    	        	if(mesString.length()<2) {
		    	        		 mesString="0"+mesString;
		   	        	  	}

		  	        	  fechaString=""+anno+mesString+"00";
		  	        	  
							 PreparedStatement ps_sql_update = connection.prepareStatement(sql_update);
							 ps_sql_update.setString(1, fechaString);
							 ps_sql_update.setString(2, valor);
							 ps_sql_update.setInt(3, anno);
							 ps_sql_update.setInt(4, mes);
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
