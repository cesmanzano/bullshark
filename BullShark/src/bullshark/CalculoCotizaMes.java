package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CalculoCotizaMes {
	private static Connection connection=null;
	private static String valorExclusivo="";
	
    public static void main(String[] args) {
    	
    	
    	
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
    	calculoCotizaMes(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoCotizaMes(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoCotizaMes() ;
    }

    public static boolean calculoCotizaMes(Connection connection) {

    	CalculoCotizaMes.connection = connection;
    	return calculoCotizaMes() ;
    }

    private static boolean calculoCotizaMes() {
                          
                     
        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    
        
/*
 * cotiza_dia        	 
 */
        String sql_select_cotiza_dia = "";
    	if(valorExclusivo.equals("")) {
    		sql_select_cotiza_dia =	"SELECT valor, fecha, apertura, alza, baja, cerrar, volumen, cambio FROM cotiza_dia ORDER BY valor, fecha";
    	}else {
    		sql_select_cotiza_dia =	"SELECT valor, fecha, apertura, alza, baja, cerrar, volumen, cambio FROM cotiza_dia WHERE valor='"+valorExclusivo+"' ORDER BY valor, fecha";
    	}
        
        
        PreparedStatement ps_cotiza_dia;
		try {
			ps_cotiza_dia = connection.prepareStatement(sql_select_cotiza_dia);
			
			ResultSet rs = ps_cotiza_dia.executeQuery();
			
			String valor = null;
			String valor_anterior = null;
			//Date fecha_anterior = null;
			
			String fecha = null;
			int mes_anterior = 0;
			int anno_anterior = 0;
			
			Double alza_maximo = null;
			Double baja_minimo = null;			
			Double apertura = null;
			Double cerrar =	null;
			Double volumen_suma = null;
			//Double cambio =	null;
			
			
			
	        while (rs.next()) {
	        	
	        	valor = rs.getString("valor");
	        	
	            //Date fecha = rs.getDate("fecha");
	        	fecha = rs.getString("fecha");
	            //LocalDate localDate = fecha.toLocalDate();
	            
	            //int anno=localDate.getYear();
	        	int anno=Integer.parseInt(fecha.substring(0, 4));
	        	
	            //int mes = localDate.getMonthValue();
	            int mes=Integer.parseInt(fecha.substring(4, 6));
	            
	            //System.out.println("Fecha:" + localDate + " Mes del año: " + mes);
	                   	
	        	//cambio de valor
	        	if((valor_anterior!= null && !valor_anterior.equals(valor))
	        		||
	        	//cambio de mes	        		
	        		(mes_anterior!=0 && mes_anterior!=mes)) {
	        		
		        	if(valor_anterior!= null && !valor_anterior.equals(valor)){
		        		System.out.println("Cotiza Mes Valor: " + valor_anterior);
		        	}
		        	
	        		hacerInsert(valor_anterior, anno_anterior, mes_anterior, apertura, alza_maximo, baja_minimo, cerrar, volumen_suma, 0.0);//TODO calcular cambio
	        		
	        		alza_maximo = null;
	    			baja_minimo = null;
	    			apertura=null;
	    			cerrar=null;
	    			volumen_suma = null;
	        	}	        			        			            	            	
	            
	            
	        	if (mes!=mes_anterior) {
	        		//primer día del mes recupero apertura
	        		apertura = rs.getDouble("apertura");
	        	}
	        	
	            //último día del mes recupero cerrar
	            cerrar = rs.getDouble("cerrar");
	            
	            //alza_maximo es el mayor alza
	            Double alza = rs.getDouble("alza");	            
	        	if(alza_maximo==null || alza>alza_maximo) {
	        		alza_maximo=alza;
	        	}
        	 
	        	//baja_minimo es el menor baja
	            Double baja = rs.getDouble("baja");	            
	        	if(baja_minimo==null || baja<baja_minimo) {
	        		baja_minimo=baja;
	        	}
	        	
	        	//volumen es la suma
	            Double volumen = rs.getDouble("volumen");	            
	            volumen_suma=(volumen_suma != null ? volumen_suma : 0.0) + (volumen != null ? volumen : 0.0);
	        	
	        	
	            valor_anterior = valor;
	            anno_anterior = anno;
	        	//fecha_anterior = fecha;
	        	mes_anterior = mes;
	        }
	        
	    	if(valor!= null ){
        		System.out.println("Valor: " + valor);
        	}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		 System.out.println("✔ Fin del proceso");
        
        return true;
    }
    
    
    private static void hacerInsert(String valor, int anno, int mes, Double apertura, Double alza, Double baja, Double cerrar, Double volumen, Double cambio) {
    try {
    	//delete primero 
    	String sql_delete_cotiza_mes = "DELETE FROM cotiza_mes WHERE valor = ? AND año = ? AND mes = ?";
    	PreparedStatement ps__delete_cotiza_mes = connection.prepareStatement(sql_delete_cotiza_mes);
    	ps__delete_cotiza_mes.setString(1, valor);
    	ps__delete_cotiza_mes.setInt(2, anno);
    	ps__delete_cotiza_mes.setInt(3, mes);
    	ps__delete_cotiza_mes.executeUpdate();
    	
    	//insert en cotiza_mes
    	String sql_insert_cotiza_mes = "INSERT INTO cotiza_mes (valor, año, mes, apertura, alza, baja, cerrar, volumen, cambio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	PreparedStatement ps_cotiza_mes = connection.prepareStatement(sql_insert_cotiza_mes);
    	
    	ps_cotiza_mes.setString(1, valor);
    	ps_cotiza_mes.setInt(2, anno);
    	ps_cotiza_mes.setInt(3, mes);
    	ps_cotiza_mes.setDouble(4, apertura);
    	ps_cotiza_mes.setDouble(5, alza);
    	ps_cotiza_mes.setDouble(6, baja);
    	ps_cotiza_mes.setDouble(7, cerrar);
        ps_cotiza_mes.setDouble(8, volumen);
    	ps_cotiza_mes.setDouble(9, cambio);
    	
    	ps_cotiza_mes.executeUpdate();
    	connection.commit();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }
}