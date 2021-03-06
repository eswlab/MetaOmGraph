package edu.iastate.metnet.metaomgraph.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Font;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import edu.iastate.metnet.metaomgraph.MetaOmGraph;
import edu.iastate.metnet.metaomgraph.MetadataCollection;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MetadataRemoveCols extends JFrame {

	private JPanel contentPane;
	private JTable table;
	int datacolIndex = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// MetadataRemoveCols frame = new MetadataRemoveCols(new String[]
					// {"a1","2b","34"},null,null);
					// frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MetadataRemoveCols getThisframe() {
		return this;
	}

	/**
	 * Create the frame.
	 */
	public MetadataRemoveCols(String[] headervals, MetadataCollection obj, ReadMetadata p) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setFont(new Font("Garamond", Font.BOLD, 16));
		contentPane.add(panel, BorderLayout.NORTH);

		JLabel lblRemoveColumns = new JLabel("Remove Columns");
		lblRemoveColumns.setForeground(Color.GREEN);
		lblRemoveColumns.setFont(new Font("Garamond", Font.BOLD, 15));
		panel.add(lblRemoveColumns);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.DARK_GRAY);
		contentPane.add(panel_1, BorderLayout.SOUTH);

		JButton btnDone = new JButton("Done");
		btnDone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (obj == null) {
					JOptionPane.showMessageDialog(panel, "Error!!! Can't delete columns. No file read...", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String[] newheaders = headervals;
				boolean[] toKeep = new boolean[newheaders.length];
				java.util.List<String> removed=new ArrayList<>();
				// get data from table
				DefaultTableModel tablemodel = (DefaultTableModel) table.getModel();
				for (int i = 0; i < tablemodel.getRowCount(); i++) {
					if (tablemodel.getValueAt(i, 2).toString().equals("Keep")) {
						toKeep[i] = true;
					} else {
						removed.add(tablemodel.getValueAt(i, 1).toString());
						toKeep[i] = false;
					}

				}

				obj.setHeaders(newheaders, toKeep);
				
				p.updateHeaders();
				p.updateRemovedCols(removed);
				getThisframe().dispose();
				p.setEnabled(true);
				p.toFront();
				p.updateTable();
				
			}
		});
		panel_1.add(btnDone);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.DARK_GRAY);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		table = new JTable() {
			@Override
			public boolean isCellEditable(int row, int column) { // dont let edit first 2 cols
				if (column == 0 || column == 1) {
					return false;
				}
				return true;
			}
		};
		table.setRowMargin(3);
		table.setRowHeight(25);
		table.setIntercellSpacing(new Dimension(2, 2));
		table.setModel(
				new DefaultTableModel(new Object[][] {}, new String[] { "Column number", "Header", "Keep/Remove" }));
		// set default values
		DefaultTableModel tablemodel = (DefaultTableModel) table.getModel();
		for (int i = 0; i < headervals.length; i++) {
			tablemodel.addRow(new String[] { String.valueOf(i + 1), headervals[i], "Keep" });
		}
	
		TableColumn optionColumn = table.getColumnModel().getColumn(2);
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Keep");
		comboBox.addItem("Remove");
		comboBox.setSelectedIndex(0);
		optionColumn.setCellEditor(new DefaultCellEditor(comboBox));

		table.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		table.setForeground(Color.RED);
		table.setBackground(Color.BLACK);
		scrollPane.setViewportView(table);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	public MetadataRemoveCols(String[] headervals, MetadataCollection obj, MetadataTableDisplayPanel p,
			boolean permanent) {

		if (obj == null) {
			JOptionPane.showMessageDialog(p, "Error!!! Can't delete columns. No file read...", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setTitle("Metadata Column Filter");
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		JPanel panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setFont(new Font("Garamond", Font.BOLD, 16));
		contentPane.add(panel, BorderLayout.NORTH);
		JLabel lblRemoveColumns;
		if (permanent) {
			lblRemoveColumns = new JLabel("Remove Columns");
		} else {
			lblRemoveColumns = new JLabel("Filter Columns");
		}
		lblRemoveColumns.setForeground(Color.GREEN);
		lblRemoveColumns.setFont(new Font("Garamond", Font.BOLD, 15));
		panel.add(lblRemoveColumns);
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.DARK_GRAY);
		contentPane.add(panel_1, BorderLayout.SOUTH);
		JButton btnDone = new JButton("Done");
		panel_1.add(btnDone);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.DARK_GRAY);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		table = new JTable() {
			@Override
			public boolean isCellEditable(int row, int column) { // dont let edit first 2 cols
				if (column == 0 || column == 1) {
					return false;
				}
				return true;
			}
		};
		table.setRowMargin(3);
		table.setRowHeight(25);
		table.setIntercellSpacing(new Dimension(2, 2));
		table.setModel(
				new DefaultTableModel(new Object[][] {}, new String[] { "Column number", "Header", "Keep/Remove" }));
		// set default values
		DefaultTableModel tablemodel = (DefaultTableModel) table.getModel();
		java.util.List<String> curr_headers = Arrays.asList(headervals);
		String[] allHeaders = obj.getHeaders();
		boolean[] toKeep = new boolean[allHeaders.length];
		for (int i = 0; i < allHeaders.length; i++) {
			if (curr_headers.contains(allHeaders[i])) {
				toKeep[i] = true;
			} else {
				toKeep[i] = false;
			}
		}

		for (int i = 0; i < allHeaders.length; i++) {
			// skip data columns as it cant be removed
			if (allHeaders[i].equals(MetaOmGraph.getActiveProject().getMetadataHybrid().getDataColName())) {
				datacolIndex = i;
				continue;
			}
			if (toKeep[i]) {
				tablemodel.addRow(new String[] { String.valueOf(i + 1), allHeaders[i], "Keep" });
			} else {
				tablemodel.addRow(new String[] { String.valueOf(i + 1), allHeaders[i], "Remove" });
			}
		}
		
		TableColumn optionColumn = table.getColumnModel().getColumn(2);
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Keep");
		comboBox.addItem("Remove");
		comboBox.setSelectedIndex(0);
		optionColumn.setCellEditor(new DefaultCellEditor(comboBox));

		table.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		table.setForeground(Color.RED);
		table.setBackground(Color.BLACK);
		scrollPane.setViewportView(table);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// action
		btnDone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// get data from table
				DefaultTableModel tablemodel = (DefaultTableModel) table.getModel();
				int j = 0;
				for (int i = 0; i < allHeaders.length; i++) {
					if (i == datacolIndex) {
						toKeep[i] = true;
						continue;
					}
					if (tablemodel.getValueAt(j, 2).toString().equals("Keep")) {
						toKeep[i] = true;
						j++;
					} else {
						toKeep[i] = false;
						j++;
					}
				}
				if (permanent) {
					int result = JOptionPane.showConfirmDialog((Component) null, "This will delete selected columns. This can't be undone","alert", JOptionPane.OK_CANCEL_OPTION);
					if(result==JOptionPane.CANCEL_OPTION) {
						return;
					}
					// update headers in object
					obj.setHeaders(allHeaders, toKeep);
					// update data in table panel
					p.resetData();
					p.updateTable(true);
					p.updateHeaders();
					MetaOmGraph.getActiveProject().getMetadataHybrid().setCurrentHeaders(obj.getHeaders());

				} else {
					String[] newHeaders = removeExcluded(toKeep, allHeaders);
					p.setHeaders(newHeaders);
					p.updateTable(true);
					p.updateHeaders();
					MetaOmGraph.getActiveProject().getMetadataHybrid().setCurrentHeaders(newHeaders);
				}
				getThisframe().dispose();
			}
		});

	}

	protected String[] removeExcluded(boolean[] toKeep, String[] headervals) {
		java.util.List<String> reslist = new ArrayList<>();
		for (int i = 0; i < toKeep.length; i++) {
			if (toKeep[i]) {
				reslist.add(headervals[i]);
			}
		}
		return reslist.toArray(new String[0]);
	}

	public void permanentlyRemoveHeaders() {

	}

	public String[] tempRemoveHeaders() {
		String[] res = null;
		return res;
	}

}
