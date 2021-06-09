import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import waterflowsim.Cell;
import waterflowsim.Simulator;
import waterflowsim.Vector2D;

/**
 * Tato trida se stara o vykresleni toku, krajiny, nazvu toku a sipek
 * popisujicich jeho smer
 * @author Tomas Papirnik
 *
 */
public class DrawingPanel extends JPanel implements Printable{

	// dimensionVector - pocet bunek na sirku, pocet bunek na vysku
	Vector2D<Integer> dimensionVector = Simulator.getDimension();
	// deltaVector - velikost jedne bunky v metrech
	Vector2D<Double> deltaVector = Simulator.getDelta();
	//pole zdroju
	waterflowsim.WaterSourceUpdater[] zdroje = Simulator.getWaterSources();
	//arraylist arraylistu uchovavajicich prepravky GraphCell
	ArrayList<ArrayList<GraphCell>> graphCells = new ArrayList<ArrayList<GraphCell>>();
	//arraylist uchovavajici prepravky GraphCell
	ArrayList<GraphCell> innerAL = null;
	//do teto promenne se uklada vypocteny index bunky po kliknuti
	int cellIndex = 0;
	//pomocne promenne na souradnice na pocitani indexu
	int calculatedGraphX = 0;
	//pomocne promenne na souradnice na pocitani indexu
	int calculatedGraphY = 0;
	//pomocne souradnice pocatku grafu
	int graphSelectionXStart = 0;
	//pomocne souradnice pocatku grafu
	int graphSelectionYStart = 0;
	//pomocne souradnice konce grafu
	int graphSelectionXEnd = 0;
	//pomocne souradnice konce grafu
	int graphSelectionYEnd = 0;
	
	//pocatecni souradnice x pro vyber grafu prepoctena zpet na souradnice mapy
	int graphSelectionXStartConverted = 0;
	//pocatecni souradnice y pro vyber grafu prepoctena zpet na souradnice mapy
	int graphSelectionYStartConverted = 0;
	//koncove souradnice y pro vyber grafu prepoctena zpet na souradnice mapy
	int graphSelectionXEndConverted = 0;
	//koncove souradnice y pro vyber grafu prepoctena zpet na souradnice mapy
	int graphSelectionYEndConverted = 0;
	
	//arraylist, ktery se naplni vybranymi bunkami na graf
	ArrayList<Integer> selectedCells = new ArrayList<Integer>();

	//tlacitko na pridani rychlosti
	JButton faster;
	//tlacitko na ubrani rychlosti
    JButton slower;
    //tlacitko na pauznuti
    JButton pause;
    //tlacitko na ukazani grafu
    JButton showGraph;
    JButton saveFile;
    //textfield ktery ukazuje velikost nextstepu
    JTextField nextStepTF;
    //tlacitko pouzivane ve grafovych oknech na tisk
	JButton printGraph;
	//chartpanel slouzici k tvorbe grafu
	ChartPanel chartPanel;

	//promenna ktera zajistuje spravne vykreslovani vyberu bunek na tvorbu grafu
	boolean isSelectingGraph = false;
	//promenna zajistuje spravne fungovani tlacitka pause
	private static double ns = 0;
	//promenna zajistuje spravny chod redukce dat
	int nextStepCounter = 5;

	
	
	//bunky na sirku
	int width = (int) (dimensionVector.x);
	//bunky na vysku
	int height = (int) (dimensionVector.y);
	
	// pole vsech bunek
	Cell[] cells = Simulator.getData();
	
	//priprava promenne water
    private BufferedImage water = null;
    
    //priprava promenne terrain
    private BufferedImage terrain = null;

    //priprava promenne label
    private BufferedImage label = null;
    
    //pomer sirky okna ku sirce obrazu
	double scaleX = 0.0;
	//pomer vysky okna ku sirce obrazu
	double scaleY = 0.0;
	//pomer okna ku obrazu
	double scale = 0.0;
	
