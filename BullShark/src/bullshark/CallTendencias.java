package bullshark;

public class CallTendencias {

	/**
	 * Esta clase llamará a la clase calculoTendencias:   
	 * 	-  Manda:   Valor / Fecha de Evaluacion / Cotizacion de Evaluacion
	 *  -  Recibe:  alzaBajaDiario, alzaBajaSemanal, alzaBajaMensual 
	 */
	public static void main(String[] args) throws Exception{
//	public static void main() throws SQLException, IOException {

		String valor ="Amazon";
		int    fechaEvaluacion = 20260301;  
		int    cotizacionEvaluacion = 5;
		String alzaBajaDiario ="";
		String alzaBajaSemanal="";
		String alzaBajaMensual="";
		
		CalculoTendencias.calculo(valor, fechaEvaluacion, cotizacionEvaluacion,	alzaBajaDiario, alzaBajaSemanal, alzaBajaMensual);
		
		System.out.println("valor: " + valor);
//		System.out.println("fechaEvaluacion: " + fechaEvaluacion);
//		System.out.println("cotizacionEvaluacion: " + cotizacionEvaluacion);
//		System.out.println("alzaBajaDiario: " + alzaBajaDiario);
//		System.out.println("alzaBajaSemanal: " + alzaBajaSemanal);
//		System.out.println("alzaBajaMensual: " + alzaBajaMensual);
	}
}
		
	
