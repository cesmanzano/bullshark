package bullshark.utilidades;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Convertir_date_varchar_cotiza_dia {
	private static Connection connection=null;
	public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
        String USER = "bolsa"; 
        String PASSWORD = "bolsa";

    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
			 
			 
			 	String sql_select_cotiza_dia =	"SELECT valor, fecha FROM cotiza_dia ORDER BY valor, fecha";
		 		String sql_update = "UPDATE cotiza_dia SET fechavarchar = ? WHERE valor =? and fecha=?";
		    		
		            PreparedStatement ps_cotiza_dia;
		    		try {
		    			ps_cotiza_dia = connection.prepareStatement(sql_select_cotiza_dia);
		    			
		    			ResultSet rs = ps_cotiza_dia.executeQuery();
		    			
		    			String valor = null;
	    			    Date fechaSQL = null;
	    			    String fechaString= null;
	    			    
		    	        while (rs.next()) {
		    	        	
		    	        	valor = rs.getString("valor");
		    	        	
		    	        	fechaSQL = rs.getDate("fecha");
		    	            
		  	        	  LocalDate local = fechaSQL.toLocalDate();
		  	        	  int año = local.getYear();   
		  	        	  int mes = local.getMonthValue();
		  	        	  int dia = local.getDayOfMonth();
		  	        	  String mesString=String.valueOf(mes);
		  	        	  if(mesString.length()<2) {
		  	        		  mesString="0"+mesString;
		  	        	  }
		  	        	  String diaString=String.valueOf(dia);
		  	        	  if(diaString.length()<2) {
		  	        		  diaString="0"+diaString;
		  	        	  }
		  	        	  fechaString=""+año+mesString+diaString;
		  	        	  
							 PreparedStatement ps_sql_update = connection.prepareStatement(sql_update);
							 ps_sql_update.setString(1, fechaString);
							 ps_sql_update.setString(2, valor);
							 ps_sql_update.setDate(3, fechaSQL);
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
