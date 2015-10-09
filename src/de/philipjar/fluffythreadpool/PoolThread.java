/*
	Thread Pool implementation.
    Copyright (C) 2015  Philip S. Lindner

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.philipjar.fluffythreadpool;

import java.util.concurrent.atomic.AtomicBoolean;

import de.philipjar.fluffythreadpool.Pool.ThreadStates;

public class PoolThread extends Thread {
	
	private final int ID;
	private Pool daddy;
	
	private Runnable runnable = null;
	
	private AtomicBoolean idleFlag = new AtomicBoolean(false);
	private boolean preRun = true;
	private boolean shutdown = false;
	
	/**
	 * Pool Thread Constructor.
	 * May only be called by a ThreadPool instance.
	 * 
	 * @param daddy
	 * @param ID
	 */
	protected PoolThread(Pool daddy, int ID) {
		super();
		this.ID = ID;
		this.daddy = daddy;
		daddy.myStateChanged(ID, ThreadStates.PRERUN);
	}
	
	@Override
	public void run() {
		while (!shutdown) {
			daddy.myStateChanged(ID, ThreadStates.EXECUTING);
			daddy.printOut("pt - " + String.valueOf(ID) + " is executing now.");
			if (runnable != null) {
				runnable.run();
			} else {
				if (preRun) {
					preRun = false;
					enterIdle();
					continue;
				}
				throw new PoolException("Thread attempted to run null!");
			}
			enterIdle();
		}
		daddy.myStateChanged(ID, ThreadStates.DEAD);
	}
	
	/**
	 * Inserts a runnable into this Thread.
	 * If this is tried during executing a runnable, a PoolException is being thrown.
	 * 
	 * @param runnable
	 */
	private void plugRunnable(Runnable runnable) {
		if (idleFlag.get() || preRun) {
			this.runnable = runnable;
		} else {
			throw new PoolException("Attempted to hotplug runnable into running Thread!");
		}
	}
	
	/**
	 * Plugs new Runnable into Thread and starts execution.
	 * @param runnable
	 */
	protected void exec(Runnable runnable) {
		if (idleFlag.get() || preRun) {
			plugRunnable(runnable);
			leaveIdle();
		} else {
			throw new PoolException("Thread already executing runnable!");
		}
	}
	
	/**
	 * Shuts down this Thread.
	 */
	protected void shutdown() {
		daddy.printOut("pt - Thread " + String.valueOf(ID) + " shutting down now.");
		shutdown = true;
		if(idleFlag.get())
			leaveIdle();
	}
	
	protected int getPTID() {
		return ID;
	}
	
	/**
	 * Enters Idle State.
	 */
	private void enterIdle() {
		if (idleFlag.get() || shutdown)
			return;
		daddy.printOut("pt - " + String.valueOf(ID) + " is Idle now.");
		idleFlag.set(true);
		daddy.myStateChanged(ID, ThreadStates.IDLE);
		idle();
	}
	
	/**
	 * Leaves Idle State and starts executing runnable.
	 */
	private void leaveIdle() {
		idleFlag.set(false);
		synchronized (idleFlag) {
			idleFlag.notify();
		}
	}
	
	/**
	 * The Idle State.
	 */
	private void idle() {
		synchronized (idleFlag) {
			while (idleFlag.get()) {
				try {
					idleFlag.wait();
				} catch (InterruptedException e) {
					throw new PoolException("Got Interrupted in Idle state!");
				}
			}
		}
	}
	
}
