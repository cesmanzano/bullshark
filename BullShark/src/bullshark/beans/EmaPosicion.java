package bullshark.beans;

public class EmaPosicion {
    private double ema;
    private int posicion;

    public EmaPosicion(double ema, int posicion) {
        this.ema = ema;
        this.posicion = posicion;
    }

	public double getEma() {
		return ema;
	}

	public void setEma(double ema) {
		this.ema = ema;
	}

	public int getPosicion() {
		return posicion;
	}

	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
    
    
}