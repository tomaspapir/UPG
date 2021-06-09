import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import waterflowsim.Cell;
import waterflowsim.Simulator;

/**
 * Tato trida se stara o vykresleni barevne legendy pro teren
 * @author Tomas Papirnik
 *
 */
public class LegendPanel extends JPanel{

	/**
	 * nastaveni velikosti okna
	 */
	public LegendPanel() {
		this.setPreferredSize(new Dimension(100, 480));
	}
	
	// maximalni vyska terenu pro dany scenar
	int terrainLevelMax = 0;
	
	// minimalni vyska terenu pro dany scenar
	int terrainLevelMin = 255;
	
	//rozsah vysky
	int range = 0;
	
	// pole vsech bunek
	Cell[] cells = Simulator.getData();
	
	
	/**
	 * predvypocet nejvyssiho a nejnizsiho bodu pro dany scenar a urcena velikost jedne casti rozsahu
	 */
	public void calculateLegend() {
		for(int i = 0; i<cells.length; i++) {
				int terrainLevel = (int)Simulator.getData()[i].getTerrainLevel();
     		      		
				if (terrainLevel > terrainLevelMax) {
					terrainLevelMax = terrainLevel;
				}
				if (terrainLevel < terrainLevelMin) {
					terrainLevelMin = terrainLevel;
				}  		        	
			
		}
	 range = (terrainLevelMax - terrainLevelMin) / 8;
	}
	
	/**
	 * vykresleni nekolika obdelniku, ktere pouze slouzi pro informaci o vysce (legenda)
	 * @param g graficky kontext
	 */
	public void drawLegend(Graphics2D g) {	

	 calculateLegend();
	 DrawingPanel model = new DrawingPanel();

	
		Graphics2D g2 = (Graphics2D)g;
		int spacing = (int) model.nimW/9;
		//System.out.println(width);
		g2.setFont(new Font("Arial", Font.PLAIN, 20));
				
		g2.setColor(Color.decode("#CD853F"));
		g2.fillRect(0, 0, 100, 30);
		
		g2.setColor(Color.decode("#DEB887"));
		g2.fillRect(0, 30, 100, 60);
		
		g2.setColor(Color.decode("#D2B48C"));
		g2.fillRect(0, 60, 100, 90);
		
		g2.setColor(Color.decode("#F5DEB3"));
		g2.fillRect(0, 90, 100, 120);
		
		g2.setColor(Color.decode("#FFEBCD"));
		g2.fillRect(0, 120, 100, 150);
		
		g2.setColor(Color.decode("#a8c173"));
		g2.fillRect(0, 150, 100, 180);
		
		g2.setColor(Color.decode("#b3c985"));
		g2.fillRect(0, 180, 100, 210);
		
		g2.setColor(Color.decode("#d4e1bb"));
		g2.fillRect(0, 210, 100, 240);
		
		g2.setColor(Color.decode("#5abcd8"));
		g2.fillRect(0, 240, 100, 270);
		
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 270, 100, 10000);

		g2.setColor(Color.BLACK);
		g2.drawString(">" + (terrainLevelMax - range), 0, 20);
		g2.drawString(">" + (terrainLevelMax - 2*range), 0, 50);
		g2.drawString(">" + (terrainLevelMax - 3*range), 0, 80);
		g2.drawString(">" + (terrainLevelMax - 4*range), 0, 110);
		g2.drawString(">" + (terrainLevelMax - 5*range), 0, 140);
		g2.drawString(">" + (terrainLevelMax - 6*range), 0, 170);
		g2.drawString(">" + (terrainLevelMax - 7*range), 0, 200);
		g2.drawString("<=" + (terrainLevelMax - 7*range), 0, 230);
		g2.drawString("Water", 0, 260);

	}

	/**
	 * metoda se stara o vykreslovani do DrawingPanelu a 
	 * vola jednotlive metody na vykresleni
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 10000, 10000);
		drawLegend(g2);
		repaint();
	}
	
}
