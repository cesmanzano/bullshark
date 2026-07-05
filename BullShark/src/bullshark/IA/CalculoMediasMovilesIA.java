package bullshark.IA;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalculoMediasMovilesIA {

	 private static final String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
	 private static final String USER = "bolsa"; 
	 private static final String PASS = "bolsa";
	
	 private static Connection connection=null;
	
    public static void main(String[] args) {
        try {
        	connection = DriverManager.getConnection(URL, USER, PASS);
        	
            int nivel=0; 		
            String plazo="D"; 	//diario
            
    		    	
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
    						    	
    						     	//delete primero 
    				            	String sql_delete_medias_moviles = "DELETE FROM medias_moviles WHERE valor = ? AND plazo = ?";
    				            	PreparedStatement ps__delete_medias_moviles = connection.prepareStatement(sql_delete_medias_moviles);
    				            	ps__delete_medias_moviles.setString(1, valor);
    				            	ps__delete_medias_moviles.setString(2, plazo);
    				            	ps__delete_medias_moviles.executeUpdate();
    				      
    						    	
    						    	   List<Registro> datos = obtenerDatos(valor);
    						    	   
    						    	    //recuperar los niveles
    						            String sql_select_configuracion = "SELECT dato FROM configuracion WHERE tipo='EMA' ORDER BY tipo, nivel";
    						            PreparedStatement ps_configuracion;
    						    		try {
    						    			ps_configuracion = connection.prepareStatement(sql_select_configuracion);
    						    			ResultSet rs_configuracion = ps_configuracion.executeQuery();
    						    		    while (rs_configuracion.next()) {
    						    		    	
    						    		    	nivel = rs_configuracion.getInt("dato");

    						            // Calcular medias
    						            calcularSMA(datos, nivel);
    						            
    						            // Guardar en BD
    						            guardarDatos(datos, nivel, plazo);
    						            
    					    			    }//end while configuracion
    					    			} catch (SQLException e) {
    					    				e.printStackTrace();
    					    			}
    					        	

    						       
    						    }//end while valores
    						} catch (SQLException e) {
    							e.printStackTrace();
    						}
    						
    					     System.out.println("Medias móviles calculadas y guardadas correctamente");
		    	
         
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Clase para manejar cada fila
    static class Registro {
    	String valor;
        Date fecha;
        double cierre;

        Double sma20;
        Double sma50;
        Double sma100;
        Double sma200;
        Double sma500;

        int posicion;
        
        public Registro(String valor, Date fecha, double cierre) {
        	this.valor = valor;
        	this.fecha = fecha;
            this.cierre = cierre;
        }
    }

    // 🔹 1. Leer datos
    public static List<Registro> obtenerDatos(String valor) throws Exception {
        List<Registro> lista = new ArrayList<>();
        
        String sql = "SELECT valor, fecha, cerrar FROM cotiza_dia WHERE valor= ? ORDER BY fecha";
        PreparedStatement ps_cotiza;
        
  	  try {
  		  ps_cotiza = connection.prepareStatement(sql);
  		  ps_cotiza.setString(1, valor);
          ResultSet rs = ps_cotiza.executeQuery();
          

            while (rs.next()) {
                lista.add(new Registro(
                		rs.getString("valor"),
                        rs.getDate("fecha"),
                        rs.getDouble("cerrar")
                ));
            }
	  	} catch (SQLException e) {
			e.printStackTrace();
		}


        return lista;
    }

    // 🔹 2. Calcular SMA optimizada
    public static void calcularSMA(List<Registro> datos, int periodo) {
        double suma = 0.0;

        for (int i = 0; i < datos.size(); i++) {
            suma += datos.get(i).cierre;

            if (i >= periodo) {
                suma -= datos.get(i - periodo).cierre;
            }

            Double resultado = null;
            int posicion=0;
            if (i >= periodo - 1) {
                resultado = suma / periodo;
                
           	 if(datos.get(i).cierre>=resultado) {
				 posicion=1;
			 }else {
				 posicion=-1;
			 }
	    
                // Guardar en el campo correspondiente
                switch (periodo) {
	                case 20 -> datos.get(i).sma20 = resultado;
	                case 50 -> datos.get(i).sma50 = resultado;
	                case 100 -> datos.get(i).sma100 = resultado;
	                case 200 -> datos.get(i).sma200 = resultado;
	                case 500 -> datos.get(i).sma500 = resultado;
                }
                datos.get(i).posicion = posicion; 
            }

        }
    }

    // 🔹 3. Guardar resultados en BD (batch)
    public static void guardarDatos(List<Registro> datos, int nivel, String plazo) throws Exception {
          	
          	//insert en cotiza_mes
          	String sql_insert_medias_moviles = "INSERT INTO medias_moviles (valor, plazo, nivel, fecha, resultado, posicion) VALUES (?, ?, ?, ?, ?, ?)";
          
          	
     //   String sql = "UPDATE medias_moviles SET resultado = ? WHERE valor =?, fecha = ?";

        try {
        	PreparedStatement ps_medias_moviles = connection.prepareStatement(sql_insert_medias_moviles);
            // PreparedStatement ps = connection.prepareStatement(sql)) {

            int contador = 0;

            for (Registro r : datos) {
//                ps.setObject(1, r.valor);
//                ps.setObject(2, r.fecha);
//                ps.setObject(3, r.sma);

            	ps_medias_moviles.setString(1, r.valor);
              	ps_medias_moviles.setString(2, plazo);
              	ps_medias_moviles.setInt(3, nivel);
              	
              	//Convertir fecha de Date a String de 8 posiciones
	        	  Date fechaSQL = r.fecha;
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
	        	  String fecha=""+año+mesString+diaString;

              	ps_medias_moviles.setString(4, fecha);
              	
                // Guardar en el campo correspondiente
                switch (nivel) {
                    case 20:
                    	if(r.sma20!=null) {
                      		ps_medias_moviles.setDouble(5, r.sma20);	
                      	}else {
                      		ps_medias_moviles.setDouble(5, 0);
                      	}                      
                    	 break;
                    case 50:
                    	if(r.sma50!=null) {
                      		ps_medias_moviles.setDouble(5, r.sma50);	
                      	}else {
                      		ps_medias_moviles.setDouble(5, 0);
                      	}
                    	 break;
                    case 100:
                    	if(r.sma100!=null) {
                      		ps_medias_moviles.setDouble(5, r.sma100);	
                      	}else {
                      		ps_medias_moviles.setDouble(5, 0);
                      	} 
                    	 break;
                    case 200:
                    	if(r.sma200!=null) {
                      		ps_medias_moviles.setDouble(5, r.sma200);	
                      	}else {
                      		ps_medias_moviles.setDouble(5, 0);
                      	}             
                    	 break;
                    case 500:
                    	if(r.sma500!=null) {
                      		ps_medias_moviles.setDouble(5, r.sma500);	
                      	}else {
                      		ps_medias_moviles.setDouble(5, 0);
                      	}        
                    	 break;
                }

                ps_medias_moviles.setDouble(6, r.posicion);	
              	
              	//ps_medias_moviles.executeUpdate();
              	//connection.commit();
      
                
                
              	ps_medias_moviles.addBatch();
                contador++;

                // Ejecutar cada 1000 para rendimiento
                if (contador % 1000 == 0) {
                	ps_medias_moviles.executeBatch();
                }
            }

            ps_medias_moviles.executeBatch(); // últimos

      	} catch (SQLException e) {
    			e.printStackTrace();
    	}

    }
}