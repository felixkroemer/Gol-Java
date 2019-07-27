package com.felixkroemer.Gol;

import java.util.ArrayList;

public class Preset {
	private ArrayList<int[]> coords;
	private int[] boundaries;
	private String overflow;
	private int rowCount;
	private String name;

	public Preset() {
		this.overflow = "";
		this.rowCount = 0;
	}

	public ArrayList<int[]> getCoords() {
		return this.coords;
	}

	public void setBoundaries(int[] boundaries) {
		this.boundaries = boundaries;
	}

	public int[] getBoundaries() {
		return this.boundaries;
	}

	public void addRow(int[] r) {
		if (this.coords == null) {
			this.coords = new ArrayList<int[]>();
		}
		this.coords.add(r);
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public String getOverflow() {
		return overflow;
	}

	public void setOverflow(String overflow) {
		this.overflow = overflow;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
