package edu.iastate.metnet.metaomgraph.ui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import edu.iastate.metnet.metaomgraph.AdjustPval;
import edu.iastate.metnet.metaomgraph.DecimalFormatRenderer;
import edu.iastate.metnet.metaomgraph.DifferentialExpResults;
import edu.iastate.metnet.metaomgraph.MetaOmGraph;
import edu.iastate.metnet.metaomgraph.MetaOmProject;
import edu.iastate.metnet.metaomgraph.chart.BoxPlot;
import edu.iastate.metnet.metaomgraph.chart.HistogramChart;
import edu.iastate.metnet.metaomgraph.chart.MetaOmChartPanel;
import edu.iastate.metnet.metaomgraph.chart.ScatterPlotChart;
import edu.iastate.metnet.metaomgraph.chart.VolcanoPlot;
import edu.iastate.metnet.metaomgraph.utils.Utils;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class logFCResultsFrame extends JInternalFrame {
	private JTable table;
	private List<String> featureNames;
	private List<Double> mean1;
	private List<Double> mean2;
	private List<Double> testPvals;
	private List<Double> ftestPvals;
	private List<Double> ftestRatiovals;
	private List<Double> testadjutestPvals;
	private List<Double> ftestadjutestPvals;
	private MetaOmProject myProject;
	String name1;
	String name2;
	String methodName;
	String pvAdjMethod;

	double pvThresh = 2;

	/**
	 * Default Properties
	 */

	private Color SELECTIONBCKGRND = MetaOmGraph.getTableSelectionColor();
	private Color BCKGRNDCOLOR1 = MetaOmGraph.getTableColor1();
	private Color BCKGRNDCOLOR2 = MetaOmGraph.getTableColor2();
	private Color HIGHLIGHTCOLOR = MetaOmGraph.getTableHighlightColor();
	private Color HYPERLINKCOLOR = MetaOmGraph.getTableHyperlinkColor();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

			}
		});
	}

	/**
	 * Create the frame.
	 */
	public logFCResultsFrame() {
		this(null, null, null, null, null, null, null, null, null, null);
	}

	public logFCResultsFrame(List<String> featureNames, List<Double> mean1, List<Double> mean2,
			MetaOmProject myProject) {
		this(featureNames, mean1, mean2, null, null, null, null, null, null, myProject);
	}

	public logFCResultsFrame(DifferentialExpResults ob, MetaOmProject myProject) {
		this(ob.getRowNames(), ob.getMeanGrp1(), ob.getMeanGrp2(), ob.getGrp1Name(), ob.getGrp2Name(),
				ob.getmethodName(), ob.getPVal(), ob.getfStat(), ob.getFPVal(), myProject);
	}

	public logFCResultsFrame(List<String> featureNames, List<Double> mean1, List<Double> mean2, String name1,
			String name2, String methodName, List<Double> pv, List<Double> ftestratio, List<Double> ftestpv,
			MetaOmProject myProject) {
		this.name1 = name1;
		this.name2 = name2;
		this.methodName = methodName;
		this.featureNames = featureNames;
		this.mean1 = mean1;
		this.mean2 = mean2;
		this.myProject = myProject;
		testPvals = pv;
		ftestRatiovals = ftestratio;
		ftestPvals = ftestpv;
		// compute adjusted pv
		if (testPvals != null) {
			testadjutestPvals = AdjustPval.computeAdjPV(testPvals, pvAdjMethod);
		}
		if (ftestPvals != null) {

			ftestadjutestPvals = AdjustPval.computeAdjPV(ftestPvals, pvAdjMethod);
		}

		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		initTableModel();
		updateTable();
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSave = new JMenuItem("Save to file");
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Utils.saveJTabletofile(table);
			}
		});
		mnFile.add(mntmSave);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenuItem mntmExportSelectedTo = new JMenuItem("Export selected to list");
		mntmExportSelectedTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get selected rowindex
				int[] rowIndices = getSelectedRowIndices();
				if (rowIndices == null || rowIndices.length == 0) {
					JOptionPane.showMessageDialog(null, "No rows selected", "Nothing selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String listName = JOptionPane.showInputDialog(logFCResultsFrame.this, "Enter a name for new list");
				if (listName == null || listName.length() < 1) {
					JOptionPane.showMessageDialog(logFCResultsFrame.this, "Invalid name", "Failed",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (myProject.addGeneList(listName, rowIndices, true)) {
					JOptionPane.showMessageDialog(logFCResultsFrame.this, "List" + listName + " added", "List added",
							JOptionPane.INFORMATION_MESSAGE);
				}
				return;
			}
		});
		mnEdit.add(mntmExportSelectedTo);

		JMenuItem mntmFilter = new JMenuItem("P-value filter");
		mntmFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double pvalThresh = 0;
				try {
					String input = (String) JOptionPane.showInputDialog(null, "Please Enter a value", "Input p-value",
							JOptionPane.QUESTION_MESSAGE, null, null, String.valueOf(pvThresh));
					if (input == null) {
						return;
					}
					pvalThresh = Double.parseDouble(input);

				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Invalid number entered. Please try again.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				pvThresh = pvalThresh;

				updateTable();

				// JOptionPane.showMessageDialog(null, "Done");

			}
		});
		mnEdit.add(mntmFilter);

		JMenuItem mntmPvalueCorrection = new JMenuItem("P-value correction");
		mntmPvalueCorrection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				// choose adjustment method
				JPanel cboxPanel = new JPanel();
				String[] adjMethods = AdjustPval.getMethodNames();
				// get a list of multiple correction methods implemented
				JComboBox pvadjCBox = new JComboBox<>(adjMethods);
				cboxPanel.add(pvadjCBox);
				int opt = JOptionPane.showConfirmDialog(null, cboxPanel, "Select categories",
						JOptionPane.OK_CANCEL_OPTION);
				if (opt == JOptionPane.OK_OPTION) {
					// set selected method to the adjustment method
					pvAdjMethod = pvadjCBox.getSelectedItem().toString();
				} else {
					return;
				}

				// correct p values

				if (testPvals != null) {
					testadjutestPvals = AdjustPval.computeAdjPV(testPvals, pvAdjMethod);
				}
				if (ftestPvals != null) {

					ftestadjutestPvals = AdjustPval.computeAdjPV(ftestPvals, pvAdjMethod);
				}

				// update in table
				updateTable();
			}
		});
		mnEdit.add(mntmPvalueCorrection);

		JMenu mnPlot = new JMenu("Plot");
		menuBar.add(mnPlot);

		JMenu mnSelected = new JMenu("Selected");
		mnPlot.add(mnSelected);

		JMenuItem mntmLineChart = new JMenuItem("Line Chart");
		mntmLineChart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get selected rowindex
				int[] rowIndices = getSelectedRowIndices();
				if (rowIndices == null || rowIndices.length == 0) {
					JOptionPane.showMessageDialog(null, "No rows selected", "Nothing selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				new MetaOmChartPanel(rowIndices, myProject.getDefaultXAxis(), myProject.getDefaultYAxis(),
						myProject.getDefaultTitle(), myProject.getColor1(), myProject.getColor2(), myProject)
								.createInternalFrame();
			}
		});
		mnSelected.add(mntmLineChart);

		JMenuItem mntmScatterplot = new JMenuItem("Scatter Plot");
		mntmScatterplot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// get selected rowindex
				int[] rowIndices = getSelectedRowIndices();
				if (rowIndices == null) {
					JOptionPane.showMessageDialog(null, "No rows selected", "Nothing selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rowIndices.length < 1) {
					JOptionPane.showMessageDialog(null,
							"Please select two or more rows and try again to plot a scatterplot.",
							"Invalid number of rows selected", JOptionPane.ERROR_MESSAGE);
					return;
				}

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {// get data for selected rows

							ScatterPlotChart f = new ScatterPlotChart(rowIndices, 0, myProject);
							MetaOmGraph.getDesktop().add(f);
							f.setDefaultCloseOperation(2);
							f.setClosable(true);
							f.setResizable(true);
							f.pack();
							f.setSize(1000, 700);
							f.setVisible(true);
							f.toFront();

						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error occured while reading data!!!", "Error",
									JOptionPane.ERROR_MESSAGE);

							e.printStackTrace();
							return;
						}
					}
				});

				return;

			}
		});
		mnSelected.add(mntmScatterplot);

		JMenuItem mntmBoxPlot = new JMenuItem("Box Plot");
		mntmBoxPlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rowIndices = getSelectedRowIndices();
				if (rowIndices == null || rowIndices.length == 0) {
					JOptionPane.showMessageDialog(null, "No rows selected", "Nothing selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// get data for box plot as hasmap
				HashMap<Integer, double[]> plotData = new HashMap<>();
				for (int i = 0; i < rowIndices.length; i++) {
					double[] dataY = null;
					try {
						// dataY = myProject.getIncludedData(selected[i]);
						// send all data; excluded data will be excluded in the boxplot class; this
						// helps in splitting data by categories by reusing cluster function
						dataY = myProject.getAllData(rowIndices[i]);
					} catch (IOException eIO) {
						// TODO Auto-generated catch block
						eIO.printStackTrace();
					}
					plotData.put(rowIndices[i], dataY);
				}

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {// get data for selected rows

							BoxPlot f = new BoxPlot(plotData, 0, myProject);
							MetaOmGraph.getDesktop().add(f);
							f.setDefaultCloseOperation(2);
							f.setClosable(true);
							f.setResizable(true);
							f.pack();
							f.setSize(1000, 700);
							f.setVisible(true);
							f.toFront();

						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error occured while reading data!!!", "Error",
									JOptionPane.ERROR_MESSAGE);

							e.printStackTrace();
							return;
						}
					}
				});

			}
		});
		mnSelected.add(mntmBoxPlot);

		JMenuItem mntmHistogram = new JMenuItem("Histogram");
		mntmHistogram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {// get data for selected rows
							int[] selected = getSelectedRowIndices();
							if (selected == null || selected.length == 0) {
								JOptionPane.showMessageDialog(null, "No rows selected", "Nothing selected",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
							// number of bins
							int nBins = myProject.getIncludedDataColumnCount() / 10;
							HistogramChart f = new HistogramChart(selected, nBins, myProject, 1, null);
							MetaOmGraph.getDesktop().add(f);
							f.setDefaultCloseOperation(2);
							f.setClosable(true);
							f.setResizable(true);
							f.pack();
							f.setSize(1000, 700);
							f.setVisible(true);
							f.toFront();

						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Error occured while reading data!!!", "Error",
									JOptionPane.ERROR_MESSAGE);

							e.printStackTrace();
							return;
						}
					}
				});
				return;

			}
		});
		mnSelected.add(mntmHistogram);

		JMenuItem mntmVolcanoPlot = new JMenuItem("Volcano plot");
		mntmVolcanoPlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				makeVolcano();

			}
		});
		mnPlot.add(mntmVolcanoPlot);

		JMenuItem mntmFcHistogram = new JMenuItem("FC histogram");
		mntmFcHistogram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				plotColumnHistogram("logFC");

				return;

			}
		});
		mnPlot.add(mntmFcHistogram);

		JMenuItem mntmPvalHistogram = new JMenuItem("P-value histogram");
		mntmPvalHistogram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				plotColumnHistogram(methodName + " pval");

				return;

			}
		});
		mnPlot.add(mntmPvalHistogram);

		JMenuItem mntmHistogramcolumn = new JMenuItem("Histogram (column)");
		mntmHistogramcolumn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				// display option to select a column
				JPanel cboxPanel = new JPanel();
				String[] colNames = new String[table.getColumnCount() - 1];

				// dont display 1st column or other non-numerical columns
				for (int cols = 1; cols < table.getColumnCount(); cols++) {
					colNames[cols-1] = table.getColumnName(cols);
				}
				// get a list of multiple correction methods implemented
				JComboBox options = new JComboBox<>(colNames);
				cboxPanel.add(options);
				int opt = JOptionPane.showConfirmDialog(null, cboxPanel, "Select column", JOptionPane.OK_CANCEL_OPTION);
				if (opt == JOptionPane.OK_OPTION) {
					// draw histogram with the selected column
					plotColumnHistogram(options.getSelectedItem().toString());
				} else {
					return;
				}

			}
		});
		mnPlot.add(mntmHistogramcolumn);

		// frame properties
		this.setClosable(true);
		// pack();
		putClientProperty("JInternalFrame.frameType", "normal");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

	}

	private void makeVolcano() {
		// create data for volcano plot object
		List<String> featureNames = new ArrayList<>();
		List<Double> fc = new ArrayList<>();
		List<Double> pv = new ArrayList<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			featureNames.add((String) table.getModel().getValueAt(i, table.getColumn("Name").getModelIndex()));
			fc.add((Double) table.getModel().getValueAt(i, table.getColumn("logFC").getModelIndex()));
			pv.add((Double) table.getModel().getValueAt(i, table.getColumn(methodName + " pval").getModelIndex()));
		}

		// make plot
		VolcanoPlot f = new VolcanoPlot(featureNames, fc, pv, name1, name2);
		MetaOmGraph.getDesktop().add(f);
		f.setDefaultCloseOperation(2);
		f.setClosable(true);
		f.setResizable(true);
		f.pack();
		f.setSize(1000, 700);
		f.setVisible(true);
		f.toFront();

	}

	private void initTableModel() {
		table = new JTable() {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				if (!isRowSelected(row)) {
					c.setBackground(getBackground());
					int modelRow = convertRowIndexToModel(row);

					if (row % 2 == 0) {
						c.setBackground(BCKGRNDCOLOR1);
					} else {
						c.setBackground(BCKGRNDCOLOR2);
					}

				} else {
					c.setBackground(SELECTIONBCKGRND);
				}

				return c;
			}

		};

		// table mouse listener
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// only do if double click
				if (e.getClickCount() < 2) {
					return;
				}
				int row = table.convertRowIndexToModel(table.rowAtPoint(new Point(e.getX(), e.getY())));
				int col = table.convertColumnIndexToModel(table.columnAtPoint(new Point(e.getX(), e.getY())));

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				int col = table.columnAtPoint(new Point(e.getX(), e.getY()));

			}

			@Override
			public void mouseExited(MouseEvent e) {
				int col = table.columnAtPoint(new Point(e.getX(), e.getY()));

			}
		});
		// end mouse listner

		// disable colum drag
		table.getTableHeader().setReorderingAllowed(false);

		DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return String.class;
				default:
					return Double.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};
		table.setModel(model);
	}

	private void updateTable() {

		DefaultTableModel tablemodel = (DefaultTableModel) table.getModel();

		tablemodel.setRowCount(0);
		tablemodel.setColumnCount(0);
		// add data

		tablemodel.addColumn("Name");
		tablemodel.addColumn("Mean(log(" + name1 + "))");
		tablemodel.addColumn("Mean(log(" + name2 + "))");
		tablemodel.addColumn("logFC");
		if (testPvals != null) {
			if (ftestRatiovals != null && ftestRatiovals.size() > 0) {
				tablemodel.addColumn("F statistic");
				tablemodel.addColumn("F test pval");
				tablemodel.addColumn("Adj F test pval");
			}
			tablemodel.addColumn(methodName + " pval");
			tablemodel.addColumn("Adj pval");
		}
		// for each row add each coloumn
		for (int i = 0; i < featureNames.size(); i++) {
			// create a temp string storing all col values for a row
			Vector temp = new Vector<>();
			temp.add(featureNames.get(i));
			temp.add(mean1.get(i));
			temp.add(mean2.get(i));
			temp.add(mean1.get(i) - mean2.get(i));
			if (testPvals != null) {
				if (testPvals.get(i) >= pvThresh) {
					continue;
				}
				if (ftestRatiovals != null && ftestRatiovals.size() > 0) {
					temp.add(ftestRatiovals.get(i));
					temp.add(ftestPvals.get(i));
					temp.add(ftestadjutestPvals.get(i));
				}
				temp.add(testPvals.get(i));
				temp.add(testadjutestPvals.get(i));
			}
			// add ith row in table
			tablemodel.addRow(temp);
		}

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		table.getTableHeader().setFont(new Font("Garamond", Font.BOLD, 14));
		// set decimal formatter to all cols except first
		for (int i = 1; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(new DecimalFormatRenderer());
		}

	}

	private int[] getSelectedRowIndices() {
		// get correct indices wrt the list
		int[] rowIndices = table.getSelectedRows();
		// JOptionPane.showMessageDialog(null, "sR:" + Arrays.toString(rowIndices));
		List<String> names = new ArrayList<>();
		int j = 0;
		for (int i : rowIndices) {
			names.add(table.getValueAt(i, table.getColumn("Name").getModelIndex()).toString());
		}
		rowIndices = myProject.getRowIndexbyName(names, true);

		return rowIndices;
	}

	/**
	 * Function to plot histogram of selected column
	 * 
	 * @param columnName
	 */
	private void plotColumnHistogram(String columnName) {

		// plot histogram of current pvalues in table
		double[] data = new double[table.getRowCount()];
		for (int r = 0; r < table.getRowCount(); r++) {
			// get p values

			data[r] = (double) table.getModel().getValueAt(r, table.getColumn(columnName).getModelIndex());
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {// get data for selected rows
					int nBins = 10;
					HistogramChart f = new HistogramChart(null, nBins, null, 2, data);
					f.setTitle(columnName + " histogram");
					MetaOmGraph.getDesktop().add(f);
					f.setDefaultCloseOperation(2);
					f.setClosable(true);
					f.setResizable(true);
					f.pack();
					f.setSize(1000, 700);
					f.setVisible(true);
					f.toFront();

				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error occured while reading data!!!", "Error",
							JOptionPane.ERROR_MESSAGE);

					e.printStackTrace();
					return;
				}
			}
		});
		return;

	}

}
