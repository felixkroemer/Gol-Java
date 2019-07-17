package com.felixkroemer.Gol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Window extends JFrame {

	private static final long serialVersionUID = 1L;
	private Gol game;
	private Container c;

	// center components
	private PixelPanel p;
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
		this.p = new PixelPanel();
		this.p.addKeyListener(new DirectionKeyListener());
		this.p.addMouseMotionListener(new MotionListener());
		this.p.addMouseWheelListener(new ZoomListener());

		c.setFocusable(true);
		c.add(this.p, BorderLayout.CENTER);

		this.initBottomPanel();

		loadInitialPreset();

		this.setResizable(false);
		this.pack();

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void moveCellPanel(int xOffset, int yOffset) {
		if (xOffset == 0 && yOffset == 0) {
			return;
		}
		game.pauseThread();
		while (game.getActive()) { // wait until Field.updateField returns and Thread sleeps or is suspended
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		this.scanPixelPanel();
		origin[0] += yOffset;
		origin[1] += xOffset;
		boolean[][] b = new boolean[this.p.getCols()][this.p.getRows()];
		Vector<Vector<Boolean>> cells = this.p.getCells();
		for (int i = 0; i < this.p.getRows(); i++) {
			for (int j = 0; j < this.p.getCols(); j++) {
				try {
					if (cells.get(i + yOffset).get(j + xOffset)) {
						b[i][j] = true;
					} else {
						b[i][j] = false;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					if (game.getLife(i + origin[0], j + origin[1])) {
						b[i][j] = true;
					} else {
						b[i][j] = false;
					}
				}
			}
		}
		this.p.reset();
		for (int i = 0; i < this.p.getRows(); i++) {
			for (int j = 0; j < this.p.getCols(); j++) {
				if (b[i][j]) {
					this.p.swap(i, j);
				}
			}
		}
		synchronized (game.pauseLock) {
			game.resumeThread();
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
		this.p.swap(i - this.origin[0], j - this.origin[1]);
	}

	public void swap(List<int[]> l) {
		for (int[] a : l) {
			a[0] = a[0] - this.origin[0];
			a[1] = a[1] - this.origin[1];
		}
		this.p.swapAll(l);
	}

	public void resetWindow() {
		game.stop();
		game.getField().resetInner();
		this.origin[0] = 0;
		this.origin[1] = 0;
		this.p.initPanel(Config.COLUMNS, Config.ROWS);
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

	public void scanPixelPanel() {
		Vector<Vector<Boolean>> cells = this.p.getCells();
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.get(0).size(); j++) {
				if (cells.get(i).get(j)) {
					game.getField().setLife(i + origin[0], j + origin[1], true);
				} else {
					game.getField().setLife(i + origin[0], j + origin[1], false);
				}
			}
		}
	}

	class StartButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			scanPixelPanel();
			p.setEditable(false);
			game.start();
		}
	}

	class StopButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			p.setEditable(true);
			game.stop();
		}
	}

	class StepButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			p.setEditable(true);
			scanPixelPanel();
			game.stop();
			game.getField().updateField();
		}
	}

	public void zoom(int a) {
		if(a<0 && !this.p.checkZoomOut(a)) {
			return;
		}
		this.game.pauseThread();
		while (game.getActive()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		this.scanPixelPanel();
		boolean[][] cells = null;
		int gap = this.p.zoomGap(a);
		if (a < 0) {
			cells = new boolean[this.p.getCols() - gap][this.p.getRows() - gap];
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					cells[i][j] = this.game.getLife(i + origin[0] + gap / 2, j + origin[1] + gap / 2);
				}
			}
		}
		this.p.zoom(a, cells);
		this.origin[0] += gap / 2;
		this.origin[1] += gap / 2;
		synchronized (game.pauseLock) {
			this.game.resumeThread();
		}
	}

	class ResetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			startButton.setSelected(false);
			Window.this.p.setEditable(true);
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
		this.resetGen();
		this.resetWindow();
		this.p.setEditable(true);
		startButton.setSelected(false);
		String text = m.getText();
		for (String key : game.getPresets().keySet()) {
			if (key.equals(text)) {
				resetWindow();
				ArrayList<int[]> a = game.getPresets().get(key);
				this.p.swapAll(a);
			}
		}
		presetLabel.setText(m.getText());
	}

	class DirectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			switch (b.getText()) {
			case "▲":
				moveCellPanel(0, 4);
				break;
			case "▼":
				moveCellPanel(0, -4);
				break;
			case "◀":
				moveCellPanel(4, 0);
				break;
			case "▶":
				moveCellPanel(-4, 0);
				break;
			}
		}
	}

	class DirectionKeyListener extends KeyAdapter {

		@Override
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

	class MotionListener extends MouseMotionAdapter {
		int[] prev;
		long time;

		@Override
		public void mouseDragged(MouseEvent e) {
			int[] curr = Window.this.p.getCellCoordinates(e.getX(), e.getY());
			if (time == 0) {
				time = e.getWhen();
			}
			if (e.getWhen() - time >= 300) {
				prev = null;
				time = e.getWhen();
			}

			if (prev == null) {
				prev = curr;
			} else {
				int xDiff = prev[0] - curr[0];
				int yDiff = prev[1] - curr[1];
				if (xDiff != 0 || yDiff != 0) {
					Window.this.moveCellPanel(xDiff, yDiff);
					prev = curr;
				}
			}
		}
	}

	class ZoomListener implements MouseWheelListener {

		public void mouseWheelMoved(MouseWheelEvent e) {
			if(e.getWheelRotation() == 0) {
				return;
			}
			Window.this.zoom(e.getWheelRotation());
		}

	}

}
