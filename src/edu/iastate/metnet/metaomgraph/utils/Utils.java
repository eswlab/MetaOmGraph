package edu.iastate.metnet.metaomgraph.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import edu.iastate.metnet.metaomgraph.GraphFileFilter;
import edu.iastate.metnet.metaomgraph.MetaOmGraph;

public class Utils {
	public static final int LOCUS = 1;
	public static final int AFFY8K = 2;
	public static final int AFFY25K = 3;
	public static final int NONE = -1;
	private static File lastDir;
	private static Long startTime;

	public Utils() {
	}

	public static <T> boolean isIn(T findMe, T[] entries) {
		boolean found = false;
		for (int i = 0; (i < entries.length) && (!found);) {
			found = findMe.equals(entries[(i++)]);
		}
		return found;
	}

	public static boolean isIn(int findMe, int[] entries) {
		boolean found = false;
		for (int i = 0; (i < entries.length) && (!found); found = entries[(i++)] == findMe) {
		}

		return found;
	}

	public static String clean(String cleanMe) {
		if (cleanMe == null)
			return null;
		String result = cleanMe.trim();
		while ((result.startsWith("\"")) || (result.startsWith("<")))
			result = result.substring(1);
		while (result.endsWith("\""))
			result = result.substring(0, result.lastIndexOf("\""));
		return result;
	}

	public static String getExtension(File f) {
		if (f != null) {
			return getExtension(f.getName());
		}
		return "";
	}

	public static String getExtension(String filename) {
		if (filename == null)
			return "";
		int i = filename.lastIndexOf('.');
		if ((i > 0) && (i < filename.length() - 1)) {
			return filename.substring(i + 1).toLowerCase();
		}
		return "";
	}

	public static String removeExtension(String fileName) {
		if (fileName.lastIndexOf(".") < 0) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	public static String removeExtension(File f) {
		return removeExtension(f.getAbsolutePath());
	}

	public static boolean isGeneID(String checkMe) {
		return getIDType(checkMe) != -1;
	}

	public static boolean isGeneID(String checkMe, boolean ignore8k) {
		int idType = getIDType(checkMe);
		return (idType != -1) && (idType != 2);
	}

	public static int getIDType(String geneID) {
		if (geneID == null)
			return -1;
		String checkMe = geneID.toLowerCase();
		String locusPattern = "at[1-5cm]g[0-9]{5}";
		String affy8kPattern1 = "[12][0-9]{4}_at";
		String affy8kPattern2 = "[12][0-9]{4}_[sig]_at";
		String affy25kPattern1 = "2[456][0-9]{4}_at";
		String affy25kPattern2 = "2[456][0-9]{4}_[sx]_at";
		if (checkMe.matches(locusPattern))
			return 1;
		if ((checkMe.matches(affy8kPattern1)) || (checkMe.matches(affy8kPattern2)))
			return 2;
		if ((checkMe.matches(affy25kPattern1)) || (checkMe.matches(affy25kPattern2)))
			return 3;
		return -1;
	}

	public static String makeQueryString(ResultSet rs, String column, String name) throws SQLException {
		if (!rs.first())
			return "";
		String result = "where (" + name + "='" + rs.getString(column) + "'";
		rs.next();
		while (!rs.isAfterLast()) {
			result = result + " or " + name + "='" + rs.getString(column) + "'";
			rs.next();
		}
		result = result + ")";
		return result;
	}

	public static String makeQueryString(String[] values, String column) {
		if ((values == null) || (values.length <= 0))
			return "";
		System.out.println("making query string");
		String result = "where (" + column + "='" + values[0] + "'";
		for (int x = 1; x < values.length; x++) {
			System.out.println(x + "=" + values[x]);
			result = result + " or " + column + "='" + values[x] + "'";
		}
		result = result + ")";
		System.out.println("done with query string");
		return result;
	}

	public static File chooseFileToOpen() {
		return chooseFileToOpen(null, null);
	}

	public static File chooseFileToOpen(FileFilter filter, Component parent) {
		if (isMac()) {
			return macFileOpen(filter, parent);
		}
		File selectedFile = null;
		JFileChooser chooseDialog = new JFileChooser(getLastDir());
		chooseDialog.setFileSelectionMode(0);
		if (filter != null) {
			chooseDialog.setFileFilter(filter);
		}
		int returnVal;
		for (returnVal = 0; ((selectedFile == null) || (!selectedFile.exists()))
				&& (returnVal != 1); selectedFile = chooseDialog.getSelectedFile()) {
			returnVal = chooseDialog.showOpenDialog(parent);
		}
		if (returnVal == 1) {
			return null;
		}
		setLastDir(selectedFile.getParentFile());
		return selectedFile;
	}

	private static File macFileOpen(final FileFilter filter, Component parent) {
		Frame parentFrame = getFrameParent(parent);
		FileDialog chooseDialog;
		if (parentFrame != null) {
			chooseDialog = new FileDialog(parentFrame, "Open");
		} else {
			Dialog parentDialog = getDialogParent(parent);
			if (parentDialog != null) {
				chooseDialog = new FileDialog(parentDialog, "Open");
			} else {
				chooseDialog = new FileDialog(new JFrame(), "Open");
			}
		}
		if (getLastDir() != null) {
			chooseDialog.setDirectory(getLastDir().getAbsolutePath());
		}
		if (filter != null) {
			FilenameFilter nameFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return filter.accept(new File(dir, name));
				}

			};
			chooseDialog.setFilenameFilter(nameFilter);
		}

