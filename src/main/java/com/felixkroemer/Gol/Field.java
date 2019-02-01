package com.felixkroemer.Gol;

public class Field {

	private Gol game;
	private boolean[][] inner;
	// is true if cell is alive
	private int[][] count;
	// is reset each generation and counts the number of live neighbors each cell
	private int aS;
	// difference between the size of the GUI grid and the inner representation of the field
	private int[][] check = { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, -1 }, { 1, -1 } };

	Field(Gol game) {
		this.game = game;
		this.inner = new boolean[Config.ROWS][Config.COLUMNS];
		this.count = new int[Config.ROWS][Config.COLUMNS];
		this.aS = 0;
		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {
				inner[i][j] = false;
				count[i][j] = 0;
			}
		}
	}

	public void setLife(int i, int j, boolean b) {
		i = i + aS;
		j = j + aS;
		inner[i][j] = b;
	}

	public boolean getLife(int i, int j, boolean adjust) { // adjusts for aS of called from within Window class
		if (adjust) {
			i += aS;
			j += aS;
		}
		return inner[i][j];
	}

	public void updateField() { // updates inner and calls Window.swap() if a cell changes its state

		game.w.addGen();

		boolean grow = false;
		for (int i = 0; i < inner.length; i++) {
			if (getLife(i, 0, false) || getLife(0, i, false) || getLife(inner.length - 1, i, false)
					|| getLife(i, inner.length - 1, false)) {
				grow = true;
				break;
			}
		}
		if (grow) {
			growField();
		}

		resetCount();
		updateInner();
		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {

				boolean prev = getLife(i, j, false);
				boolean b;

				if (count[i][j] < 2) {
					inner[i][j] = false;
					b = false;
				} else if (count[i][j] == 2 && !getLife(i, j, false)) {
					inner[i][j] = false;
					b = false;
				} else if (count[i][j] == 3 || count[i][j] == 2) {
					inner[i][j] = true;
					b = true;
				} else {
					inner[i][j] = false;
					b = false;
				}

				// checks if a cell that changed its state is within the GUI grid
				if (prev != b && i >= aS && i <= inner.length - aS - 1 && j >= aS && j <= inner[0].length - 1 - aS)
					game.w.swap(i - aS, j - aS);
			}
		}
	}

	public void updateInner() {
		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {
				if (getLife(i, j, false)) {
					for (int k = 0; k < check.length; k++) {
						if (i + check[k][0] >= 0 && i + check[k][0] <= inner.length - 1 && j + check[k][1] >= 0
								&& j + check[k][1] <= inner[0].length - 1) {
							count[i + check[k][0]][j + check[k][1]] += 1;
						}
					}
				}
			}
		}
	}

	// extends inner and count by 20 in each direction
	private void growField() {
		aS += 20;
		boolean[][] newInner = new boolean[inner.length + 40][inner[0].length + 40];
		int[][] newCount = new int[inner.length + 40][inner[0].length + 40];
		for (int i = 0; i < newInner.length; i++) {
			for (int j = 0; j < newInner[0].length; j++) {
				newInner[i][j] = false;
				newCount[i][j] = 0;
			}
		}

		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {
				newInner[i + 20][j + 20] = inner[i][j];
				newCount[i + 20][j + 20] = count[i][j];
			}
		}
		inner = newInner;
		count = newCount;
	}

	public void resetCount() {
		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {
				count[i][j] = 0;
			}
		}
	}

	public void resetInner() {
		for (int i = 0; i < inner.length; i++) {
			for (int j = 0; j < inner[0].length; j++) {
				inner[i][j] = false;
			}
		}
	}

}
