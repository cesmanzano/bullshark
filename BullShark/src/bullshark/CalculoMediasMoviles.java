package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CalculoMediasMoviles {
	private static Connection connection=null;
	private static String valorExclusivo="";
	
    public static void main(String[] args) {
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
         calculoMediasMoviles(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoMediasMoviles(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoMediasMoviles() ;
    }

    public static boolean calculoMediasMoviles(Connection connection) {

    	CalculoMediasMoviles.connection = connection;
    	return calculoMediasMoviles() ;
    }

    private static boolean calculoMediasMoviles() {
                          
                     
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
        
        bucle("D");
        
        bucle("S");
        
        bucle("M");
		
		System.out.println("✔ Fin del proceso");
		return true;
    }
    
    private static void bucle(String plazo) {
        int nivel=0;
        String sql_select_configuracion = "SELECT dato FROM configuracion WHERE tipo='SMA' ORDER BY tipo, nivel";
        PreparedStatement ps_configuracion;
		try {
			ps_configuracion = connection.prepareStatement(sql_select_configuracion);
			ResultSet rs_configuracion = ps_configuracion.executeQuery();
		    while (rs_configuracion.next()) {
		    	
		    	nivel = rs_configuracion.getInt("dato");
		    	
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
						    	System.out.println(valor+" "+plazo+" "+nivel);
						    	
						    	if(valorExclusivo.equals("") || (!valorExclusivo.equals("") && valorExclusivo.equals(valor))) {
						    	
						    		CalculoMediasMoviles.calcula(valor, plazo, nivel);
						    		
						    	}
						    }//end while valores
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
				    	
			    }//end while configuracion
			} catch (SQLException e) {
				e.printStackTrace();
			}

    }
    
    
    
    
    
    /*
     * valor=BBVA, Santander, Apple, etc
     * plazo=D:diario, S:semanal, M:mensual
     * nivel=número de registros
     */
    private static void calcula(String valor, String plazo, int nivel) {
    	
    	//César aquí prueba mensual
    	
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
    		
			// int veces=0;
			 Double cerrar = 0.0;
			 Double suma = 0.0;
			 int posicion = 0;
			 
			 //día a día
			 
			 //movernos a la fila nivel -1 porque luego hacemos next
			 try {
				 rs.absolute(nivel);
				 System.out.println("Nivel: "+nivel +" Plazo: " +plazo+" "+rs.getString("valor")+ " "+rs.getString("fecha"));
			 }catch (SQLException e) {
				 System.out.println("Valor: "+valor+ " rs.absolute("+nivel+") error SQLException " + e);
			 }
			 //
			 while (rs.next()) {
				 System.out.println("Nivel: "+nivel +" Plazo: " +plazo+" "+rs.getString("valor")+ " "+rs.getString("fecha"));

				 int filaLeida=rs.getRow();
				 String fechaInsertString="";
				 suma = 0.0;
				 //leo n días atrás (nivel)
				 int numeroDeDias=0;
				 for (int i = 0; i < nivel; i++) {
					 try {
						 if(rs.previous()) {
							 if(i==0) {
								  fechaInsertString=rs.getString("fecha"); 								 
							 }
							 numeroDeDias++;
							 cerrar=rs.getDouble("cerrar");
							 suma=suma + cerrar;
							 System.out.println("Nivel: "+nivel +" Plazo: " +plazo+" "+rs.getString("valor")+ " "+rs.getString("fecha") + " contador: " + i);							 
						 }					
					 } catch (SQLException e) {
							e.printStackTrace();
					 }
				 }
				 if(numeroDeDias==nivel) {
					 //System.out.println("Hay datos para calcular medias móviles " + rs.getString("valor")+ " "+fechaInsert);
					 Double sma = suma / nivel;
					 //corregido el error Luismi
					 //Posición
					 if(cerrar>=sma) {
						 posicion=1;
					 }else {
						 posicion=-1;
					 }
					 
					 //Señal. Leer medias móviles del día anterior y ver si la posición es igual o no a esta que estamos calculando
					 // si la posición del día anterior es igual a la de hoy se pone 0
					 // si cambia de positivo a negativo es -1
					 // si cambia de negativo a positivo es 1
					 // si cambia se recoge la posición y si no cambia es 0
					 
					 
					 hacerInsert(valor, plazo, nivel, fechaInsertString, cerrar, sma, posicion);
					 
				 }
				 //movernos a la fila nivel+1
				 try {
					 rs.absolute(filaLeida);
				 }catch (SQLException e) {
					 System.out.println("Valor: "+valor+ " rs.absolute("+filaLeida+") error SQLException " + e);
				 }
			 } 
  		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    private static void hacerInsert(String valor, String plazo, int nivel, String fecha, Double cerrar, Double sma, int posicion) {
        try {
        	//delete primero 
        	String sql_delete_medias_moviles = "DELETE FROM medias_moviles WHERE valor = ? AND plazo = ? AND nivel = ? AND fecha = ?";
        	PreparedStatement ps__delete_medias_moviles = connection.prepareStatement(sql_delete_medias_moviles);
        	ps__delete_medias_moviles.setString(1, valor);
        	ps__delete_medias_moviles.setString(2, plazo);
        	ps__delete_medias_moviles.setInt(3, nivel);
        	ps__delete_medias_moviles.setString(4, fecha);
        	ps__delete_medias_moviles.executeUpdate();
        	
        	//insert en cotiza_mes
        	String sql_insert_medias_moviles = "INSERT INTO medias_moviles (valor, plazo, nivel, fecha, cotizacion, sma, posicion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        	PreparedStatement ps_medias_moviles = connection.prepareStatement(sql_insert_medias_moviles);
        	
        	ps_medias_moviles.setString(1, valor);
        	ps_medias_moviles.setString(2, plazo);
        	ps_medias_moviles.setInt(3, nivel);
        	ps_medias_moviles.setString(4, fecha);
        	ps_medias_moviles.setDouble(5, cerrar);
        	ps_medias_moviles.setDouble(6, sma);
        	ps_medias_moviles.setInt(7, posicion);
        	
        	ps_medias_moviles.executeUpdate();
        	connection.commit();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}

        }
}