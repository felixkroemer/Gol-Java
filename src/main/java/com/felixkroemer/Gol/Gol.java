package com.felixkroemer.Gol;

import java.util.List;

public class Gol {
	private Window w;
	private Field f;
	private boolean isRunning;
	private int sleepDuration;
	private RunThread t;
	public final Object pauseLock = new Object();

	Gol() {
		this.sleepDuration = Config.SLEEP;
		this.isRunning = false;
		this.f = new Field(this);
		this.w = new Window(this);
		this.t = new RunThread(this);
	}

	public Field getField() {
		return this.f;
	}

	public void start() {
		this.isRunning = true;
		new Thread(this.t).start();
	}

	public void stop() {
		this.isRunning = false;
	}

	public void pauseThread() {
		this.t.pause();
	}

	public void resumeThread() {
		this.t.resume();
	}

	public boolean getActive() {
		return this.t.isActive;
	}

	public boolean getRunning() {
		return this.isRunning;
	}

	public int getSleep() {
		return this.sleepDuration;
	}

	public void setSleep(int d) {
		this.sleepDuration = d;
	}

	public void addGen() {
		this.w.addGen();
	}

	public boolean getLife(int x, int y) {
		int aS = this.f.getAdditionalSpace();
		x = x + aS;
		y = y + aS;
		return this.f.getLife(x, y);
	}

	public void swap(List<int[]> l) {
		this.w.swap(l);
	}

	public static void main(String[] args) {
		Gol g = new Gol();
	}
}
