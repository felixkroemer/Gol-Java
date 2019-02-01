package com.felixkroemer.Gol;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;

public class Gol {
	Window w;
	Field f;
	boolean isRunning;
	HashMap<String, ArrayList<int[]>> presets;
	int sleepDuration;

	Gol() {
		this.presets = initPresets();
		this.sleepDuration = Config.SLEEP;
		this.isRunning = false;
		this.f = new Field(this);
		this.w = new Window(this);
	}

	public void run() {
		while (isRunning) {
			try {
				this.f.updateField();
				Thread.sleep(sleepDuration);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}

	public HashMap<String, ArrayList<int[]>> initPresets() {
		// http://www.conwaylife.com/wiki/Run_Length_Encoded
		// http://www.conwaylife.com/patterns/all.zip
		presets = new HashMap<String, ArrayList<int[]>>();
		File folder = new File("Presets");
		int defCounter = 1;

		fileLoop: for (File file : folder.listFiles()) {
			try {
				ArrayList<int[]> l = new ArrayList<int[]>();
				FileReader fr = new FileReader("Presets/" + file.getName());
				BufferedReader br = new BufferedReader(fr);
				String line; // line in .rle file
				boolean boundaries = false; // set to true after line with boundaries
				boolean lastLine = false; // set to true if line ends with '!'
				String overflow = ""; // characters at end of line after last '$'
				String name = "Default " + defCounter; // default name if #N is missing
				int rowCount = 0;
				int posX = 0;
				int posY = 0;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("#N")) {
						name = line.substring(3, line.length());
					} else if (line.startsWith("#")) {
						continue;
					} else if (boundaries == false) { // first line defines pattern boundaries and game rules
						boundaries = true;
						line = line.toLowerCase();
						if (line.indexOf("x =") == -1) { // missing boundaries line
							continue fileLoop;
						}
						posX = Integer.parseInt(line.substring(line.indexOf("x =") + 4, line.indexOf("y =") - 2));
						int IndexPosY = line.indexOf("y =") + 4;
						String StringPosY = "";
						try {
							do {
								StringPosY += line.charAt(IndexPosY);
								IndexPosY++;
							} while (line.charAt(IndexPosY) >= '0' && line.charAt(IndexPosY) <= '9');
						} catch (IndexOutOfBoundsException e) {
						}
						posY = Integer.parseInt(StringPosY);

						if (posX >= Config.COLUMNS || posY >= Config.ROWS) {
							continue fileLoop;
						}
					}

					else {
						String[] lines = line.split("\\$", -1);
						lines[0] = overflow + lines[0];
						if (lines[lines.length - 1].endsWith("!")) { // last line
							lastLine = true;
							String lastString = lines[lines.length - 1];
							lines[lines.length - 1] = lastString.substring(0, lastString.length() - 1);
						}
						if (!lastLine) {
							overflow = lines[lines.length - 1];
							lines[lines.length - 1] = "";
						}
						for (String row : lines) {
							String num = ""; // String representation of run_count
							int count; // Integer Representation of run_count
							int index = 0;
							for (int i = 0; i < row.length(); i++) {
								if (row.charAt(i) >= '0' && row.charAt(i) <= '9') {
									num = num + row.charAt(i);
									if (i == row.length() - 1) {
										rowCount += Integer.parseInt(num) - 1;
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
											int x = rowCount;
											int y = index - count + j;
											int[] xy = { x + (Config.ROWS - posX) / 2,
													y + (Config.COLUMNS - posY) / 2 };
											l.add(xy);
										}
										num = "";
									}
								}
							}
							if (!row.equals("")) {
								rowCount++;
							}
						}
					}
				}
				br.close();
				presets.put(name, l);
				if (name.startsWith("Default")) {
					defCounter++;
				}
			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		return presets;
	}

	public static void main(String[] args) {
		Gol g = new Gol();
	}
}
