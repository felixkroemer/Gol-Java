package com.felixkroemer.Gol;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.event.*;
import java.util.ArrayList;

public class Window extends JFrame {

	private static final long serialVersionUID = 1L;
	private Gol game;
	private Container c;

	// center components
	private JPanel cellPanel;
	private JPanel[][] ar;
	private int[] origin = new int[] { 0, 0 };

	// menu components
	private JToggleButton startButton;
	private JButton stopButton;
	private JButton stepButton;
	private JButton resetButton;
	private JMenu presets;
	private JMenuItem[] presetList;
	private JLabel sleepLabel;
	private JSlider sleepSlider;
	private JTextField sleepTF;

	// bottomPanel components
	private JPanel bottomPanel;
	private JLabel genLabel;
	private int genCounter;
	private JLabel presetLabel;
	private JButton[] arrows;
	private DirectionListener dL;

	Window(Gol game) {

		this.game = game;
		c = this.getContentPane();

		this.initMenuBar();

		// setting up center components
		cellPanel = new JPanel();
		cellPanel.setLayout(new GridLayout(Config.ROWS, Config.COLUMNS, 0, 0));
		this.ar = fillCellPanel(Config.ROWS, Config.COLUMNS);
		c.addKeyListener(new DirectionKeyListener());

		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		cellPanel.setCursor(blankCursor);
		c.setFocusable(true);
		c.add(cellPanel, BorderLayout.CENTER);

		this.initBottomPanel();

		loadInitialPreset();

		this.setResizable(false);
		this.pack();

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JPanel[][] fillCellPanel(int rows, int cols) {
		JPanel[][] ar = new JPanel[rows][cols];
		CellListener cl = new CellListener();
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				ar[i][j] = new JPanel();
				ar[i][j].setPreferredSize(new Dimension(Config.CELL_DIM, Config.CELL_DIM));
				ar[i][j].setBackground(Config.C2);
				ar[i][j].addMouseListener(cl);
				cellPanel.add(ar[i][j]);
			}
		}
		return ar;
	}

	private void moveCellPanel(int dir) {
		if (game.getActive()) {
			return;
		}
		scanPanels();
		int steps = 4;
		int xOffset = 0;
		int yOffset = 0;
		switch (dir) {
		case 0:
			yOffset = -steps;
			break;
		case 1:
			yOffset = steps;
			break;
		case 2:
			xOffset = -steps;
			break;
		case 3:
			xOffset = steps;
			break;
		}
		origin[0] += yOffset;
		origin[1] += xOffset;
		boolean[][] b = new boolean[ar.length][ar[0].length];
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				try {
					if (ar[i + yOffset][j + xOffset].getBackground() == Config.C1) {
						b[i][j] = true;
					} else {
						b[i][j] = false;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					if (game.getField().getLife(i + origin[0], j + origin[1], true)) {
						b[i][j] = true;
					} else {
						b[i][j] = false;
					}
				}
			}
		}
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				if (b[i][j]) {
					ar[i][j].setBackground(Config.C1);
				} else {
					ar[i][j].setBackground(Config.C2);
				}
			}
		}
	}

	public void addGen() {
		genCounter++;
		genLabel.setText("Generation " + genCounter);
	}

	public void resetGen() {
		genCounter = 0;
		genLabel.setText("Generation " + genCounter);
	}

	public void swap(int i, int j) {
		try {
			if (ar[i - origin[0]][j - origin[1]].getBackground() == Config.C1) {
				ar[i - origin[0]][j - origin[1]].setBackground(Config.C2);
			} else {
				ar[i - origin[0]][j - origin[1]].setBackground(Config.C1);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
	}

	public void resetWindow() {
		game.stop();
		game.getField().resetInner();
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				ar[i][j].setBackground(Config.C2);
			}
		}
	}

	public void initMenuBar() {
		JMenuBar menu = new JMenuBar();

		startButton = new JToggleButton("Start");
		StartButtonListener startListener = new StartButtonListener();
		startButton.addActionListener(startListener);

		stopButton = new JButton("Stop");
		StopButtonListener stopListener = new StopButtonListener();
		stopButton.addActionListener(stopListener);

		stepButton = new JButton("Step");
		StepButtonListener stepListener = new StepButtonListener();
		stepButton.addActionListener(stepListener);

		resetButton = new JButton("Reset");
		ResetButtonListener resetListener = new ResetButtonListener();
		resetButton.addActionListener(resetListener);

		presets = new JMenu("Presets ↓");
		this.initPresets();

		sleepLabel = new JLabel("Timespan");

		sleepSlider = new JSlider(0, 400, Config.SLEEP);
		sleepSlider.setOpaque(false);
		sleepSlider.setMaximumSize(new Dimension(100, 25));
		SleepSliderListener ssl = new SleepSliderListener();
		sleepSlider.addChangeListener(ssl);

		sleepTF = new JTextField(Integer.toString(Config.SLEEP), 3);
		sleepTF.setHorizontalAlignment(SwingConstants.RIGHT);
		SleepListener sL = new SleepListener();
		sleepTF.addActionListener(sL);

		setJMenuBar(menu);
		menu.add(Box.createRigidArea(new Dimension(10, 25)));
		menu.add(startButton);
		menu.add(Box.createRigidArea(new Dimension(10, 25)));
		menu.add(stopButton);
		menu.add(Box.createRigidArea(new Dimension(10, 25)));
		menu.add(stepButton);
		menu.add(Box.createRigidArea(new Dimension(10, 25)));
		menu.add(resetButton);
		menu.add(Box.createRigidArea(new Dimension(10, 25)));
		menu.add(presets);
		menu.add(Box.createHorizontalGlue());
		menu.add(sleepLabel);
		menu.add(sleepSlider);
		menu.add(sleepTF);
		menu.add(new JLabel(" ms "));
		sleepTF.setMaximumSize(new Dimension(5, 25));
	}

	public void initBottomPanel() {
		bottomPanel = new JPanel();
		bottomPanel.setPreferredSize(new Dimension(c.getWidth(), 25));
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS)); // FlowLayout());
		bottomPanel.setBackground(new Color(238, 238, 238));
		c.add(bottomPanel, BorderLayout.SOUTH);

		genCounter = 0;
		genLabel = new JLabel("Generation " + genCounter);
		bottomPanel.add(Box.createRigidArea(new Dimension(10, 25)));
		bottomPanel.add(genLabel);

		presetLabel = new JLabel("No Pattern selected");
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(presetLabel);
		bottomPanel.add(Box.createHorizontalGlue());

		arrows = new JButton[4];

		for (int i = 0; i < arrows.length; i++) {
			String dir = "";
			switch (i) {
			case 0:
				dir = "▲";
				break;
			case 1:
				dir = "▼";
				break;
			case 2:
				dir = "◀";
				break;
			case 3:
				dir = "▶";
				break;
			}

			arrows[i] = new JButton(dir);
			dL = new DirectionListener();
			arrows[i].addActionListener(dL);
			bottomPanel.add(arrows[i]);
			arrows[i].setMargin(new Insets(0, 0, 0, 0));
			arrows[i].setPreferredSize(new Dimension(25, 25));
			arrows[i].setBackground(new Color(238, 238, 238));
			arrows[i].setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
	}

	public void initPresets() {
		presetList = new JMenuItem[game.getPresets().size()];
		PresetListener pl = new PresetListener();
		int i = 0;
		for (String key : game.getPresets().keySet()) {
			presetList[i] = new JMenuItem(key);
			presetList[i].addActionListener(pl);
			presets.add(presetList[i]);
			i++;
		}
	}

	public void loadInitialPreset() {
		if (presetList.length > 0) {
			int r = (int) (Math.random() * presetList.length);
			loadPreset(presetList[r]);
		}
	}

	class CellListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JPanel p = (JPanel) e.getSource();
			if (!game.getRunning()) {
				if (p.getBackground() != Config.C1) {
					p.setBackground(Config.C1);
				} else {
					p.setBackground(Config.C2);
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
			JPanel p = (JPanel) e.getSource();
			int[] pos = findPanel(p);
			paintCursor(pos[0], pos[1], true);
			c.requestFocusInWindow();
		}

		public void mouseExited(MouseEvent e) {
			JPanel p = (JPanel) e.getSource();
			int[] pos = findPanel(p);
			paintCursor(pos[0], pos[1], false);
		}
	}

	public int[] findPanel(JPanel p) {
		int x = 0;
		int y = 0;
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				if (ar[i][j] == p) {
					x = i;
					y = j;
				}
			}
		}
		int[] pos = { x, y };
		return pos;
	}

	public void paintCursor(int x, int y, boolean enter) {
		Color p = enter ? blendColor(Config.C1, Config.C2, 0.8) : Config.C2;
		int width = (int) ((Config.ROWS * 1.0) / 40);
		if (width < 2) {
			width = 2;
		}
		for (int i = -width; i <= width; i++) {
			for (int j = -width; j <= width; j++) {
				int factor = Math.abs(i) + Math.abs(j);
				if (factor >= width * 3) {
					continue;
				}
				try {
					if (ar[x + i][y + j].getBackground() != Config.C1) {
						if (enter) {
							Color rgb = blendColor(Config.C1, Config.C2, 0.8 - (factor / 5.0));
							ar[x + i][y + j].setBackground(rgb);

						} else {
							ar[x + i][y + j].setBackground(p);
						}
					}
				} catch (ArrayIndexOutOfBoundsException f) {
					continue;
				}
			}
		}
	}

	public Color blendColor(Color c1, Color c2, double l) {
		int[] rgb = new int[3];
		rgb[0] = (int) (c1.getRed() * l + c2.getRed() * (1 - l));
		rgb[1] = (int) (c1.getGreen() * l + c2.getGreen() * (1 - l));
		rgb[2] = (int) (c1.getBlue() * l + c2.getBlue() * (1 - l));
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

	public void scanPanels() {
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				Color col = ar[i][j].getBackground();
				if (col == Config.C1) {
					game.getField().setLife(i + origin[0], j + origin[1], true);
				} else {
					game.getField().setLife(i + origin[0], j + origin[1], false);
				}
			}
		}
	}

	class StartButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			scanPanels();
			game.start();
		}
	}

	class StopButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			game.stop();
		}
	}

	class StepButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			scanPanels();
			game.stop();
			game.getField().updateField();
		}
	}

	class ResetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			resetWindow();
			resetGen();
			presetLabel.setText("No Pattern selected");
		}
	}

	class SleepListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JTextField t = (JTextField) e.getSource();
			String text = t.getText();
			if (!text.matches("(\\d)*")) {
				t.setBackground(Color.RED);
			} else {
				t.setBackground(Color.WHITE);
				if (Integer.parseInt(text) > sleepSlider.getMaximum()) {
					sleepSlider.setValue(sleepSlider.getMaximum());
				} else {
					sleepSlider.setValue(Integer.parseInt(text));
				}
				t.setText(text);
				game.setSleep(Integer.parseInt(text));
			}
		}
	}

	class SleepSliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider s = (JSlider) e.getSource();
			game.setSleep(s.getValue());
			sleepTF.setText(String.valueOf(s.getValue()));
		}
	}

	class PresetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JMenuItem m = (JMenuItem) e.getSource();
			loadPreset(m);
		}
	}

	public void loadPreset(JMenuItem m) {
		resetGen();
		startButton.setSelected(false);
		String text = m.getText();
		for (String key : game.getPresets().keySet()) {
			if (key.equals(text)) {
				resetWindow();

				ArrayList<int[]> a = game.getPresets().get(key);
				for (int[] r : a) {
					ar[r[0]][r[1]].setBackground(Config.C1);
				}
			}
		}
		presetLabel.setText(m.getText());
	}

	class DirectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			game.pauseThread();
			JButton b = (JButton) e.getSource();
			switch (b.getText()) {
			case "▲":
				moveCellPanel(0);
				break;
			case "▼":
				moveCellPanel(1);
				break;
			case "◀":
				moveCellPanel(2);
				break;
			case "▶":
				moveCellPanel(3);
				break;
			}
			synchronized (game.pauseLock) {
				game.resumeThread();
			}
		}
	}

	class DirectionKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int id = e.getKeyCode();
			int dir = 0;
			switch (id) {
			case KeyEvent.VK_UP:
				dir = 0;
				break;
			case KeyEvent.VK_DOWN:
				dir = 1;
				break;
			case KeyEvent.VK_LEFT:
				dir = 2;
				break;
			case KeyEvent.VK_RIGHT:
				dir = 3;
				break;
			default:
				return;
			}
			Window.this.dL.actionPerformed(new ActionEvent(Window.this.arrows[dir], 0, ""));
		}
	}
}