	//sirka obrazu * pomer
	int nimW = 0;
	//vyska obrazu * pomer
	int nimH = 0;
	//nova, vypoctena souradnice x
	int startX = 0;
	
	//ziskani souradnice x
	public int getStartX() {
		return startX;
	}	

	//nova, vypoctena souradnice y
	int startY = 0;
	
	//ziskani souradnice y
	public int getStartY() {
		return startY;
	}
	
	// minimalni vyska terenu pro dany scenar
	int terrainLevelMin = 255;
	// maximalne vyska terenu pro dany scenar
	int terrainLevelMax = 0;
	
	//rozsah mezi max a min
	int range = 0;

	//rychlost kroku simulatoru
	public double nextStep = 0.05;	
	
	//ubehnuty cas (pro graf)
	public double elapsedTime = 0;
	
	//nova instance timeru
	Timer timer = new Timer();
	
	// ziskani absolutni hodnoty deltaX
	public double getDeltaXAbs() {
		return Math.abs(deltaVector.x);
	}
	
	//ziskani absolutni hodnoty deltaY
	public double getDeltaYAbs() {
		return Math.abs(deltaVector.y);
	}
	
	//ziskani rychlosti kroku
	public double getNextStep() {
		return nextStep;
	}

	//nastaveni rychlosti kroku a jeho zaokrouhleni na dve desetinna mista
	public void setNextStep(double nextStep) {
		if (nextStep >= 0 && nextStep < 1) {
			this.nextStep = Math.round(nextStep * 100.0) / 100.0;
		}		
	}


