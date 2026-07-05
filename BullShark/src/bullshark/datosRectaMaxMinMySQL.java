package bullshark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class datosRectaMaxMinMySQL {

	/**
	 * Esta clase extrae datos desde una SQL y calcula los dos mínimos y los dos
	 * máximos, junto con sus fechas.
	 */

	static final String URL = "jdbc:mysql://localhost:3306/bullshark?rewriteBatchedStatements=true";
	static final String USER = "root";
	static final String PASS = "bolsa";
	static Scanner sc = new Scanner(System.in);

	private static String ficheroCotizacionesMinimas = "C:\\Fernancio\\ProyectoBolsa\\datosCotizacionesMinimas.txt";
	private static String ficheroCotizacionesMaximas = "C:\\Fernancio\\ProyectoBolsa\\datosCotizacionesMaximas.txt";

	public static void main(String [] arg) throws SQLException, IOException {

		String valor = "";
		String anteriorValor = "";
		double cotizacionAlza = 0;
		double cotizacionBaja = 0;
		String fechaCotizacion = "";	//Es la fecha recuperada de la base de datos. 
		String fechaEvaluacion = "";	//Es la fecha sobre la que vamos a evaluar la cotizacion. 
		double cotizacionEvaluacion = 0;//Es la cotización que vamos a evaluar. 
		String maximosMinimos ="";		//Para calcular o los máximos o los mínimos. 

	//Declaramos un valor inicial muy grande
		int primeraMinimaCotizacion = Integer.MAX_VALUE;  
		int segundaMinimaCotizacion = Integer.MAX_VALUE;
		int primeraFechaMinimaCotizacion = 0;
		int segundaFechaMinimaCotizacion = 0;

	//Declaramos un valor inicial muy pequeño       
		int primeraMaximaCotizacion = Integer.MIN_VALUE;  
		int segundaMaximaCotizacion = Integer.MIN_VALUE;
		int primeraFechaMaximaCotizacion = 0;
		int segundaFechaMaximaCotizacion = 0;

		try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

			//Recuperamos los dos valores más pequeños mediante un SQL. 
			 ResultSet rs = con.createStatement().executeQuery("SELECT alza FROM cotiza ORDER BY alza ASC LIMIT 2");

//				valor = rs.getString("valor");
//			    cotizacionAlza = rs.getDouble("alza");
//				cotizacionBaja = rs.getDouble("baja");

				//CADA VEZ QUE CAMBIE DE VALOR, HAY QUE ESCRIbiR FICHEROS. 
				
				 if (anteriorValor == null){
					 anteriorValor = valor;}
					 
				//Al cambiar el Valor, guardamos datos en ficheros.
				 if (!valor.equals(anteriorValor)) {
					String sql = "INSERT INTO ficheroCotizacionesMinimas (anteriorValor, primeraMinimaCotizacion,primeraFechaMinimaCotizacionsegundaMinimaCotizacion,segundaFechaMinimaCotizacion) "
							+ "VALUES (anteriorValor, primeraMinimaCotizacion, primeraFechaMinimaCotizacion, segundaMinimaCotizacion, segundaFechaMinimaCotizacion)";
					
					PreparedStatement objetoParaEnviarParametrosASQL = con.prepareStatement(sql);

					File archivo = new File("ruta/del/fichero.pdf");
				    FileInputStream instanciaAlFichero = new FileInputStream(archivo);

					objetoParaEnviarParametrosASQL.setString(1, archivo.getName());
					objetoParaEnviarParametrosASQL.setBinaryStream(2, instanciaAlFichero, (int) archivo.length());

					objetoParaEnviarParametrosASQL.executeUpdate();
					instanciaAlFichero.close();

				//Repetir lo mismo para el fichero de máximos. 
					
					System.out.println("Fichero guardado correctamente");
					anteriorValor = valor;

					
				//Mínimos
					String datosCotizacionesMinimas = valor + ";" + primeraMinimaCotizacion + ";" + primeraFechaMinimaCotizacion + ";" + segundaMinimaCotizacion + ";" + segundaFechaMinimaCotizacion;
					EscriboFicheroCotizacionesMinimas(ficheroCotizacionesMinimas, datosCotizacionesMinimas, false);
				//Máximos       
					String datosCotizacionesMaximas = valor + ";" + primeraMaximaCotizacion + ";" + primeraFechaMaximaCotizacion + ";" + segundaMaximaCotizacion + ";" + segundaFechaMaximaCotizacion;
					EscriboFicheroCotizacionesMaximas(ficheroCotizacionesMaximas, datosCotizacionesMaximas, false);
				}
				
			}
		

	// Llamamos a método que evalua la cotización a una fecha. Si este valor está por encima de la línea de tendencia, devuelve un 1. Si no, devuelve un -1.
		fechaEvaluacion = "20260107";
		cotizacionEvaluacion = 6;
		maximosMinimos = "maximos";
//        int alzaBaja = evaluadorCotizacion.evaluarCotizacion(valor, fechaEvaluacion, cotizacionEvaluacion, maximosMinimos);

//		if (alzaBaja == 1) {
//			System.out.println("Valor al alza.");
//		}
//		if (alzaBaja == -1) {
//			System.out.println("Valor a la baja");  
//		}
	}

