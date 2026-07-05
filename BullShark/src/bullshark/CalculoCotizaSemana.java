package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;


public class CalculoCotizaSemana {
	private static Connection connection=null;
	private static String valorExclusivo="";
	
    public static void main(String[] args) {
    	//conexión
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
    	calculoCotizaSemana(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoCotizaSemana(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoCotizaSemana(connection) ;
    }

    public static boolean calculoCotizaSemana(Connection connection) {

    	CalculoCotizaSemana.connection = connection;
    	return calculoCotizaSemana() ;
    }
    
    private static boolean calculoCotizaSemana() {
                          
                     
        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    
        
/*
 * cotiza_dia        	 
 */
        String sql_select_cotiza_dia ="";
        
       	if(valorExclusivo.equals("")) {
       		sql_select_cotiza_dia="SELECT valor, fecha, apertura, alza, baja, cerrar, volumen, cambio FROM cotiza_dia ORDER BY valor, fecha";
       	}else {
       		sql_select_cotiza_dia="SELECT valor, fecha, apertura, alza, baja, cerrar, volumen, cambio FROM cotiza_dia WHERE valor='"+valorExclusivo+"' ORDER BY valor, fecha";
       	}
        
        
        PreparedStatement ps_cotiza_dia;
		try {
			ps_cotiza_dia = connection.prepareStatement(sql_select_cotiza_dia);
			
			ResultSet rs = ps_cotiza_dia.executeQuery();
			
			String valor = null;
			String valor_anterior = null;
			String fecha = null;
			Date fecha_actual = null;
			Date fecha_anterior = null;
			int semana_anterior = 0;
			int anno_anterior = 0;
			
			Double alza_maximo = null;
			Double baja_minimo = null;			
			Double apertura = null;
			Double cerrar =	null;
			Double volumen_suma = null;
			//Double cambio =	null;
			int numero_de_dias_en_la_semana = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
	        while (rs.next()) {
	        	
	        	valor = rs.getString("valor");
	        	
	            //Date fecha = rs.getDate("fecha");
	          	fecha = rs.getString("fecha");
	          	
	          	try {
					fecha_actual = sdf.parse(fecha);
				} catch (ParseException e) {

					e.printStackTrace();
				}
	          	
	            LocalDate localDate = LocalDate.ofInstant(fecha_actual.toInstant(),ZoneId.systemDefault());
	            
	            //int anno=localDate.getYear();
	          	int anno=Integer.parseInt(fecha.substring(0, 4));
	            
	            int semana = localDate.get(WeekFields.of(Locale.getDefault()).weekOfYear());	            
	            
	            //System.out.println("Fecha:" + localDate + " Semana del año: " + semana);
	            
	         // Ajustar para que nunca pase de 52
	            if(semana==53) {
	            	semana=1;
	            	anno++;
	            	//System.out.println("Fecha:" + localDate + " Semana del año modificada: " + semana);
	            }
	            
	            
	            
	            
	        	//cambio de valor
	        	if((valor_anterior!= null && !valor_anterior.equals(valor))
	        		||
	        	//cambio de semana	        		
	        		(semana_anterior!=0 && semana_anterior!=semana)) {
	        		
	        		
		        	if(valor_anterior!= null && !valor_anterior.equals(valor)){
		        		System.out.println("Cotiza Semana Valor: " + valor_anterior);
		        	}
	        		
	        		hacerInsert(valor_anterior, anno_anterior, semana_anterior, numero_de_dias_en_la_semana, apertura, alza_maximo, baja_minimo, cerrar, volumen_suma, 0.0);//TODO calcular cambio
	        		
	        		alza_maximo = null;
	    			baja_minimo = null;
	    			apertura=null;
	    			cerrar=null;
	    			volumen_suma = null;
	    			numero_de_dias_en_la_semana = 0;
	        	}	        			        			            	            	
	            
	            
	            //primer dia de la semana recupero apertura
	            if (esPrimerDiaSemana(fecha_actual, fecha_anterior)) {
	            	apertura = rs.getDouble("apertura");
	            }

	            //viernes recupero cerrar	            
//	            DayOfWeek diaSemana = localDate.getDayOfWeek();	 
	            //System.out.println(diaSemana); // MONDAY, TUESDAY, etc.
	           // if(diaSemana == DayOfWeek.FRIDAY) {
		            cerrar = rs.getDouble("cerrar");
	           // }
	            
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
	        	
	            numero_de_dias_en_la_semana++;
	        	
	            valor_anterior = valor;
	            anno_anterior = anno;
	        	fecha_anterior = fecha_actual;
	        	semana_anterior = semana;
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
    private static boolean esPrimerDiaSemana(Date actual, Date anterior) {

        // Si no hay fecha anterior, es primer día de semana
        if (anterior == null) {
            return true;
        }
        
        //Pasar de Date a LocalDate
        LocalDate fechaActual = LocalDate.ofInstant(actual.toInstant(),ZoneId.systemDefault());
        LocalDate fechaAnterior = LocalDate.ofInstant(anterior.toInstant(),ZoneId.systemDefault());

        WeekFields wf = WeekFields.ISO; // lunes como primer día

        int semanaActual = fechaActual.get(wf.weekOfYear());
        int anioSemanaActual = fechaActual.get(wf.weekBasedYear());

        int semanaAnterior = fechaAnterior.get(wf.weekOfYear());
        int anioSemanaAnterior = fechaAnterior.get(wf.weekBasedYear());

        return semanaActual != semanaAnterior || anioSemanaActual != anioSemanaAnterior;
    }
    
    private static void hacerInsert(String valor, int anno, int semana, int numero_de_dias_en_la_semana, Double apertura, Double alza, Double baja, Double cerrar, Double volumen, Double cambio) {
    try {
    	
    	//componer fecha con anno + semana
    	String semanaString=String.valueOf(semana)	;
    	if(semanaString.length()<2) {
    		semanaString="0"+semanaString;
  	  	}
    	String fecha=anno+semanaString+"00";
    	
    	//delete primero 
    	String sql_delete_cotiza_semana = "DELETE FROM cotiza_semana WHERE valor = ? AND fecha = ? ";
    	//System.out.println(sql_delete_cotiza_semana + " valor=" + valor + " fecha="+ fecha);
    	
    	PreparedStatement ps__delete_cotiza_semana = connection.prepareStatement(sql_delete_cotiza_semana);
    	ps__delete_cotiza_semana.setString(1, valor);
    	ps__delete_cotiza_semana.setString(2, fecha);
    	ps__delete_cotiza_semana.executeUpdate();
    	//System.out.println("Filas borradas: "+ps__delete_cotiza_semana.executeUpdate());
    	
    	//insert en cotiza_semana
    	String sql_insert_cotiza_semana = "INSERT INTO cotiza_semana (valor, fecha, dias, apertura, alza, baja, cerrar, volumen, cambio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	PreparedStatement ps_cotiza_semana = connection.prepareStatement(sql_insert_cotiza_semana);
    	
    	ps_cotiza_semana.setString(1, valor);
    	ps_cotiza_semana.setString(2, fecha);
    	ps_cotiza_semana.setInt(3, numero_de_dias_en_la_semana);
    	ps_cotiza_semana.setDouble(4, apertura);
    	ps_cotiza_semana.setDouble(5, alza);
    	ps_cotiza_semana.setDouble(6, baja);
    	ps_cotiza_semana.setDouble(7, cerrar);
        ps_cotiza_semana.setDouble(8, volumen);
    	ps_cotiza_semana.setDouble(9, cambio);
    	
    	ps_cotiza_semana.executeUpdate();
    	connection.commit();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }
}