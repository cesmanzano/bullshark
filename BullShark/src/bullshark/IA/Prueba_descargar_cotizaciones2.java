package bullshark.IA;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

	public class Prueba_descargar_cotizaciones2 {

	    public static void downloadEOD(String ticker, String apiKey, String filePath) throws IOException {

	        // 1. Crear carpeta si no existe
	        File file = new File(filePath);
	        File parentDir = file.getParentFile();

	        if (parentDir != null && !parentDir.exists()) {
	            if (parentDir.mkdirs()) {
	                System.out.println("Carpeta creada: " + parentDir.getAbsolutePath());
	            }
	        }

	        // 2. Crear archivo si no existe
	        if (!file.exists()) {
	            if (file.createNewFile()) {
	                System.out.println("Archivo creado: " + file.getAbsolutePath());
	            }
	        }

	        // 3. Construir URL de descarga
	        String url = "https://eodhistoricaldata.com/api/eod/" + ticker +
	                     "?api_token=" + apiKey + "&fmt=csv";

	        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
	        con.setRequestMethod("GET");

	        int status = con.getResponseCode();
	        if (status != 200) {
	            throw new IOException("Error HTTP " + status);
	        }

	        // 4. Descargar y escribir en el archivo
	        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	             BufferedWriter out = new BufferedWriter(new FileWriter(file))) {

	            String line;
	            while ((line = in.readLine()) != null) {
	                out.write(line);
	                out.newLine();
	            }
	        }

	        System.out.println("Descarga completada: " + filePath);
	    }

	    public static void main(String[] args) {
	        try {
	            downloadEOD("IBE.MC", "69c6af2fd2a4b9.40676154", "C:\\bullshark\\cotizaciones\\iberdrola.csv");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