//	Metodo: recibe un valor, lanza sql para recuperar 2 máximos y 2 mínimos y genera datos en las tablas de máximos y mínimos



// GRABAR COTIZACIONES MINIMAS
	// (no sé si sería bueno generar tb una copia de este fichero)
	private static boolean EscriboFicheroCotizacionesMinimas(String fichero, String texto, boolean add){
		OutputStreamWriter outputStreamWriter = null;
		try{
			File file=new File(fichero);
			File directorio=new File(file.getParent());
			if(!directorio.exists()){
				directorio.mkdirs();
			}
			outputStreamWriter = new OutputStreamWriter( new FileOutputStream(fichero,add));
			try (BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
				bufferedWriter.write(texto+"\r\n");
				bufferedWriter.flush();
			}
		}catch(FileNotFoundException e){
			return false;
		}catch(IOException e) {
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

//GRABAR COTIZACIONES MAXIMAS
	private static boolean EscriboFicheroCotizacionesMaximas(String fichero, String texto, boolean add){
		OutputStreamWriter outputStreamWriter = null;
		try{
			File file=new File(fichero);
			File directorio=new File(file.getParent());
			if(!directorio.exists()){
				directorio.mkdirs();
			}
			outputStreamWriter = new OutputStreamWriter (new FileOutputStream(fichero,add));
			try (BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
				bufferedWriter.write(texto+"\r\n");
				bufferedWriter.flush();
			}
		}catch(FileNotFoundException e){
			return false;
		}catch(IOException e){
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

	// EVALUAR COTIZACION
//	public static int evaluarCotizacion(int fecha, int cotizacion){
//		//private static String ficheroCotizacionesMinimas="C:\\Fernancio\\ton\\datosCotizacionesMinimas.txt";
//		int primeraMinimaCotizacion = 0;  
//		int segundaMinimaCotizacion = 0;
//		int primeraFechaMinimaCotizacion = 0;
//		int segundaFechaMinimaCotizacion = 0;
//		FileReader fr = null;
//		BufferedReader br = null;
//		try{
//			fr = new FileReader (ficheroCotizacionesMinimas);
//			br = new BufferedReader(fr);
//			//Lectura del fichero
//			String registro;          
//			while((registro = br.readLine())!=null){
//				//Dividir campos por el ";"
//				String[] campos = registro.split(";");
//				//Tomamos la cotizacion del campo2 -> campos[2]
//				primeraMinimaCotizacion= Integer.parseInt(campos[0]);
//				primeraFechaMinimaCotizacion = Integer.parseInt(campos[1]);
//				segundaMinimaCotizacion  = Integer.parseInt(campos[2]);
//				segundaFechaMinimaCotizacion = Integer.parseInt(campos[3]);
//			}
//		}catch (Exception e) {
//		}finally{
//			//En el finally cerramos el fichero, para asegurarnos de que se cierra tanto si todo va bien como si salta una excepcion.
//			try{
//				if( null != fr ){  
//					fr.close();
//				}
//			}catch (Exception e2){
//				e2.printStackTrace();
//			}
//		}
//
//		//Calculamos recta de tendencia.
//		// x = fechas
//		// y = cotizaciones
//		// y=(y2-y1)/(x2-x1)*(x-x1)+y1
//
//		int pendiente = 0;
//		int cotizacionExtrapolada = 0;
//
//		if((segundaMinimaCotizacion - primeraMinimaCotizacion) !=0) {
//			pendiente  = (segundaFechaMinimaCotizacion - primeraFechaMinimaCotizacion) / (segundaMinimaCotizacion - primeraMinimaCotizacion);      
//		}
//		cotizacionExtrapolada = pendiente * (fecha - primeraFechaMinimaCotizacion) + primeraMinimaCotizacion;
//
//		//Comparamos la cotización recibida con la calculada mediante la recta de tendencia.
//		if(cotizacion >= cotizacionExtrapolada) {
//			return 1; //Al alza.
//		}else {
//			return -1;//A la baja.
//		}
//	}
}