		chooseDialog.setVisible(true);
		String selectedFile = chooseDialog.getFile();
		if (selectedFile == null) {
			return null;
		}
		File result = new File(chooseDialog.getDirectory(), selectedFile);
		setLastDir(result.getParentFile());
		return result;
	}

	public static File chooseFileToSave(FileFilter filter, String extension, Component parent, boolean forceExtension) {
		if (isMac()) {
			return macFileSave(filter, extension, parent, forceExtension);
		}
		File destination = null;
		String filename = null;
		JFileChooser chooseDialog = new JFileChooser(getLastDir());
		chooseDialog.setFileSelectionMode(0);
		if (filter != null)
			chooseDialog.setFileFilter(filter);
		int returnVal = 0;
		for (boolean ready = false; !ready;) {
			while ((destination == null) && (returnVal != 1)) {
				returnVal = chooseDialog.showSaveDialog(parent);
				destination = chooseDialog.getSelectedFile();
			}
			if (returnVal != 0)
				return null;
			filename = destination.getAbsolutePath();
			if ((extension != null) && (!extension.equals(""))) {
				if (forceExtension) {
					if (!getExtension(filename.toLowerCase()).equals(extension.toLowerCase())) {
						filename = filename + "." + extension;
					}
				} else if (getExtension(filename).equals("")) {
					filename = filename + "." + extension;
				}

				destination = new File(filename);
			}
			if (destination.exists()) {
				int overwrite = JOptionPane.showConfirmDialog(parent, filename + " already exists.  Overwrite?",
						"Overwrite File", 1, 2);
				if ((overwrite == 2) || (overwrite == -1))
					return null;
				if (overwrite == 0) {
					ready = true;
				} else
					destination = null;
			} else {
				ready = true;
			}
		}
		setLastDir(destination.getParentFile());
		return destination;
	}

	private static File macFileSave(final FileFilter filter, String extension, Component parent,
			boolean forceExtension) {
		String filename = null;
		File destination = null;

		Frame parentFrame = getFrameParent(parent);
		FileDialog chooseDialog;
		if (parentFrame != null) {
			chooseDialog = new FileDialog(parentFrame, "Save", 1);
		} else {
			Dialog parentDialog = getDialogParent(parent);
			if (parentDialog != null) {
				chooseDialog = new FileDialog(parentDialog, "Save", 1);
			} else {
				chooseDialog = new FileDialog(new JFrame(), "Save", 1);
			}
		}
		if (getLastDir() != null) {
			chooseDialog.setDirectory(getLastDir().getAbsolutePath());
		}
		if (filter != null) {
			FilenameFilter nameFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {

					return filter.accept(new File(dir, name));
				}

			};
			chooseDialog.setFilenameFilter(nameFilter);
		}
		boolean success = false;
		while (!success) {
			chooseDialog.setVisible(true);
			filename = chooseDialog.getFile();
			if (filename == null) {
				return null;
			}
			if ((extension != null) && (!extension.equals(""))) {
				if (forceExtension) {
					if (!getExtension(filename.toLowerCase()).equals(extension.toLowerCase())) {
						filename = filename + "." + extension;
					}
				} else if (getExtension(filename).equals("")) {
					filename = filename + "." + extension;
				}
			}

			destination = new File(chooseDialog.getDirectory(), filename);

			success = true;
		}
		setLastDir(chooseDialog.getDirectory());
		return destination;
	}

	public static File getLastDir() {
		return lastDir;
	}

	public static void setLastDir(File newLastDir) {
		if ((newLastDir != null) && (newLastDir.exists()) && (newLastDir.isDirectory()))
			lastDir = newLastDir;
	}

	public static void setLastDir(String newLastDir) {
		setLastDir(new File(newLastDir));
	}

	public static File chooseFileToSave() {
		return chooseFileToSave(null, null, null, false);
	}

	public static int linearSearch(Object[] array, Object findMe) {
		for (int x = 0; x < array.length; x++) {
			if (array[x].equals(findMe))
				return x;
		}
		return -1;
	}

	public static String getPOName(String POTerm) {
		return POTerm.substring(POTerm.lastIndexOf(":") + 2).trim();
	}

	public static void setSearchFieldColors(JTextField field, boolean found) {
		if ((field.getText().equals("")) || (found)) {
			field.setBackground(Color.WHITE);
			field.setForeground(Color.BLACK);
		} else {
			field.setBackground(new Color(255, 102, 102));
			field.setForeground(Color.WHITE);
		}
	}

	public static void sizeColumnWidthToFit(JTable table) {
		table.setAutoResizeMode(0);
		for (int col = 0; col < table.getColumnCount(); col++) {
			TableColumn myCol = table.getColumnModel().getColumn(col);
			int longest = myCol.getHeaderRenderer()
					.getTableCellRendererComponent(table, myCol.getHeaderValue(), false, false, 0, col)
					.getPreferredSize().width;
			int longIndex = -1;
			for (int row = 0; row < table.getRowCount(); row++) {
				if (table.getValueAt(row, col) != null) {
					String thisData = table.getValueAt(row, col).toString();
					if (thisData.indexOf(";") >= 0)
						thisData = thisData.substring(0, thisData.indexOf(";"));
					if (thisData.length() > longest) {
						longest = thisData.length();
						longIndex = row;
					}
				}
			}
			if (longIndex == -1) {
				myCol.setPreferredWidth(longest);
			} else {
				String longString = table.getValueAt(longIndex, col) + "";
				int width = table.getFontMetrics(table.getFont()).stringWidth(longString)
						+ 2 * table.getColumnModel().getColumnMargin() + 2;
				if (width > 300)
					width = 300;
				myCol.setPreferredWidth(width);
			}
		}
	}

	public static FileFilter createFileFilter(final String[] extensions, final String description) {
		if ((extensions == null) || (extensions.length <= 0)) {
			throw new InvalidParameterException("extensions must not be null, and must have length>0");
		}

		FileFilter filter = new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				for (int x = 0; x < extensions.length; x++) {
					if (getExtension(f).toLowerCase().equals(extensions[x].toLowerCase()))
						return true;
				}
				return false;
			}

			public String getDescription() {
				String extString = "(*." + extensions[0];
				for (int x = 1; x < extensions.length; x++) {
					extString += ", *." + extensions[x];
				}
				extString += ")";
				return description + " " + extString;
			}

		};
		return filter;

	}

	public static FileFilter createFileFilter(String extension, String description) {
		return createFileFilter(new String[] { extension }, description);
	}

	public static boolean onSamePath(TreeNode[] path1, TreeNode[] path2) {
		if ((path1 == null) || (path2 == null))
			return false;
		boolean result = true;
		int shortest = path1.length < path2.length ? path1.length - 1 : path2.length - 1;
		for (int x = shortest; (x > 0) && (result); x--)
			result = path1[x] == path2[x];
		return result;
	}

	public static String hardWrap(String text, int len) {
		if (text == null) {
			return "";
		}
		if (len <= 0) {
			return text;
		}
		if (text.length() < len) {
			return text;
		}
		StringBuilder result = new StringBuilder();
		String sep = System.getProperty("line.separator");
		int lastBreak = 0;
		while (lastBreak < text.length()) {
			int end = lastBreak + len;
			if (end > text.length()) {
				end = text.length();
			}
			result.append(text.substring(lastBreak, end));
			lastBreak += len;
			if (lastBreak < text.length()) {
				result.append(sep);
			}
		}
		return result.toString();
	}

	public static String wrapText(String text, int len, String lineBreak) {
		if (text == null) {
			return new String();
		}

		if (len <= 0) {
			return new String(text);
		}

		if (text.length() <= len) {
			return new String(text);
		}
		char[] chars = text.toCharArray();
		Vector<String> lines = new Vector();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		for (int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if (chars[i] == ' ') {
				if (line.length() + word.length() > len) {
					lines.add(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
		}

		if (word.length() > 0) {
			if (line.length() + word.length() > len) {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		if (line.length() > 0) {
			lines.add(line.toString());
		}

		String ret = new String();
		int c = 0;
		for (Enumeration<String> e = lines.elements(); e.hasMoreElements(); c++) {
			if (c != 0) {
				ret = ret + lineBreak + e.nextElement();
			} else {
				ret = e.nextElement();
			}
		}

		return ret;
	}

	public static void appendMapEntry(Map map, Object key, String newValue, String delimiter) {
		Object oldValue = map.get(key);
		if (oldValue == null) {
			map.put(key, newValue);
		} else {
			String combinedValue = oldValue + delimiter + newValue;
			map.put(key, combinedValue);
		}
	}

	public static File unzip(File source, ZipEntry entry) throws IOException {
		return unzip(source, entry, null);
	}

	public static File unzip(File source, ZipEntry entry, String filenamePrefix) throws IOException {
		if (filenamePrefix == null) {
			filenamePrefix = "";
		}
		File result = new File(source.getParent(), filenamePrefix + entry.getName());
		ZipFile zipSource = new ZipFile(source);
		BufferedInputStream in = new BufferedInputStream(zipSource.getInputStream(entry));
		FileOutputStream out = new FileOutputStream(result);
		int i = 0;
		byte[] bytesIn = new byte[1024];
		while ((i = in.read(bytesIn)) >= 0) {
			out.write(bytesIn, 0, i);
		}
		return result;
	}

	public static File[] unzip(File source, File destDir) throws IOException {
		ZipFile zipSource = new ZipFile(source);
		File[] result = new File[zipSource.size()];
		Enumeration<? extends ZipEntry> entries = zipSource.entries();
		int count = 0;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			BufferedInputStream in = new BufferedInputStream(zipSource.getInputStream(entry));
			File outfile = new File(destDir, entry.getName());
			FileOutputStream out = new FileOutputStream(outfile);
			int i = 0;
			byte[] bytesIn = new byte[1024];
			while ((i = in.read(bytesIn)) >= 0) {
				out.write(bytesIn, 0, i);
			}
			result[(count++)] = outfile;
		}
		return result;
	}

	public static String compressString(String compressMe, int length) {
		if (compressMe.length() <= length) {
			return compressMe;
		}
		StringBuilder result = new StringBuilder(compressMe.substring(0, length / 2));
		result.append("...");
		result.append(compressMe.substring(compressMe.length() - length / 2));
		return result.toString();
	}

	public static String compressPath(String path, int length) {
		if (path.length() <= length)
			return path;
		String[] pathParts;

		if (File.separator.equals("\\")) {
			pathParts = path.split("\\\\");
		} else {
			pathParts = path.split(File.separator);
		}
		int i = pathParts.length - 1;
		StringBuilder result = new StringBuilder(pathParts[(i--)]);
		while ((result.length() < length) && (i >= 0)) {
			result.insert(0, pathParts[(i--)] + File.separator);
		}
		result.insert(0, "..." + File.separator);
		return result.toString();
	}

	public static boolean isMac() {
		String lcOSName = System.getProperty("os.name").toLowerCase();
		return lcOSName.startsWith("mac os x");
	}

	public static Frame getFrameParent(Component c) {
		if ((c == null) || (c.getParent() == null)) {
			return null;
		}
		if ((c instanceof Frame)) {
			return (Frame) c;
		}
		Frame parentFrame = null;
		Component parent = c.getParent();
		if ((parent instanceof Frame)) {
			return (Frame) parent;
		}
		while ((parent.getParent() != null) && (parentFrame == null)) {
			parent = parent.getParent();
			if ((parent instanceof Frame)) {
				parentFrame = (Frame) parent;
			}
		}
		return parentFrame;
	}

	public static Dialog getDialogParent(Component c) {
		if ((c == null) || (c.getParent() == null)) {
			return null;
		}
		if ((c instanceof Dialog)) {
			return (Dialog) c;
		}
		Dialog parentDialog = null;
		Component parent = c.getParent();
		if ((parent instanceof Dialog)) {
			return (Dialog) parent;
		}
		while ((parent.getParent() != null) && (parentDialog == null)) {
			parent = parent.getParent();
			if ((parent instanceof Dialog)) {
				parentDialog = (Dialog) parent;
			}
		}
		return parentDialog;
	}

	public static DefaultMutableTreeNode createTreeFromThrowable(Throwable e) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(e);
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement ste : trace) {
			root.add(new DefaultMutableTreeNode(ste));
		}
		return root;
	}

	public static double stringMatchPercent(String s1, String s2, boolean ignoreCase) {
		if ((s1 == null) && (s2 == null)) {
			return 1.0D;
		}
		if ((s1 == null) && (s2 != null)) {
			return 0.0D;
		}
		if ((s2 == null) && (s1 != null)) {
			return 0.0D;
		}
		if (ignoreCase) {
			if (s1.equalsIgnoreCase(s2)) {
				return 1.0D;
			}
		} else if (s1.equals(s2)) {
			return 1.0D;
		}

		if (s1.length() == s2.length()) {
			return simpleStringMatch(s1, s2, ignoreCase);
		}
		int padCount;

		if (s1.length() < s2.length()) {
			String padMe = s1;
			int maxMatches = s2.length();
			padCount = s2.length() - s1.length();
		} else {
			String padMe = s2;
			int maxMatches = s1.length();
			padCount = s1.length() - s2.length();
		}
		double bestResult = 0.0D;

		return 0.0D;
	}

	public static double complexStringMatch(Character[] s1, String s2, boolean ignoreCase) {
		double matches = 0.0D;
		int nullCount = 0;
		Character[] arrayOfCharacter = s1;
		int j = s1.length;
		for (int i = 0; i < j; i++) {
			Character c = arrayOfCharacter[i];
			if (c == null) {
				nullCount++;
			}
		}
		int maxMatches = s2.length() - nullCount;
		for (int i = 0; i < s1.length; i++) {
			if (s1[i] != null) {
				if (ignoreCase) {
					if (Character.toLowerCase(s1[i].charValue()) == Character.toLowerCase(s2.charAt(i))) {
						matches += 1.0D;
					}
				} else if (s1[i].equals(new Character(s2.charAt(i)))) {
					matches += 1.0D;
				}
			}
		}

		return matches / maxMatches;
	}

	public static double simpleStringMatch(String s1, String s2, boolean ignoreCase) {
		if (s1.length() != s2.length()) {
			throw new InvalidParameterException("Both strings must have same length");
		}
		double matches = 0.0D;
		for (int i = 0; i < s1.length(); i++) {
			if (ignoreCase) {
				if (Character.toLowerCase(s1.charAt(i)) == Character.toLowerCase(s2.charAt(i))) {
					matches += 1.0D;
				}
			} else if (s1.charAt(i) == s2.charAt(i)) {
				matches += 1.0D;
			}
		}

		return matches / s1.length();
	}

	public static String stripHTML(String cleanMe) {
		return cleanMe.replaceAll("\\<.*?\\>", "");
	}

	public static File chooseDir(Component parent) {
		JFileChooser chooseDialog = new JFileChooser(getLastDir());
		chooseDialog.setFileSelectionMode(1);
		int returnVal = chooseDialog.showSaveDialog(parent);
		if (returnVal != 0) {
			return null;
		}
		setLastDir(chooseDialog.getSelectedFile());
		return chooseDialog.getSelectedFile();
	}

	public static long calcArraySize(double[] calcMe) {
		long result = 12 + 8 * calcMe.length;
		return result;
	}

	public static String convertNanoTimeToWords(long time) {
		return convertNanoTimeToWords(time, false);
	}

	public static String convertNanoTimeToWords(long time, boolean verbose) {
		if (time < 1000L) {
			if (!verbose) {
				return time + " ns";
			}
			return "<1s";
		}
		long myTime = time / 1000L;
		if (myTime < 1000L) {
			if (!verbose) {
				return time + " µs";
			}
			return "<1s";
		}
		myTime /= 1000L;
		if (myTime < 1000L) {
			if (!verbose) {
				return myTime + " ms";
			}
			return myTime / 1000L + "s";
		}
		myTime /= 1000L;
		if (myTime < 60L) {
			return myTime + " seconds";
		}
		long sec = myTime % 60L;
		myTime /= 60L;
		if (myTime < 60L) {
			if (!verbose) {
				if (sec >= 30L) {
					myTime += 1L;
				}
				return myTime + " minutes";
			}
			return myTime + " minutes " + sec + " seconds";
		}
		long min = myTime % 60L;
		myTime /= 60L;
		if (myTime < 24L) {
			if (!verbose) {
				if (min >= 30L) {
					myTime += 1L;
				}
				return myTime + " hours";
			}
			return myTime + " hours " + min + " minutes " + sec + "seconds";
		}
		long hour = myTime % 24L;
		myTime /= 24L;
		if (myTime < 7L) {
			if (!verbose) {
				if (hour >= 12L) {
					myTime += 1L;
				}
				return myTime + " days";
			}
			return myTime + " days " + hour + " hours " + min + " minutes " + sec + " seconds";
		}
		long day = myTime % 7L;
		myTime /= 7L;
		return myTime + " weeks " + day + " days " + hour + " hours " + min + " minutes " + sec + " seconds";
	}

	public static int randomInt(int min, int max) {
		double rand = Math.random() * (max - min) + min;
		return (int) Math.round(rand);
	}

	public static int showComboBoxDialog(Frame parent, String title, String message, Object[] vals) {
		final ChangeableInt result = new ChangeableInt(-1);
		final JComboBox box = new JComboBox(vals);
		final JDialog dialog = new JDialog(parent, title, true);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, 1));
		panel.add(new JLabel(message));
		panel.add(box);
		JButton okButton = new JButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				result.setValue(box.getSelectedIndex());
				dialog.dispose();
			}

		});
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}

		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(2);
		dialog.setVisible(true);
		return result.getValue();
	}

	public static class ChangeableInt {
		int value;

		public ChangeableInt(int value) {
			this.value = value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public String toString() {
			return value + "";
		}
	}

	public static <T> int searchArray(T target, T[] array, int hit) {
		int result = -1;
		int needed = hit;
		for (int i = 0; (i < array.length) && (result < 0); i++) {
			if (target.equals(array[i])) {
				needed--;
				if (needed < 0) {
					result = i;
				}
			}
		}
		return result;
	}

	public static class Link {
		private String href;
		private String text;

		public Link(String html) {
			Pattern hrefRegex = Pattern.compile("(?<=href=\")[^\"]*(?=\")", 160);
			Matcher hrefMatcher = hrefRegex.matcher(html);
			if (hrefMatcher.find()) {
				href = hrefMatcher.group();
			}
			Pattern regex = Pattern.compile("(?<=<a[^>]{1,10000}+>)[^<]*(?=</a>)", 226);

			Matcher regexMatcher = regex.matcher(html);
			if (regexMatcher.find()) {
				text = regexMatcher.group();
			}
		}

		public String getHref() {
			return href;
		}

		public String getText() {
			return text;
		}
	}

	public static String superClean(String cleanMe) {
		String firstResult = cleanMe;
		String result = firstResult.replaceAll("\\A[ ,./\\?!_-]|[ ,./\\?!_-]\\z", "");
		if (result.length() == firstResult.length()) {
			return result;
		}
		return superClean(result);
	}

	public static void startWatch() {
		startTime = Long.valueOf(System.currentTimeMillis());
	}

	public static long stopWatch() {
		if (startTime == null) {
			throw new IllegalStateException("startWatch() must be called before stopWatch()");
		}
		return System.currentTimeMillis() - startTime.longValue();
	}

	public static String removeExtendedChars(String cleanMe) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cleanMe.length(); i++) {
			int c = cleanMe.charAt(i);
			if (((c >= 32) && (c <= 126)) || (c == 181)) {
				result.append((char) c);
			}
		}

		return result.toString();
	}

	public static String condenseString(String shrinkMe) {
		return removeExtendedChars(shrinkMe.replaceAll("\\s+", " ").trim());
	}

	public static String escapeChar(String escapeMe, char c) {
		Character[] chars = new Character[1];
		chars[0] = Character.valueOf(c);
		return escapeChars(escapeMe, chars);
	}

	public static String escapeChars(String escapeMe, Character[] chars) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < escapeMe.length(); i++) {
			char c = escapeMe.charAt(i);
			if (isIn(Character.valueOf(c), chars)) {
				result.append('\\');
			}
			result.append(c);
		}
		return result.toString();
	}

	public static File mandatoryFileOpen() {
		return mandatoryFileOpen(null);
	}

	public static File mandatoryFileOpen(String extension) {
		File result;

		if (extension != null) {
			result = chooseFileToOpen(createFileFilter(extension, extension + " files"), null);
		} else {
			result = chooseFileToOpen();
		}
		if (result == null) {
			System.exit(0);
		}
		return result;
	}

	public static BufferedReader mandatoryFileReader() throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(mandatoryFileOpen()), "UTF-8"));
	}

	public static Color brighter(Color c) {
		float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hsbVals[2] += (1.0F - hsbVals[2]) / 2.0F;
		return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
	}

	public static Color darker(Color c) {
		float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hsbVals[2] /= 2.0F;
		return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
	}

	public static BufferedReader getUTF8Reader(File source) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"));
	}

	public static BufferedWriter getUTF8Writer(File dest) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), "UTF-8"));
	}

	public static String[] splitCSVLine(String line) {
		ArrayList<String> result = new ArrayList();
		try {
			Pattern regex = Pattern.compile("(?:,|^)([^\",]+|\"(?:[^\"]|\"\")*\")?", 194);

			Matcher regexMatcher = regex.matcher(line);
			while (regexMatcher.find()) {
				for (int i = 1; i <= regexMatcher.groupCount(); i++) {
					result.add(regexMatcher.group(i));
					// regexMatcher.group(i); regexMatcher.start(i);
					// regexMatcher.end(i);
				}

			}
		} catch (PatternSyntaxException localPatternSyntaxException) {
		}

		return result.toArray(new String[0]);
	}

	public static double log(double val, int base) {
		return Math.log(val) / Math.log(base);
	}

	public static String makeXMLElement(String name, String text, Map<String, String> attribs, int depth,
			boolean closed) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			result.append("  ");
		}
		result.append("<" + name);
		if (attribs != null) {
			Set<String> keys = attribs.keySet();
			for (String key : keys) {
				String value = attribs.get(key).replace("\"", "&quot;").replaceAll("&quot(?=[^;])", "&quot;");
				value = value.replace("'", "&apos;").replaceAll("&apos(?=[^;])", "&apos;");
				value = value.replace("<", "&lt;").replaceAll("&lt(?=[^;])", "&lt;");
				value = value.replace(">", "&gt;").replaceAll("&gt(?=[^;])", "&gt;");
				value = value.replaceAll("&amp(?=[^;])", "&amp;").replaceAll("&(?!gt;|lt;|apos;|quot;)", "&amp;");
				result.append(
						" " + key + "=\"" + attribs.get(key).replace("&", "&amp;").replace("\"", "&quot;") + "\"");
			}
		}
		if ((closed) && ((text == null) || ("".equals(text)))) {
			result.append(" />");
		} else if (closed) {
			result.append(">" + text + "</" + name + ">");
		} else {
			result.append(">");
		}
		return result.toString();
	}

	public static String getSizeString(long bytes) {
		String[] suffixes = { "Bytes", "KB", "MB", "GB", "TB", "PB" };
		double finalSize = bytes;
		int index = 0;
		while (finalSize > 1024.0D) {
			finalSize /= 1024.0D;
			index++;
		}
		return new DecimalFormat("#.##").format(finalSize) + " " + suffixes[index];
	}

	/**
	 * @author urmi save a jtable to file, default delimiter is tab
	 * @param table
	 * @return
	 */
	public static int saveJTabletofile(JTable table) {
		return saveJTabletofile(table, "\t");
	}
