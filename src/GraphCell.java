
/**
 * Prepravka pro jednu bunku s atributy cas a vyska hladiny
 * @author tomas
 *
 */
public class GraphCell {

	//vyska vodni hladiny
	private double waterSurfaceHeight;
	//cas
	private double time;
	
	//konstruktor
	public GraphCell(double waterSurfaceHeight, double time) {
		this.waterSurfaceHeight = waterSurfaceHeight;
		this.time = time;
	}

	// ziskani vodni hladiny
	public double getWaterSurfaceHeight() {
		return waterSurfaceHeight;
	}
	
	//nastaveni vodni hladiny
	public void setWaterSurfaceHeight(double waterSurfaceHeight) {
		this.waterSurfaceHeight = waterSurfaceHeight;
	}

	//ziskani casu
	public double getTime() {
		return time;
	}

	//nastaveni casu
	public void setTime(double time) {
		this.time = time;
	}
	
	
}


