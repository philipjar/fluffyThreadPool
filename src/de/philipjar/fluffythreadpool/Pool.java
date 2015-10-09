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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Pool {
	
	private PrintStream output;
	
	private int maxThreads = 0;
	public enum ThreadStates {
		PRERUN, EXECUTING, IDLE, DEAD
	}
	
	private ArrayList<PoolThread> threads = new ArrayList<PoolThread>();
	private HashMap<Integer, ThreadStates> ThreadIdToState = new HashMap<Integer, ThreadStates>();
	
	LinkedList<Runnable> taskQueue = new LinkedList<Runnable>();
	
	/**
	 * Thread Pool Constructor.
	 * Feed me.
	 * 
	 * @param maxThreads
	 * @param output
	 */
	public Pool(int maxThreads, PrintStream output) {
		this.maxThreads = maxThreads;
		this.output = output;
		buildPool();
	}
	
	private void buildPool() {
		if (maxThreads < 1) {
			throw new PoolException("Max Number of Threads is " + String.valueOf(maxThreads) + "!");
		}
		for (int i = 0; i < maxThreads; i++) {
			threads.add(new PoolThread(this, i));
			threads.get(i).start();
		}
	}
	
	/**
	 * Adds the given Runnable to the execution System.
	 * 
	 * @param runnable
	 */
	public void execute(Runnable runnable) {
		boolean foundIdle = false;
		for (PoolThread thread : threads) {
			if (ThreadIdToState.get(thread.getPTID()).equals(ThreadStates.IDLE)) {
				foundIdle = true;
				thread.exec(runnable);
				break;
			}
		}
		if (!foundIdle) {
			String hash = Integer.toHexString(runnable.hashCode());
			printOut("execute - Didn't found Idle Thread...adding Runnable " + hash + " to Queue. "
					+ "(Queuesize is " + String.valueOf(taskQueue.size()) + ").");
			taskQueue.add(runnable);
		}
	}
	
	public void printOut(String msg) {
		if (output != null)  {
			output.println(msg);
			output.flush();
		}
	}
	
	/**
	 * Gets called back by a PoolThread instance 
	 * to indicate a change in its state.
	 * 
	 * @param ID
	 */
	protected synchronized void myStateChanged(int ID, ThreadStates state) {
		ThreadIdToState.put(ID, state);
		if (state.equals(ThreadStates.IDLE) && !taskQueue.isEmpty()) {
			threads.get(ID).exec(taskQueue.remove());
		}
	}
	
	/**
	 * Shuts down the whole ThreadPool.
	 */
	public void shutdown() {
		printOut("shutdown - Shutting down the Pool.");
		for (PoolThread pThread : threads) { 
			pThread.shutdown();
		}
	}
}
