package bullshark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class rectaTendenciaMySQL {
	
	/**
	 * Esta clase extrae datos desde un fichero txt y calcula los dos mínimos. 
	 */
	
	private static File ficheroCotizaciones = new File ("C:\\Fernancio\\ton\\datosCotizaciones.txt");
	private static String ficheroCotizacionesMinimas="C:\\Fernancio\\ton\\datosCotizacionesMinimas.txt";
	
    public static void main(String [] arg) {
	      FileReader fr = null;
	      BufferedReader br = null;
	      int primeraMinimaCotizacion = Integer.MAX_VALUE;  
	      int segundaMinimaCotizacion = Integer.MAX_VALUE;
	      int primeraFechaMinimaCotizacion = 0;
	      int segundaFechaMinimaCotizacion = 0;

	      int primeraMaximaCotizacion = Integer.MIN_VALUE;  
	      int segundaMaximaCotizacion = Integer.MIN_VALUE;
	      int primeraFechaMaximaCotizacion = 0;
	      int segundaFechaMaximaCotizacion = 0;
	      
	     //Apertura del fichero y creacion de BufferedReader para poder hacer una lectura comoda (disponer del metodo readLine()).       
	      try{
	         fr = new FileReader (ficheroCotizaciones);
	         br = new BufferedReader(fr);
	         
	      //Lectura del fichero
	         String registro;          
	         int cotizacion = 0;
	         //Declaramos un valor inicial muy grande
	         while((registro = br.readLine())!=null){
	        //Dividir campos por el ";"
	        	 String[] campos = registro.split(";");
	        //Tomamos la cotizacion del campo2 -> campos[2]
	         	cotizacion = Integer.parseInt(campos[2]);

	         //MENORES COTIZACIONES
	         	//Comparamos y guardamos las MENORES cotizaciones y sus fechas:
	        	if (cotizacion < primeraMinimaCotizacion) {
	        		segundaMinimaCotizacion      = primeraMinimaCotizacion;
	        		primeraMinimaCotizacion      = cotizacion;
	        		primeraFechaMinimaCotizacion = Integer.parseInt(campos[1]);
	        	}else if (primeraMinimaCotizacion < cotizacion && cotizacion < segundaMinimaCotizacion) {
	        		segundaMinimaCotizacion      = cotizacion;
	        		segundaFechaMinimaCotizacion = Integer.parseInt(campos[1]);
	        	}
	        	
	         //MAYORES COTIZACIONES
	        	//Comparamos y guardamos las MAYORES cotizaciones y sus fechas:
	        	if (cotizacion > primeraMaximaCotizacion) {
	        		segundaMaximaCotizacion      = primeraMaximaCotizacion;
	        		primeraMaximaCotizacion      = cotizacion;
	        		primeraFechaMaximaCotizacion = Integer.parseInt(campos[1]);
	        	}else if (primeraMaximaCotizacion > cotizacion && cotizacion > segundaMaximaCotizacion) {
	        		segundaMaximaCotizacion      = cotizacion;
	        		segundaFechaMaximaCotizacion = Integer.parseInt(campos[1]);
	        	}
	         }
	         
	         System.out.println("Primera Mínima Cotizacion: " 		+ primeraMinimaCotizacion);
	         System.out.println("Fecha Primera Mínima Cotizacion: " + primeraFechaMinimaCotizacion);
	         System.out.println("Segunda Mínima Cotizacion: " 		+ segundaMinimaCotizacion);
	         System.out.println("Fecha Segunda Mínima Cotizacion: " + segundaFechaMinimaCotizacion);
	         
	         System.out.println("Primera Máxima Cotizacion: " 		+ primeraMaximaCotizacion);
	         System.out.println("Fecha Primera Máxima Cotizacion: " + primeraFechaMaximaCotizacion);
	         System.out.println("Segunda Máxima Cotizacion: " 		+ segundaMaximaCotizacion);
	         System.out.println("Fecha Segunda Máxima Cotizacion: " + segundaFechaMaximaCotizacion);
	      }
	      catch(Exception e){
	         e.printStackTrace();
	      }finally{
	      // En el finally cerramos el fichero, para asegurarnos de que se cierra tanto si todo va bien como si salta una excepcion.
	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
	      }
	      
          //Guardamos datos en ficheros.
	         String datosCotizacionesMinimas = primeraMinimaCotizacion + ";" + primeraFechaMinimaCotizacion + ";" + segundaMinimaCotizacion + ";" + segundaFechaMinimaCotizacion;
	         EscriboFicheroCotizacionesMinimas(ficheroCotizacionesMinimas, datosCotizacionesMinimas, false);
	      
	      if (!ficheroCotizaciones.exists()){
              System.out.println("Error. No existe fichero de cotización");
              return;
          }
	      
	    //Llama a este método enviando una fecha y un valor de cotización. 
	    //Si este valor está por encima de la línea de tendencia, devuelve un 1. Si no, devuelve un 0.
	      int alzaBaja = evaluarCotizacion(20260310,11);
	      System.out.println("Resultado: " + alzaBaja);  
	   }
	 
   private static boolean EscriboFicheroCotizacionesMinimas(String fichero, String texto, boolean add){
			OutputStreamWriter outputStreamWriter = null;
				try{
			    	File file=new File(fichero);
			    	File directorio=new File(file.getParent());
			    	if(!directorio.exists()){
			    		directorio.mkdirs();
			    	}
					outputStreamWriter = new OutputStreamWriter( new FileOutputStream(fichero,add));			
					BufferedWriter bufferedWriter=new BufferedWriter(outputStreamWriter);
					bufferedWriter.write(texto+"\r\n");
					bufferedWriter.flush();
				}catch(FileNotFoundException e)	{		
					return false;
				}catch(IOException e)	{		
					return false;
				}finally{
					try{
						outputStreamWriter.close();
					}catch(Exception e1){
						 e1.printStackTrace();
					}
				}		
			return true;		
		}
   
   public static int evaluarCotizacion(int fecha, int cotizacion){	 
	   
//	   private static String ficheroCotizacionesMinimas="C:\\Fernancio\\ton\\datosCotizacionesMinimas.txt";
	   int primeraMinimaCotizacion = 0;  
	   int segundaMinimaCotizacion = 0;
	   int primeraFechaMinimaCotizacion = 0;
	   int segundaFechaMinimaCotizacion = 0;
	   FileReader fr = null;
	   BufferedReader br = null;
	   
	   try{
	        fr = new FileReader (ficheroCotizacionesMinimas);
	        br = new BufferedReader(fr);
	      //Lectura del fichero
		   String registro;          
	       while((registro = br.readLine())!=null){
	      //Dividir campos por el ";"
	    	   String[] campos = registro.split(";");
	      //Tomamos la cotizacion del campo2 -> campos[2]
	    	   primeraMinimaCotizacion 	    = Integer.parseInt(campos[0]);
	    	   primeraFechaMinimaCotizacion = Integer.parseInt(campos[1]);
	    	   segundaMinimaCotizacion 	    = Integer.parseInt(campos[2]);
	    	   segundaFechaMinimaCotizacion = Integer.parseInt(campos[3]);
	       }
	   }catch (Exception e) {
	   }finally{
	      //En el finally cerramos el fichero, para asegurarnos de que se cierra tanto si todo va bien como si salta una excepcion.
		         try{                    
		            if( null != fr ){   
		               fr.close();     
		            }                  
		         }catch (Exception e2){ 
		            e2.printStackTrace();
		         }
	}
	   
      //Calculamos recta de tendencia.
	   // x = fechas
	   // y = cotizaciones
	   // y=(y2-y1)/(x2-x1)*(x-x1)+y1
	   	   
	      int pendiente = 0;
	      int cotizacionExtrapolada = 0;

	      pendiente  = (segundaFechaMinimaCotizacion - primeraFechaMinimaCotizacion) / (segundaMinimaCotizacion - primeraMinimaCotizacion);	      
	      cotizacionExtrapolada = pendiente * (fecha - primeraFechaMinimaCotizacion) + primeraMinimaCotizacion;
	      
	   //Comparamos la cotización recibida con la calculada mediante la recta de tendencia. 
	      if(cotizacion >= cotizacionExtrapolada) {
	    	  return 1; //Al alza.
	      }else {
	    	  return -1;//A la baja.
	      }
   }
}