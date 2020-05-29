package org.moeaframework.analysis.diagnostics;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import resources.Resource;

public class ParameterPane extends JFrame {

	private static final long serialVersionUID= 62946895420923644L;

	public ParameterPane(String title, Parameterization p) throws IOException {

		super(title);

		String[] files= getFiles(p);

		setLayout(new GridLayout(2, 3));
		JFreeChart sbxplot= getChart("SBX", getData(files[0]));
		JFreeChart deplot= getChart("DE", getData(files[1]));
		JFreeChart umplot= getChart("UM", getData(files[2]));
		JFreeChart pcxplot= getChart("PCX", getData(files[3]));
		JFreeChart undxplot= getChart("UNDX", getData(files[4]));
		JFreeChart spxplot= getChart("SPX", getData(files[5]));

		getContentPane().add(new ChartPanel(sbxplot));
		getContentPane().add(new ChartPanel(deplot));
		getContentPane().add(new ChartPanel(umplot));
		getContentPane().add(new ChartPanel(pcxplot));
		getContentPane().add(new ChartPanel(undxplot));
		getContentPane().add(new ChartPanel(spxplot));

		setSize(12000, 12000);
		pack();
	}

	private JFreeChart getChart(String name, XYDataset data) {

		JFreeChart chart= ChartFactory.createScatterPlot(
			name,
			null, null, data, PlotOrientation.HORIZONTAL, false, false, false);
		chart.setBorderVisible(false);
		chart.getTitle().setFont(new Font("times", 12, 20));
		final XYPlot plot= (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);

		final XYItemRenderer renderer= plot.getRenderer();
		Ellipse2D.Double circle= new Ellipse2D.Double(-3, -3, 3, 3);
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setSeriesShape(0, circle);

		if (name != "DE") {
			final ValueAxis x= plot.getDomainAxis();
			x.setVisible(false);
			x.setRange(0.0, 10.0);
			final ValueAxis y= plot.getRangeAxis();
			y.setVisible(false);
			y.setRange(0.0, 10.0);
		} else {
			final ValueAxis x= plot.getDomainAxis();
			x.setVisible(false);
			x.setRange(0.0, 12.0);
			final ValueAxis y= plot.getRangeAxis();
			y.setVisible(false);
			y.setRange(0.0, 10.0);
		}

		return chart;
	}

	public XYDataset getData(String filename) throws IOException {
		XYSeriesCollection dataset= new XYSeriesCollection();
		XYSeries data= createDataSet(filename);
		dataset.addSeries(data);
		return dataset;
	}

	public static XYSeries createDataSet(String name) throws IOException {

		InputStream in= Resource.class.getResourceAsStream(name);
		BufferedReader csvR= new BufferedReader(new InputStreamReader(in));
		XYSeries data= new XYSeries("param data");

		while (csvR.readLine() != null) {
			String row= csvR.readLine();
			if (row != null) {
				String[] entry= row.split(",");
				data.add(Double.parseDouble(entry[1]), Double.parseDouble(entry[0]));
			}

		}

		csvR.close();
		in.close();
		return data;
	}

	private String roundZeroToOne(double num) {
		double rounded= Math.round(num * 10) / 10.0;
		return Double.toString(rounded);
	}

	private String roundZeroToHundred(double num) {
		int rounded= (int) Math.rint(num);
		int tens= (rounded + 5) / 10 * 10;
		return Integer.toString(tens);
	}

	private String[] getRelaventData(Parameterization p) {
		String[] paramdata= new String[11];

		// get sbx info
		paramdata[0]= roundZeroToOne(p.sbxRate);
		paramdata[1]= roundZeroToHundred(p.sbxDistributionIndex);

		// get de info
		paramdata[2]= roundZeroToOne(p.deCrossoverRate);
		paramdata[3]= roundZeroToOne(p.deStepSize);

		// get um info
		paramdata[4]= roundZeroToOne(p.umRate);

		// get pcx info
		paramdata[5]= roundZeroToOne(p.pcxEta);
		paramdata[6]= roundZeroToOne(p.pcxZeta);

		// get undx info
		paramdata[7]= roundZeroToOne(p.undxEta);
		paramdata[8]= roundZeroToOne(p.undxZeta);

		// get spx info
		paramdata[9]= roundZeroToOne(p.spxEpsilon);

		// get pm info
		paramdata[10]= roundZeroToHundred(p.pmDistributionIndex);

		return paramdata;
	}

	private String[] getFiles(Parameterization p) {
		String[] files= new String[6];
		String[] suffix= getRelaventData(p);
		String end= "_pm" + suffix[10] + ".csv";

		// sbx file
		files[0]= "sbx_data_" + suffix[0] + "_" + suffix[1] + end;

		// de file
		files[1]= "de_data_" + suffix[2] + "_" + suffix[3] + end;

		// um file
		files[2]= "um_data_" + suffix[4] + end;

		// pcx file
		files[3]= "pcx_data_" + suffix[5] + "_" + suffix[6] + end;

		// undx file
		files[4]= "undx_data_" + suffix[7] + "_" + suffix[8] + end;

		// spx file
		files[5]= "spx_data_" + suffix[9] + end;

		return files;
	}
}
