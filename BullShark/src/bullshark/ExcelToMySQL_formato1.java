package bullshark;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class ExcelToMySQL_formato1 {
	 private static final int BATCH_SIZE = 1000;
    
    public static void main(String[] args) {
    	
         String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
         String USER = "bolsa"; 
         String PASSWORD = "bolsa";
         //String excelPath = "C:/bullshark/Quotes-6212.xls";
         //String excelPath = "C:/bullshark/quotes_Banco Santander S.A..xls";
         String excelPath = "C:/bullshark/cotizaciones";
         
         
    	exceltomysql(URL, USER, PASSWORD, excelPath);
    }
    
    	
    public static boolean exceltomysql(String URL, String USER, String PASSWORD, String excelPath) {
    	Connection connection=null;
    	try {
			 connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return exceltomysql(connection,  excelPath, true) ;
    }
    
    public static boolean exceltomysql(Connection connection, String excelPath, boolean batch) {
        //String excelPath = "C:/bullshark/Quotes-6212.xls";
                        
        
//1        Acción	BBVA (MSE)					
//2        Período	24/02/2015 - 24/02/2026					
//3        Moneda	EUR					
//4        						
//5        Fecha	Abrir	Al alza	A la baja	cerrar	Volumen	Cambio %
        
        

        try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    
        File file = new File(excelPath);
        
        //Si lo que llega es un archivo .xls
        if(file.isFile()) {
        	System.out.println(excelPath);
        	leerExcel(connection, excelPath, batch);
        }
        
        //Si lo que llega es un directorio, leer los archivos .xls
        if(file.isDirectory()) {
        	File[] archivos = file.listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".xls"));

            if (archivos != null) {
                for (File archivo : archivos) {
                    System.out.println(archivo.getName());
                    leerExcel(connection, archivo.getPath(), batch);
                }
            }
        }
        
        return true;        
    }