	/**
	 * konstruktor ve kterem se nastavuje velikost okna, listenery na vytvoreni grafu
	 */
	public DrawingPanel() {
		this.setPreferredSize(new Dimension(640, 480));
				
		this.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent evt) {
        		       		
        		cellIndex = calculateCellIndex(evt.getX(), evt.getY());
        		Vector2D calculatedCoords = calculateNewCoords(evt.getX(), evt.getY());
        		if ((int)calculatedCoords.x <= width && (int)calculatedCoords.y <= height && (int)calculatedCoords.x >= 0 && (int)calculatedCoords.y >= 0) {
        			createGraph();
        			printGraphData();
    				isSelectingGraph = false;

        		}
        		        		
        	}
		});
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {

				graphSelectionXStart = evt.getX();
				graphSelectionYStart = evt.getY();
				
				isSelectingGraph = true;
				int cellIndexStart = calculateCellIndex(evt.getX(), evt.getY());
				
				System.out.println(cellIndexStart);
				
				Vector2D coordsStart = (calculateNewCoords(evt.getX(), evt.getY()));
				
				graphSelectionXStartConverted = (int) coordsStart.x;
				graphSelectionYStartConverted = (int) coordsStart.y;
								
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				selectedCells.clear();

				graphSelectionXEnd = e.getX();
				graphSelectionYEnd = e.getY();
				
				int cellIndexEnd = calculateCellIndex(e.getX(), e.getY());
				System.out.println(cellIndexEnd);
				
				Vector2D coordsEnd = (calculateNewCoords(e.getX(), e.getY()));
				
				graphSelectionXEndConverted = (int) coordsEnd.x;
				graphSelectionYEndConverted = (int) coordsEnd.y;
				
				if (graphSelectionXStartConverted <= width && graphSelectionYStartConverted <= height && graphSelectionXStartConverted >= 0 &&
					graphSelectionYStartConverted >= 0 && graphSelectionXEndConverted <= width && graphSelectionYEndConverted <= height && 
					graphSelectionXEndConverted >= 0 && graphSelectionYEndConverted >= 0) {
					
					calculateSelectedCells();
        		}
				
			}
		});
	}
	
	
	/**
	 * vypocte ze souradnic pozici bunky
	 * @param originalX originalni souradnice eventu x
	 * @param originalY originalni souradnice eventu y
	 * @return index bunky na kterou uzivatel klikl
	 */
	public int calculateCellIndex(int originalX, int originalY) {
		
		double newX = (originalX - (getStartX() * getDeltaXAbs()));
		double newY = (originalY - (getStartY() * getDeltaYAbs()));
		
		double widthRatio = width / (nimW * getDeltaXAbs());
		double heightRatio = height / (nimH * getDeltaYAbs());
		
		calculatedGraphX = (int) (newX * widthRatio);
		calculatedGraphY = (int) (newY * heightRatio);
		
		int index = (calculatedGraphY * width) + calculatedGraphX;
		
		return index;
	}
	
	/**
	 * vyber bunek podle vyberu mysi a nasledne ulozeni do arraylistu
	 */
	public void calculateSelectedCells() {
				
		for (int y = graphSelectionYStartConverted; y < graphSelectionYEndConverted; y++) {
			
			for (int x = graphSelectionXStartConverted; x < graphSelectionXEndConverted; x++) {
				
				int index = (y * dimensionVector.x) + x;
				
				if (index >= 0 && index <= (dimensionVector.x * dimensionVector.y)) {
				
						selectedCells.add(index);
				}
			}
		}
			
	}
	
	
	/**
	 * prevadeni souradnic kliknuti na souradnice uvnitr obrazku
	 * @param originalX puvodni souradnice eventu x
	 * @param originalY puvodni souradnice eventu y
	 * @return
	 */
	public Vector2D calculateNewCoords(int originalX, int originalY) {
		
		double newX = (originalX - (getStartX() * getDeltaXAbs()));
		double newY = (originalY - (getStartY() * getDeltaYAbs()));
		
		double widthRatio = width / (nimW * getDeltaXAbs());
		double heightRatio = height / (nimH * getDeltaYAbs());
		
		calculatedGraphX = (int) (newX * widthRatio);
		calculatedGraphY = (int) (newY * heightRatio);
		
		Vector2D newCoords = new Vector2D(calculatedGraphX, calculatedGraphY);
		
		return newCoords;
	}
	
	/**
	 * vytvoreni grafickeho vyberu bunek ktere se maji zobrazit ve grafu
	 * @param g2 graficky kontext
	 * @param startX pocatecni souradnice x
	 * @param startY pocatecni souradnice y
	 * @param endX konecna souradnice x
	 * @param endY konecna souradnice y
	 * @param isSelecting kresli se pouze pokud je tento boolean true
	 */
	public void createSelectionTool(Graphics2D g2, int startX, int startY, int endX, int endY, boolean isSelecting) {
		
		if (isSelecting) {
			
			int width = (int)((endX - startX) / getDeltaXAbs());
			int height = (int)((endY - startY) / getDeltaYAbs());
			
			
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.BLACK);
			
			g2.drawRect((int) (startX/getDeltaXAbs()),(int) (startY/getDeltaYAbs()), width, height);

		}
		
	}
	
	/**
	 * vytvoreni noveho grafu, ktery ukazuje udaje podle toho, kam uzivatel klikl
	 */
	public void createGraph() {
		JFrame graph = new JFrame("Graph");
        
        final XYSeries waterSurface = new XYSeries("Water surface");
        
		
		for (int i = 0; i < graphCells.get(cellIndex).size(); i++) {
			waterSurface.add(graphCells.get(cellIndex).get(i).getTime(), graphCells.get(cellIndex).get(i).getWaterSurfaceHeight());
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(waterSurface);
        
        JFreeChart waterLvlChart = ChartFactory.createXYLineChart(
        	"Water level chart",
        	"Time [s]",
        	"Water surface height [m]",
        	dataset);
        chartPanel = new ChartPanel(
                waterLvlChart
        );
        
        JPanel buttons = new JPanel();
        printGraph = new JButton("Print");
        
        buttons.add(printGraph);
        
        graph.add(buttons, BorderLayout.SOUTH);
        graph.add(chartPanel, BorderLayout.CENTER);
        graph.pack();              
        graph.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graph.setMinimumSize(new Dimension(320, 240));
        graph.setSize(640, 480);
        graph.setLocationRelativeTo(null);
	    graph.setVisible(true);	    
	 
	}
	
	/**
	 * vytvoreni grafu podle vyberu bunek	
	 */
	public void createSelectionGraph() {
        	
        ArrayList<GraphCell> avgCells = new ArrayList<GraphCell>();
        
        for (int i = 0; i < graphCells.get(0).size(); i++) {
        	
        	double addedWaterLevel = 0;
        	
			int cellCount = 0;

        	for (int j = 0; j < selectedCells.size(); j++) {
    			double waterLevel = graphCells.get(selectedCells.get(j)).get(i).getWaterSurfaceHeight();

        		if (waterLevel > 0) {
        			addedWaterLevel += waterLevel;
        			cellCount++;
        		}
        	}
        	
        	if (addedWaterLevel/cellCount > 0) {
        		avgCells.add(new GraphCell(addedWaterLevel / cellCount, graphCells.get(0).get(i).getTime()));
      
        	} else avgCells.add(new GraphCell(0, graphCells.get(0).get(i).getTime()));

        }
        
		JFrame avggraph = new JFrame("Average Graph");

		
        final XYSeries avgWaterSurface = new XYSeries("Average water surface");
        
    	for (int i = 0; i < avgCells.size(); i++) {
    		avgWaterSurface.add(avgCells.get(i).getTime(), avgCells.get(i).getWaterSurfaceHeight());
		}

		
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(avgWaterSurface);
        
        JFreeChart avgWaterLvlChart = ChartFactory.createXYLineChart(
        	"Average water level chart",
        	"Time [s]",
        	"Water surface height [m]",
        	dataset);
        chartPanel = new ChartPanel(
        		avgWaterLvlChart
        );
        
        JPanel buttons = new JPanel();
        printGraph = new JButton("Print");
        
        buttons.add(printGraph);
        
        avggraph.add(buttons, BorderLayout.SOUTH);
        avggraph.add(chartPanel, BorderLayout.CENTER);
        avggraph.pack();              
        avggraph.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        avggraph.setMinimumSize(new Dimension(320, 240));
        avggraph.setSize(640, 480);
        avggraph.setLocationRelativeTo(null);
        avggraph.setVisible(true);	    
	 
	}
	
	/**
	 * Vytvoreni tlacitek na ovladani
	 */
	public JPanel getButtons() {
		 faster = new JButton("Faster");
	     slower = new JButton("Slower");
	     pause = new JButton("Pause");
	     showGraph = new JButton("Show selection graph");
	     saveFile = new JButton("Save map");

	     JLabel speed = new JLabel("Speed: ");
	     nextStepTF = new JTextField("" + getNextStep());
	     nextStepTF.setEditable(false);
	     
	     buttonController();
	     
	     JPanel buttons = new JPanel();
	     buttons.add(speed);
	     buttons.add(nextStepTF);
	     buttons.add(faster);
	     buttons.add(slower);
	     buttons.add(pause);
	     buttons.add(showGraph);
	     buttons.add(saveFile);
	     
	     return buttons;
	}
	
	/**
	 * Vytvoreni listeneru na tlacitka
	 */
	public void buttonController() {
		
		pause.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (pause.getText() == "Pause") {
					ns = getNextStep();
					setNextStep(0);
					nextStepTF.setText("Paused");
					pause.setText("Resume");	
				} else if (pause.getText() == "Resume") {
					pause.setText("Pause");
					setNextStep(ns);
					nextStepTF.setText("" + ns);
				}
			}
		});
		
		faster.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pause.getText() == "Pause") {
				setNextStep(getNextStep() + 0.01);
				nextStepTF.setText("" + getNextStep());
				}
			}
		});
        
        slower.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pause.getText() == "Pause") {
				setNextStep(getNextStep() - 0.01);
				nextStepTF.setText("" + getNextStep());
				}
				
			}
		});
        
        showGraph.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				isSelectingGraph = false;
				
				createSelectionGraph();
				printGraphData();
			}
		});
        
        saveFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveImage("Map.jpg", 640, 480);
				} catch (IOException g) {
					g.printStackTrace();
				}
				
			}
		});
        

	}
	
	 public void saveImage(String path, int W, int H) throws IOException {
		 
	       BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_3BYTE_BGR);

	       drawBackground(img.createGraphics());
		   drawWaterFlowState(img.createGraphics());
		   drawSourcesDrawLabels(img.createGraphics());

	       ImageIO.write(img, "jpeg", new File(path));
	    }	
	/**
	 * Tato metoda zajistuje listener pro vytisteni grafu
	 */
	void printGraphData() {
		printGraph.addActionListener(new ActionListener() {			

            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob job = PrinterJob.getPrinterJob();
                if (job.printDialog())
                {
                    job.setPrintable(chartPanel);
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
				
	}
	
	/**
	 * vytvoreni dat, ktera se nasledne budou vyzobrazovat  ve grafech
	 */
	void prepareGraphData() {
		
		if ((nextStepCounter % 5) == 0) {
			if (graphCells.isEmpty()) {
				for (int i = 0; i < cells.length; i++) {
					innerAL = new ArrayList<GraphCell>();
					graphCells.add(innerAL);
				}
			}
		
			for (int j = 0; j < graphCells.size(); j++) {
				GraphCell cell = new GraphCell(cells[j].getWaterLevel(), elapsedTime);
				graphCells.get(j).add(cell);
			}
		 nextStepCounter++;
		}
		else {
			nextStepCounter++;
		}
	}	
	
	/**
	 * zavolani metod na vykresleni vody a na vykresleni terenu
	 * @param g graficky kontext
	 */	
	public void drawWaterFlowState(Graphics2D g) {
		drawWaterLayer(g);
		drawTerrain(g);
	}
	
	/**
	 * vytvori se novy BufferedImage a pole rgbArray, pokud je bunka na pozici i mokra
	 * tak se nastavi prvek v poli na pozici i na modrou, pote se pole pixelu nastavi do promenne 
	 * image a tridni promenna (this.water) se nastavi na image; 
	 * this.water se vykresli
	 */
	public void drawWaterLayer(Graphics2D g) {
			Graphics2D g2 = (Graphics2D)g;
	            
	        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

	        int[] rgbArray = new int[height*width];

	        for(int i = 0; i<cells.length; i++) {
	        	if(!cells[i].isDry()) {
	        		rgbArray[i] = 0xFF5abcd8;
	        	}	        	
	        }
	              
	        image.setRGB(0,  0, width, height, rgbArray, 0, width);
	        this.water = image;
	        
	        g2.drawImage(this.water, startX, startY, nimW, nimH, null);
	        
	        
	}
	
	/**
	 * vytvori se novy BufferedImage a pole rgbArray, pokud je bunka na pozici i sucha
	 * tak se nastavi prvek v poli na pozici i na barvu, podle jeho vysky, tridni promenna
	 * terrain se nastavi na image a vykresli se
	 * dale se tu taky pocita max, min vyska terenu a jejich rozsah
	 * @param g Graficky kontext
	 */
	public void drawTerrain(Graphics2D g) {
		Graphics2D g2 = (Graphics2D)g;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        int[] rgbArray = new int[height*width];

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

        for(int i = 0; i<cells.length; i++) {
        	if(cells[i].isDry()) {
        		int terrainLevel = (int)Simulator.getData()[i].getTerrainLevel();
    
        		if (terrainLevel > (terrainLevelMax - range)) {
        	        
        			rgbArray[i] = 0xFFCD853F;
        		}        		
        		else if (terrainLevel > (terrainLevelMax - 2*range)) {

        			rgbArray[i] = 0xFFDEB887;

        		}        		
        		else if (terrainLevel > (terrainLevelMax - 3*range)) {

        			rgbArray[i] = 0xFFD2B48C;
        			
        		}
        		else if (terrainLevel > (terrainLevelMax - 4*range)) {

        			rgbArray[i] = 0xFFF5DEB3;

        		}
        		else if (terrainLevel > (terrainLevelMax - 5*range)) {

        			rgbArray[i] = 0xFFFFEBCD;

        		}
        		else if (terrainLevel > (terrainLevelMax - 6*range)) {

        			rgbArray[i] = 0xFFa8c173;

        		}
        		else if (terrainLevel > (terrainLevelMax - 7*range)) {

        			rgbArray[i] = 0xFFb3c985;

        		}        		
        		else if (terrainLevel <= (terrainLevelMax - 7*range)){

        			rgbArray[i] = 0xFFd4e1bb;
        		}
        	}
        }
              
        image.setRGB(0,  0, width, height, rgbArray, 0, width);
        this.terrain = image;
        
        g2.drawImage(this.terrain, startX, startY, nimW, nimH, null);
	}
	
	
	/**
	 * vypocet scalovani obrazku vuci velikosti okna (ze cviceni)
	 * @param width sirka okna
	 * @param height vyska okna
	 */
	void computeModel2WindowTransformation(int width, int height) {
		
		scaleX = ((double)width) / this.width;
		scaleY = ((double)height) / this.height;
		scale = Math.min(scaleX, scaleY);
		
		nimW = (int)(this.width*scale);
		nimH = (int)(this.height*scale);
		
		startX = (width - nimW) / 2;
		startY = (height - nimH) / 2;		
		
	}
	
	
	/**
	 * priprava k vykresleni labelu a zdroju
	 * @param g graficky kontext
	 */
	void prepareWaterSources(Graphics2D g) {
		Graphics2D g2 = (Graphics2D)g;
		BufferedImage l= new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);	
		g2 = l.createGraphics();
		
	
		
		for(int i = 0; i<zdroje.length; i++){
			    int cell = zdroje[i].getIndex();
			
	            int x = width;
	            int y = 0;
	            int tempCell = cell;


	            while(tempCell>x) {
	                tempCell = tempCell - x;
	                y++;
	            }

	            if (tempCell == x) {
	                x = 0;
	                y++;
	            }
	            else {
	                x = tempCell;
	            }
	            
			    Vector2D<Double> grad = Simulator.getGradient(cell);
           
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);	            
	            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
	            		RenderingHints.VALUE_STROKE_PURE);	            
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	            		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	            
	            g2.setStroke(new BasicStroke(2));
	            g2.setColor(Color.black);
	            
	            double vectorStartX = x;
	            double vectorStartY = y;
	            double vectorEndX = x + (grad.x * -1);
	            double vectorEndY = y + (grad.y * -1);

	            double sizeC = (vectorEndY - vectorStartY) / (vectorEndX - vectorStartX);
	            double beta = Math.atan(sizeC);
	            
	            AffineTransform old = g2.getTransform();
	        	g2.rotate(beta,x, y);
	        	
	        	if ((vectorEndX - vectorStartX) < 0 && (!zdroje[i].getName().equals("Levy dolni")) && (!zdroje[i].getName().equals("Levy horni"))) {
	        		 drawArrow(g2, x - 10, y - 25.0 , x - 40, y - 25.0, 5.0, 10.0);
	 	        	 g2.drawString(zdroje[i].getName(), x - 50, y - 35);
	        	}
	        	else if (zdroje[i].getName().equals("Pravy dolni") || zdroje[i].getName().equals("Pravy horni")){
	        		drawArrow(g2, x - 90, y, x - 30, y, 5.0, 10.0);
		        	g2.drawString(zdroje[i].getName(), x - 90, y - 10);

	        	}
	        	else if (zdroje[i].getName().equals("Levy dolni") || zdroje[i].getName().equals("Levy horni")){
	        		drawArrow(g2, x + 90, y, x + 30, y, 5.0, 10.0);
		        	g2.drawString(zdroje[i].getName(), x + 40, y - 10);
	        	}
	        	else {
	        		drawArrow(g2, x + 10, y - 25.0 , x + 40.0, y - 25.0, 5.0, 10.0);
		        	g2.drawString(zdroje[i].getName(), x + 8, y - 35);
		       
	        	}
	            g2.setTransform(old);

	            
	    		this.label = l;

      	       
		}
		
	}
	
	/** 
	 * vykresleni vodnich zdroju a labelu
	 * @param g graficky kontext
	 */
	void drawSourcesDrawLabels (Graphics2D g) {
		  prepareWaterSources(g);
  		  g.drawImage(this.label, startX, startY, nimW, nimH, null);

	}
	
    /**
     * vykresleni sipky pomoci kodu ze cviceni
     * @param g2d   graficky kontext
     * @param x1    x-ova souradnice zacatku sipky
     * @param y1    y-ova souradnice zacatku sipky
     * @param x2    x-ova souradnice konce sipky
     * @param y2    y-ova souradnice konce sipky
     * @param k, l  nastaveni "rucicek" sipky
     */
	public void drawArrow(Graphics2D g2d, double x1, double y1, double x2, double y2, double k, double l) {
	      g2d.draw(new Line2D.Double(x1, y1, x2, y2));
	      
	      double ux = x2-x1;
	      double uy = y2-y1;
	      
	      double len = Math.hypot(ux,uy);
	      
	      ux/=len;
	      uy/=len;
	      
	      double nx = uy;
	      double ny = -ux;
	      
	      double cx = x2 - ux*l;
	      double cy = y2 - uy*l;
	      
	      double d1x = cx + nx*k;
	      double d1y = cy + ny*k;
	      double d2x = cx - nx*k;
	      double d2y = cy - ny*k;

          g2d.draw(new Line2D.Double(d1x, d1y, x2, y2));
	      g2d.draw(new Line2D.Double(d2x, d2y, x2, y2));
	    }
	
	/**
	 * prekresli pozadi na bilo
	 * @param g2d graficky kontext
	 */
	public void drawBackground(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width*width, height*height);
	}
	
	/**
	 * 	tato metoda zajistuje vytisteni mapy
	 * 	@param graphics graficky kontext
	 *  @param pageFormat format stranky
	 *  @param pageIndex index stranky
	 */
	@Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D)graphics;
       
        g2.translate(pageFormat.getWidth() / 10, 10);
		computeModel2WindowTransformation((int)(pageFormat.getWidth() * 0.8), (int)(pageFormat.getHeight() * 0.8));
		g2.scale(getDeltaXAbs(), getDeltaYAbs());
      
        drawBackground(g2);
		drawWaterFlowState(g2);
		drawSourcesDrawLabels(g2);

        return PAGE_EXISTS;
    }
	
	/**
	 * metoda se stara o vykreslovani do DrawingPanelu a vola jednotlive metody na vykresleni
	 * take tu je zajistena reakce na nextStep a data do grafu se sbiraji kazdy paty nextstep
	 */
	 @Override
	    public void paint(Graphics g) {
		 
		  Graphics2D g2 = (Graphics2D)g;
		  g2.scale(Math.abs(deltaVector.x), Math.abs(deltaVector.y));
		  drawBackground(g2);
		  drawWaterFlowState(g2);
		  drawSourcesDrawLabels(g2);
		  computeModel2WindowTransformation((int)(this.getWidth()/Math.abs(deltaVector.x)), (int)(this.getHeight()/Math.abs(deltaVector.y)));
		  		  
		  if (nextStep > 0) {
			  
			  elapsedTime += nextStep;
			  Simulator.nextStep(nextStep);
			  
				  prepareGraphData();
				  nextStepCounter = 0;
		  }
		  
		  createSelectionTool(g2, graphSelectionXStart, graphSelectionYStart, graphSelectionXEnd, graphSelectionYEnd, isSelectingGraph);
		  timer.schedule(new TimerTask() {
			  @Override
			  public void run() {
				  repaint(); 
			  }
		  }, 100);
		  	  
	    }
	
	}
