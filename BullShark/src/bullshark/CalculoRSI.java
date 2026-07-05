package bullshark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CalculoRSI {
	private static Connection connection=null;
	
    public static void main(String[] args) {
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         
         calculoRSI(URL, USER, PASSWORD);
    }
    
    	
    public static boolean calculoRSI(String URL, String USER, String PASSWORD) {
    	
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return calculoRSI() ;
    }

    public static boolean calculoRSI(Connection connection) {

    	CalculoRSI.connection = connection;
    	return calculoRSI() ;
    }

    private static boolean calculoRSI() {
                          
                     
        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
        for (int i1 = 0; i1 < 3 ; i1++) {
        	
           int nivel=0; 		
           String valor=null;
           String plazo=null;
           
	           if (i1==0) {
	        	   plazo="D"; 	//diario    	
	           }else if (i1==1) {
	        	   plazo="S"; 	//Semanal    
	           }else {
	        	   plazo="M"; 	//Mensual    
	           }
    		
    					/*
    					 * leer maestro_valores        	 
    					 */
    						
    				        String sql_select_maestro_valores = "SELECT valor FROM maestro_valores ORDER BY valor";
    				        PreparedStatement ps_maestro_valores;
    						try {
    							ps_maestro_valores = connection.prepareStatement(sql_select_maestro_valores);
    							ResultSet rs_valores = ps_maestro_valores.executeQuery();
    						    while (rs_valores.next()) {
    						    	
    						    	valor = rs_valores.getString("valor");
    						    	
    						     	//delete primero 
    				            	String sql_delete_oscilador_rsi = "DELETE FROM oscilador_rsi WHERE valor = ? AND plazo = ?";
    				            	PreparedStatement ps_delete_oscilador_rsi = connection.prepareStatement(sql_delete_oscilador_rsi);
    				            	ps_delete_oscilador_rsi.setString(1, valor);
    				            	ps_delete_oscilador_rsi.setString(2, plazo);
    				            	ps_delete_oscilador_rsi.executeUpdate();
    				            	connection.commit();
    				      
    						    	
    				            	List<Registro> datos=null;
    						    	datos = obtenerDatos(valor, plazo);			    	   
    						    	
    						    	   
    						    	   //recuperar los niveles
    						            String sql_select_configuracion = "SELECT dato FROM configuracion WHERE tipo='RSI' ORDER BY tipo, nivel";
    						            PreparedStatement ps_configuracion;
    						    		try {
    						    			ps_configuracion = connection.prepareStatement(sql_select_configuracion);
    						    			ResultSet rs_configuracion = ps_configuracion.executeQuery();
    						    		    while (rs_configuracion.next()) {
    						    		    	
    						    		    	nivel = rs_configuracion.getInt("dato");

    						    		    	
    						// Calcular RSI----------------------------------------------------------------------------------------------------------------
    						    		    	  double incrementos = 0.0;
    						    		          double decrementos = 0.0;

    						    		          
    						    		          for (int i = 0; i < datos.size(); i++) {
    						    		    
    						    		          
    						    		          	if (i >= 1){	
    						    		          		
    						    		         // acumulamos incrementos y decrementos...
    						    		          		double anterior = datos.get(i - 1).cierre;
        						    		          	double actual = datos.get(i).cierre;			    		          	
    						    		          		double diferencia = 0.0;
    
        						    		          	diferencia = actual - anterior;        						    		          	 	
        						    		          	
        						    		          	if (diferencia >= 0) {
    						    		          			incrementos += diferencia;
    						    		          			
    						    		          		} else {	
    						    		          			decrementos += (diferencia * -1);
    						    		          		}
	
    						    		          		
    						    		  	      // Quitamos incremeto o decremento del último del periodo...
    						    		  	            if (i > nivel) {
    						    		  	            	double inicialAnterior = datos.get(i-nivel).cierre;
        						    		          		double inicialActual = datos.get(i-nivel+1).cierre;
        						    		          		
        						    		          		diferencia = inicialActual - inicialAnterior;        						    		          	 	
            						    		          	
            						    		          	if (diferencia >= 0) {
        						    		          			incrementos -= diferencia;
        						    		          			
        						    		          		} else {	
        						    		          			decrementos -= (diferencia * -1);
        						    		          		}
    						    		  	            }
    						    		               
    						    		  	       // calculamos formula RSI
    						    		  	            Double resultado = null;
    						    		  	            int senal = 0;
    						    		  	            
    						    		  	            if (i >= nivel) {
    						    		  	            	double promedioSubidas = incrementos / nivel;
    						    		  	            	double promedioBajadas = decrementos / nivel;
    						    		  	            	
    						    		  	            	resultado = 100 - (100 / (1 + ( promedioSubidas / promedioBajadas )));
    						    		  	              
    						    		  	            	if (resultado < 0 ) {
    						    		  	            		resultado = resultado * -1;
    						    		  	            	}
    						    		  	                
    						    		  	            	// calculo de posición 
    						    		  	           	    if (resultado <= 20) {
    						    		  	           	    	senal = 2;
    						    		  	           	    }else if (resultado <= 30) {
    						    		  	           	    	senal = 1;
    						    		  	           	    }else if (resultado < 70) {
    						    		  	           	    	senal = 0;
    						    		  	           	    }else if (resultado <= 80) {
    						    		  	           	    	senal = -1;
    						    		  	           	    }else {
    						    		  	           	    	senal = -2;
    						    		  	           	    }
    						    		  	           	 	
    						    		  	           	    datos.get(i).resultado = resultado; 
    						    		  	                datos.get(i).senal = senal;
    						    		  	                
    						    		  	                datos.get(i).incrementos = incrementos;
    						    		  	                datos.get(i).decrementos = decrementos; 
  						    		  	                    
    						    		  	            }
    						    		          	}
    						    		          }
    						            
    						    // Guardar en BD------------------------------------------------------------------------------------------------
    						    		       //insert en oscilador_rsi
    						    		       	String sql_insert_oscilador_rsi = "INSERT INTO oscilador_rsi (valor, plazo, nivel, fecha, resultado, senal, cotizacion, incrementos, decrementos) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    						    		        try {
    						    		        	PreparedStatement ps_oscilador_rsi = connection.prepareStatement(sql_insert_oscilador_rsi);
    						    		     
    						    		            int contador = 0;

    						    		            for (Registro r : datos) {


    						    		            	ps_oscilador_rsi.setString(1, r.valor);
    						    		              	ps_oscilador_rsi.setString(2, plazo);
    						    		              	ps_oscilador_rsi.setInt(3, nivel);
    						    		              	ps_oscilador_rsi.setString(4, r.fecha);
    						    		                ps_oscilador_rsi.setDouble(5, r.resultado);	
    						    		                ps_oscilador_rsi.setInt(6, r.senal);
    						    		                ps_oscilador_rsi.setDouble(7, r.cierre);
    						    		                ps_oscilador_rsi.setDouble(8, r.incrementos);
    						    		                ps_oscilador_rsi.setDouble(9, r.decrementos);	
    						    		                
    						    		              	ps_oscilador_rsi.addBatch();
    						    		                contador++;

    						    		                // Ejecutar cada 100 para rendimiento
    						    		                if (contador % 100 == 0) {
    						    		                	ps_oscilador_rsi.executeBatch();
    						    		                    connection.commit();
    						    		                }
    						    		            }

    						    		            ps_oscilador_rsi.executeBatch(); // últimos
    						    		            connection.commit();

    						    		      	} catch (SQLException e) {
    						    		    			e.printStackTrace();
    						    		    	}

    						            
    					    			    }//end while configuracion
    					    			} catch (SQLException e) {
    					    				e.printStackTrace();
    					    			}
    					        	

    						       
    						    }//end while valores
    						} catch (SQLException e) {
    							e.printStackTrace();
    							return false;
    						} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								return false;
							}
    						
				if (plazo == "D") {
					System.out.println("RSI diario calculado y guardado correctamente");   		
				} else if (plazo == "S") {
					System.out.println("RSI Semanal calculado y guardado correctamente");   		
				}else {
					System.out.println("RSI Mensual calculado y guardado correctamente");   		
				}
    						
    						
        }
  
        	System.out.println("RSI calculado y guardado correctamente");   							
			System.out.println("✔ Fin del proceso");
			return true;
		
    }

    // 🔹 Clase para manejar cada fila
    static class Registro {
    	String valor;
        String fecha;
        double cierre;
        
        double resultado;
        int senal;
        
        double incrementos;
        double decrementos;
        
        public Registro(String valor, String fecha, double cierre) {
        	this.valor = valor;
        	this.fecha = fecha;
            this.cierre = cierre;
        }
    }

    // 1. Leer datos cotización diaria...
    private static List<Registro> obtenerDatos(String valor, String plazo) throws Exception {
        List<Registro> lista = new ArrayList<>();
        
        
        String sql = null;
        if (plazo == "D") {
        	sql = "SELECT valor, fecha, cerrar FROM cotiza_dia WHERE valor= ? ORDER BY fecha";
        }else if (plazo == "S") {
        	sql = "SELECT valor, fecha, cerrar FROM cotiza_semana WHERE valor= ? ORDER BY fecha";
        }else {
        	sql = "SELECT valor, fecha, cerrar FROM cotiza_mes WHERE valor= ? ORDER BY fecha";
        }
        
        
        PreparedStatement ps_cotiza;
        
  	  try {
  		  ps_cotiza = connection.prepareStatement(sql);
  		  ps_cotiza.setString(1, valor);
          ResultSet rs = ps_cotiza.executeQuery();
          

            while (rs.next()) {
                lista.add(new Registro(
                		rs.getString("valor"),
                        rs.getString("fecha"),
                        rs.getDouble("cerrar")
                ));
            }
	  	} catch (SQLException e) {
			e.printStackTrace();
		}


        return lista;
    }

 
}