/**
 * 
 * @param table
 * @param delim delimiter
 * @return
 */
	public static int saveJTabletofile(JTable table, String delim) {
		int status = 1; // 0 success; 1 fail
		// export file as tab delimited .txt
		final File destination = Utils.chooseFileToSave(new GraphFileFilter(GraphFileFilter.TEXT), "txt",
				MetaOmGraph.getMainWindow(), true);
		if (destination == null)
			return 1;
		try {
			FileWriter fw = new FileWriter(destination);
			for (int col = 0; col < table.getColumnCount(); col++) {
				if (col == table.getColumnCount() - 1) {
					fw.write(table.getColumnName(col) + "\n");
				} else {
					fw.write(table.getColumnName(col) + delim);
				}

			}
			for (int row = 0; row < table.getRowCount(); row++) {
				String thisLine = "";
				for (int col = 0; col < table.getColumnCount(); col++) {
					// thisLine += table.getValueAt(row, col) + "\t";
					if (col == table.getColumnCount() - 1) {
						if (row == table.getRowCount() - 1) {
							// last row
							fw.write(table.getValueAt(row, col) + "");
						} else {
							fw.write(table.getValueAt(row, col) + "\n");
						}

					} else {
						fw.write(table.getValueAt(row, col) + delim);
					}
				}
				// thisLine += "\n";
				// fw.write(thisLine);
			}
			fw.close();
			JOptionPane.showMessageDialog(null, "File saved to:" + destination.getAbsolutePath(), "File saved",
					JOptionPane.INFORMATION_MESSAGE);
			status = 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error in saving file:" + destination.getAbsolutePath(), "Error",
					JOptionPane.ERROR_MESSAGE);
			status = 1;
		}
		return status;
	}
}
