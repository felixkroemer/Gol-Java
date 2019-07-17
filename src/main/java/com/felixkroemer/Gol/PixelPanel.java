package com.felixkroemer.Gol;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

public class PixelPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Vector<Vector<Boolean>> cells;
	private boolean editable;
	private int cols, rows, cellDim;

	public PixelPanel() {
		this.initPanel(Config.COLUMNS, Config.ROWS);
		this.editable = true;

		this.addMouseListener(new CellListener());

		this.setFocusable(true);
		this.setPreferredSize(new Dimension(Config.ROWS * Config.CELL_DIM, Config.COLUMNS * Config.CELL_DIM));
		this.setBackground(Config.C2);
	}

	public void initPanel(int rows, int cols) {
		this.rows = Config.ROWS;
		this.cols = Config.COLUMNS;
		this.cellDim = Config.CELL_DIM;
		this.cells = new Vector<Vector<Boolean>>();
		for (int i = 0; i < cols; i++) {
			this.cells.add(new Vector<Boolean>());
			for (int j = 0; j < rows; j++) {
				this.cells.get(i).addElement(false);
			}
		}
		this.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int dim = this.cellDim;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				if (this.cells.get(i).get(j)) {
					g2.setColor(Config.C1);
				} else {
					g2.setColor(Config.C2);
				}
				g2.fillRect(j * dim, i * dim, dim, dim);
			}
		}
	}

	public void swap(int i, int j) {
		try {
			if (this.cells.get(i).get(j)) {
				this.cells.get(i).set(j, false);
			} else {
				this.cells.get(i).set(j, true);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		this.repaint();
	}

	public void swapAll(List<int[]> l) {
		for (int[] a : l) {
			try {
				if (this.cells.get(a[0]).get(a[1])) {
					this.cells.get(a[0]).set(a[1], false);
				} else {
					this.cells.get(a[0]).set(a[1], true);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
		this.repaint();
	}

	public boolean checkZoomOut(int a) {
		if (this.cellDim + a <= 3) {
			return false;
		} else {
			return true;
		}
	}

	public int zoomGap(int a) {
		long newDim = Math.round(this.getWidth() / ((this.cellDim + a) * 1.0));
		return (int) (this.cols - (newDim % 2 == 0 ? newDim : newDim-1));
	}

	public void zoom(int a, boolean[][] updatedCells) {
		int diff = this.zoomGap(a);
		this.cellDim += a;

		for (int i = 0; i < Math.abs(diff) / 2; i++) {
			if (a > 0) {
				this.cells.remove(0);
				this.cells.remove(this.cells.size() - 1);
			} else {
				Vector<Boolean> v1 = new Vector<Boolean>();
				for (int k = 0; k < this.rows; k++) {
					v1.add(false);
				}
				this.cells.add(v1);
				this.cells.add(0, new Vector<Boolean>(v1));
			}
		}
		for (int i = 0; i < this.cells.size(); i++) {
			for (int j = 0; j < Math.abs(diff) / 2; j++) {
				if (a > 0) {
					this.cells.get(i).remove(0);
					this.cells.get(i).remove(this.cells.get(i).size() - 1);
				} else {
					this.cells.get(i).add(0, false);
					this.cells.get(i).add(false);
				}
			}
		}
		if (a < 0) {
			for (int i = 0; i < this.cells.size(); i++) {
				for (int j = 0; j < this.cells.get(0).size(); j++) {
					this.cells.get(i).set(j, updatedCells[i][j]);
				}
			}
		}
		this.cols -= diff;
		this.rows -= diff;

		this.repaint();
	}

	public void reset() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.cells.get(i).set(j, false);
			}
		}
		this.repaint();
	}

	public int getRows() {
		return this.rows;
	}

	public int getCols() {
		return this.cols;
	}

	public void setEditable(boolean b) {
		this.editable = b;
	}

	public Vector<Vector<Boolean>> getCells() {
		return this.cells;
	}

	public int[] getCellCoordinates(int x, int y) {
		return new int[] { x / this.cellDim, y / this.cellDim };
	}

	class CellListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			int[] coords = PixelPanel.this.getCellCoordinates(e.getX(), e.getY());
			if (PixelPanel.this.editable) {
				PixelPanel.this.swap(coords[1], coords[0]);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			PixelPanel.this.requestFocusInWindow();
		}
	}
}