/*
 * Leer una excel y grabar los datos en db
 */
    private static boolean leerExcel(Connection connection, String nombreFichero, boolean batch) {
    	
        List<String> errores = new ArrayList<>();
    	
        try (
    	 
        		
            FileInputStream fichero = new FileInputStream(nombreFichero);
        		
        		
        		
        	Workbook workbook = new HSSFWorkbook(fichero); // .xls
        		//workbook = new HSSFWorkbook(fichero);
//        		if(excelPath.lastIndexOf(".xls")) {
//        			
//        		}else {
//        				workbook = new XSSFWorkbook(fichero);
//    			}
            
    		
        ) {

            Sheet sheet = (Sheet) workbook.getSheetAt(0);//primera hoja

            // Cabecera
            
            
/*
 * maestro_valores
 */
            
            String sql_maestro_valores  = "INSERT INTO maestro_valores (valor, moneda, ISIN, mercado) VALUES (?, ?, ?, ?)";
            PreparedStatement ps_maestro_valores = connection.prepareStatement(sql_maestro_valores);
            
          //Nombre Valor
            Row row0 = sheet.getRow(0); 
            	String valor = row0.getCell(1).getStringCellValue();
    	   //Moneda
        	 Row row2 = sheet.getRow(2); 
        	 String moneda = row2.getCell(1).getStringCellValue();
        	 
        	 String ISIN="";
        	 String mercado="";
        	 
        	 ps_maestro_valores.setString(1, valor);
        	 ps_maestro_valores.setString(2, moneda);
        	 ps_maestro_valores.setString(3, ISIN);
        	 ps_maestro_valores.setString(4, mercado);
        	 try {
        		 ps_maestro_valores.executeUpdate();
        	 } catch (java.sql.SQLIntegrityConstraintViolationException e) {
        		 String sql_update_maestro_valores = "UPDATE maestro_valores set valor = ?, moneda = ?, ISIN = ?, mercado = ? where valor = ?";
        		 PreparedStatement ps_update_maestro_valores = connection.prepareStatement(sql_update_maestro_valores);
        		 ps_update_maestro_valores.setString(1, valor);
        		 ps_update_maestro_valores.setString(2, moneda);
        		 ps_update_maestro_valores.setString(3, ISIN);
        		 ps_update_maestro_valores.setString(4, mercado);
        		 ps_update_maestro_valores.setString(5, valor);  //para el where
            	 ps_update_maestro_valores.executeUpdate();
        	 }
        	 connection.commit();
        	 
            
            // Datos     
        	 
/*
 * cotiza_dia        	 
 */
         	//delete primero 
         	String sql_delete_cotiza_dia = "DELETE FROM cotiza_dia WHERE valor = ? ";
         	PreparedStatement ps__delete_cotiza_dia = connection.prepareStatement(sql_delete_cotiza_dia);
         	ps__delete_cotiza_dia.setString(1, valor);
         	ps__delete_cotiza_dia.executeUpdate();

        	 
        String sql_insert_cotiza_dia = "INSERT INTO cotiza_dia (valor, fecha, dia, apertura, alza, baja, cerrar, volumen, cambio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps_cotiza_dia = connection.prepareStatement(sql_insert_cotiza_dia);
        	 
    	  int count = 0;
            for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
               
                //Si la fila es nulo o la primera celda es nulo leemos siguiente fila
                if (row == null || row.getCell(0) == null) continue;
                
                //Si la primera celda no es un número leemos siguiente fila 
                Cell cell0 = row.getCell(0);
                if (cell0 != null) {
                    switch (cell0.getCellTypeEnum()) {
                        case NUMERIC:
                            break;
                        default:
                        	continue;
                    }
                }
                
                
                
                try {                
	                Date fecha = new Date();
	                fecha 	= (Date) row.getCell(0).getDateCellValue();
	                double apertura = row.getCell(1).getNumericCellValue();
	                double alza 	= row.getCell(2).getNumericCellValue();
	                double baja 	= row.getCell(3).getNumericCellValue();
	                double cerrar 	= row.getCell(4).getNumericCellValue();
	                double volumen 	= row.getCell(5).getNumericCellValue();   
	                double cambio =0;
	                try {
	                	cambio 	= row.getCell(6).getNumericCellValue();   
	                }catch (Exception e) {
	                	
	                }
	
	                ps_cotiza_dia.setString(1, valor);
	                ps_cotiza_dia.setDate(2, new java.sql.Date(fecha.getTime()));
	                	String dia = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
	                ps_cotiza_dia.setString(3, dia);
	                ps_cotiza_dia.setDouble(4, apertura);
	                ps_cotiza_dia.setDouble(5, alza);
	                ps_cotiza_dia.setDouble(6, baja);
	                ps_cotiza_dia.setDouble(7, cerrar);
	                ps_cotiza_dia.setDouble(8, volumen);
	                ps_cotiza_dia.setDouble(9, cambio);
	                
	                if (batch) {
	                	ps_cotiza_dia.addBatch();
	                    count++;
	                    if (count % BATCH_SIZE == 0) {
	                    	ps_cotiza_dia.executeBatch();
	                    	connection.commit(); //Can't call commit when autocommit=true
	                    }
	                }else {             
		                try {
		                	ps_cotiza_dia.executeUpdate();
		                	
						} catch (java.sql.SQLIntegrityConstraintViolationException e) {
							//TODO Clave duplicada
							  String sql_update_cotiza_dia = "UPDATE cotiza_dia SET valor = ?, fecha = ?, dia = ?, apertura = ?, alza = ?, baja = ?, cerrar = ?, volumen = ?, cambio = ? where valor = ? and fecha = ?";
				        		 
				        		 PreparedStatement ps_update_cotiza_dia = connection.prepareStatement(sql_update_cotiza_dia);
				        		 ps_update_cotiza_dia.setString(1, valor);
				        		 ps_update_cotiza_dia.setDate(2, new java.sql.Date(fecha.getTime()));
				        		 String dia2 = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
				        		 ps_update_cotiza_dia.setString(3, dia2);
				        		 ps_update_cotiza_dia.setDouble(4, apertura);
				        		 ps_update_cotiza_dia.setDouble(5, alza);
				        		 ps_update_cotiza_dia.setDouble(6, baja);
				        		 ps_update_cotiza_dia.setDouble(7, cerrar);  
				        		 ps_update_cotiza_dia.setDouble(8, volumen);  
				        		 ps_update_cotiza_dia.setDouble(9, cambio);  
				        		 
				        		 ps_update_cotiza_dia.setString(10, valor); //  where
				        		 ps_update_cotiza_dia.setDate(11, new java.sql.Date(fecha.getTime())); //where
				        		 
				            	 ps_update_cotiza_dia.executeUpdate();
				            	 connection.commit();
							  
						}
	                }
				}catch(Exception filaError) {
					 errores.add("Fila " + (i + 1) + ": " + filaError.getMessage());
				}                
            }//fin for
            if (batch) {
            	try {
	            	ps_cotiza_dia.executeBatch();
	            	connection.commit(); //Can't call commit when autocommit=true
            	}catch(Exception filaError) {
            		errores.add("Fila última tanda:" + filaError.getMessage());					
				}
            }
         // Mostrar errores al final
            if (!errores.isEmpty()) {
                System.out.println("⚠ Filas con error:");
                errores.forEach(System.out::println);
            } else {
                System.out.println("✔ Importación completada sin errores");
            }            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}