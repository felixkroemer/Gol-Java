package com.felixkroemer.Gol;

public class RunThread implements Runnable {

	Gol game;
	boolean paused;
	boolean isActive;

	RunThread(Gol g) {
		game = g;
		this.paused = false;
		this.isActive = false;
	}

	public void run() {
		while (game.testRunning()) {
			synchronized (game.pauseLock) {
				if (!game.testRunning()) {
					break;
				}
				if (paused) {
					try {
						game.pauseLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			try {
				this.isActive = true;
				game.getField().updateField();
				this.isActive = false;
				Thread.sleep(game.getSleep());
			} catch (InterruptedException e) {
			}
		}
	}

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		game.pauseLock.notifyAll();
		paused = false;
	}
}
