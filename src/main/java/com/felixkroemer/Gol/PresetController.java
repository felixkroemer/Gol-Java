package com.felixkroemer.Gol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class PresetController {

	private static HashMap<String, Preset> presets = initPresets();

	private static HashMap<String, Preset> initPresets() {
		// http://www.conwaylife.com/wiki/Run_Length_Encoded
		// http://www.conwaylife.com/patterns/all.zip
		presets = new HashMap<String, Preset>();
		File folder = new File("Presets");
		int defCounter = 1;

		for (File file : folder.listFiles()) {
			try {
				Preset p = parseFile(file);
				if (p != null) {
					if (p.getName() == null) {
						p.setName("Default " + defCounter);
						defCounter++;
					}
					presets.put(p.getName(), p);
				}
			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		return presets;
	}

	private static Preset parseFile(File file) throws FileNotFoundException, IOException {
		Preset p = new Preset();
		FileReader fr = new FileReader("Presets/" + file.getName());
		BufferedReader br = new BufferedReader(fr);
		String line; // line in .rle file
		boolean boundsSet = false; // set to true after line with boundaries
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#N")) {
				p.setName(line.substring(3, line.length()));
			} else if (line.startsWith("#")) {
				continue;
			} else if (!boundsSet) { // first line defines pattern boundaries and game rules
				p.setBoundaries(getBoundaries(line));
				if (p.getBoundaries() == null) {
					br.close();
					return null;
				}
				boundsSet = true;
			} else {
				parseRows(line, p);
			}
		}
		br.close();
		return p;
	}

	private static int[] getBoundaries(String line) {
		int[] boundary = new int[2];
		line = line.toLowerCase();
		if (line.indexOf("x =") == -1) { // missing boundaries line
			return null;
		}
		boundary[0] = Integer.parseInt(line.substring(line.indexOf("x =") + 4, line.indexOf("y =") - 2));
		int IndexPosY = line.indexOf("y =") + 4;
		String StringPosY = "";
		try {
			do {
				StringPosY += line.charAt(IndexPosY);
				IndexPosY++;
			} while (line.charAt(IndexPosY) >= '0' && line.charAt(IndexPosY) <= '9');
		} catch (IndexOutOfBoundsException e) {
		}
		boundary[1] = Integer.parseInt(StringPosY);

		if (boundary[0] >= 200 || boundary[1] >= 200) {
			return null;
		}
		return boundary;
	}

	public static void parseRows(String line, Preset p) {
		boolean lastLine = false;
		String[] rows = line.split("\\$", -1);
		rows[0] = p.getOverflow() + rows[0];
		if (rows[rows.length - 1].endsWith("!")) { // last line
			lastLine = true;
			rows[rows.length - 1] = rows[rows.length - 1].substring(0, rows[rows.length - 1].length() - 1);
		}
		if (!lastLine) {
			p.setOverflow(rows[rows.length - 1]);
			rows[rows.length - 1] = "";
		}
		for (String row : rows) {
			parseRow(row, p);
		}
	}

	public static void parseRow(String row, Preset p) {
		String num = ""; // String representation of run_count
		int count; // Integer Representation of run_count
		int index = 0;
		for (int i = 0; i < row.length(); i++) {
			if (row.charAt(i) >= '0' && row.charAt(i) <= '9') {
				num = num + row.charAt(i);
				if (i == row.length() - 1) {
					p.setRowCount(p.getRowCount() + Integer.parseInt(num) - 1); // additional dead rows
					break;
				}
			} else {

				if (num != "") {
					index += Integer.parseInt(num);
				} else {
					index++;
				}

				if (row.charAt(i) == 'b') { // dead cells, num can be ignored and reset
					num = "";
					continue;
				} else { // living cells
					if (num.equals("")) { // if run_count is omitted
						count = 1;
					} else {
						count = Integer.parseInt(num);
					}
					// iterating over the number of live cells to fill the ArrayList
					for (int j = 0; j < count; j++) {
						int x = p.getRowCount();
						int y = index - count + j;
						p.addRow(new int[] { x, y });
					}
					num = "";
				}
			}
		}
		if (!row.equals("")) {
			p.setRowCount(p.getRowCount() + 1);
		}
	}

	public static Preset getPreset(String name) {
		return presets.get(name);
	}

	public static Set<String> getPresetNames() {
		return presets.keySet();
	}
}
