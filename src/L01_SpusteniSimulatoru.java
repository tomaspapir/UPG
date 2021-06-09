import waterflowsim.Simulator;
import waterflowsim.Vector2D;
import waterflowsim.Scenarios;
import waterflowsim.Cell;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Tato trida se stara o vytvoreni okna a nastaveni
 * scenare podle argumentu zadanym uzivatelem
 * @author Tomas Papirnik
 *
 */

public class L01_SpusteniSimulatoru {

	/** hlavni metoda programu
	 * @param args cislo scenare, ktery se ma zapnout
	 */
	public static void main(String[] args) {	
		
		if(args.length == 0 || Integer.parseInt(args[0]) > (Simulator.getScenarios().length - 1) || Integer.parseInt(args[0]) < 0) {
			Simulator.runScenario(0);
		}
		
		else {
			Simulator.runScenario(Integer.parseInt(args[0]));
		}
	
		
		JFrame win = new JFrame();
		
	    win.setTitle("Tomas Papirnik - A19B0155P");

	    makeGui(win);

	    win.pack();

	    win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    win.setLocationRelativeTo(null);
	    win.setVisible(true);
	}
	
	/**
	 * vytvoreni noveho DrawingPanelu a LegendPanelu na kresleni a pridani ho do JFramu
	 * dale vytvoreni tlacitka na tisk a reakce na jeho kliknuti
	 * @param win JFrame, do ktereho se ma pridat vytvoreny DrawingPanel
	 */
	 private static void makeGui(JFrame win) {
		
	        DrawingPanel model = new DrawingPanel();
	        LegendPanel legend = new LegendPanel();
	        win.setLayout(new BorderLayout());
	        win.add(legend, BorderLayout.EAST);
	        win.add(model, BorderLayout.CENTER);
	        
	        JButton print = new JButton("Print");
	        
	        print.addActionListener(new ActionListener() {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                PrinterJob job = PrinterJob.getPrinterJob();
	                if (job.printDialog())
	                {
	                    job.setPrintable(model);
	                    try {
	                        job.print();
	                    } catch (PrinterException ex) {
	                        ex.printStackTrace();
	                    }
	                }
	            }
	        });

	        JPanel buttons = new JPanel();
	        
	        buttons.add(model.getButtons());
	        buttons.add(print);
	        
	        win.add(buttons, BorderLayout.SOUTH);
	 }
		 
}
