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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Pool {
	
	private int maxThreads = 0;
	public enum ThreadStates {
		PRERUN, EXECUTING, IDLE, DEAD
	}
	
	private ArrayList<PoolThread> threads = new ArrayList<PoolThread>();
	private HashMap<Integer, ThreadStates> ThreadIdToState = new HashMap<Integer, ThreadStates>();
	LinkedList<Runnable> taskQueue = new LinkedList<Runnable>();
	
	public Pool(int maxThreads) {
		this.maxThreads = maxThreads;
		buildPool();
	}
	
	private void buildPool() {
		if (maxThreads == 0) {
			throw new PoolException("Max Number of Threads is 0 - won't run anything");
		}
		for (int i = 0; i < maxThreads; i++) {
			threads.add(new PoolThread(this, i));
			threads.get(i).start();
		}
	}
	
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
			taskQueue.add(runnable);
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
		for (PoolThread pThread : threads) { 
			pThread.shutdown();
		}
	}
}
