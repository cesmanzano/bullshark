package bullshark.beans;

public class Cotizacion {
    public String fecha;
    public double cierre;

    public Cotizacion(String fecha, double cierre) {
        this.fecha = fecha;
        this.cierre = cierre;
    }
}